package com.chatapp.service.impl;

import com.chatapp.entity.config.AppConfig;
import com.chatapp.entity.constants.Constants;
import com.chatapp.entity.dto.MessageSendDto;
import com.chatapp.entity.dto.SysSettingDto;
import com.chatapp.entity.dto.TokenUserInfoDto;
import com.chatapp.entity.enums.*;
import com.chatapp.entity.po.ChatMessage;
import com.chatapp.entity.po.ChatSession;
import com.chatapp.entity.po.ChatSessionUser;
import com.chatapp.entity.po.UserContact;
import com.chatapp.entity.query.*;
import com.chatapp.entity.vo.PaginationResultVO;
import com.chatapp.exception.BusinessException;
import com.chatapp.mappers.ChatMessageMapper;
import com.chatapp.mappers.ChatSessionMapper;
import com.chatapp.mappers.ChatSessionUserMapper;
import com.chatapp.mappers.UserContactMapper;
import com.chatapp.redis.RedisComponent;
import com.chatapp.service.ChatMessageService;
import com.chatapp.utils.CopyTools;
import com.chatapp.utils.StringTools;
import com.chatapp.websocket.MessageHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service("chatMessageService")
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public List<ChatMessage> findListByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.deleteByParam(param);
    }

    @Override
    public ChatMessage getChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    @Override
    public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    @Override
    public Integer deleteChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }

        /**
     * 保存聊天消息，并更新会话相关信息。
     * <p>
     * 该方法首先校验发送用户是否具备与目标联系人通信的权限（机器人除外），
     * 然后根据消息类型和联系人类型构建或获取对应的会话ID，
     * 接着插入新的聊天记录并更新会话信息及参与用户的会话状态，
     * 最终通过消息处理器将封装好的消息发送出去。
     *
     * @param chatMessage         聊天消息对象，包含待保存的消息内容及相关属性
     * @param tokenUserInfoDto    当前登录用户的信息对象，用于身份验证和信息填充
     * @return MessageSendDto     封装后的消息发送数据传输对象，供后续处理使用
     */
    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        // 非机器人用户需要检查是否有权与指定联系人通信
        if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())) {
            List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
            if (!contactList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (UserContactTypeEnum.USER == userContactTypeEnum) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        String sessionId;
        String sendUserId = tokenUserInfoDto.getUserId();
        String contactId = chatMessage.getContactId();
        long curTime = System.currentTimeMillis();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        String lastMessage = chatMessage.getMessageContent();
        String messageContent = StringTools.resetMessageContent(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();

        // 根据不同类型的消息决定如何生成会话 ID
        if (ArrayUtils.contains(new Integer[]{
                MessageTypeEnum.CHAT.getType(),
                MessageTypeEnum.GROUP_CREATE.getType(),
                MessageTypeEnum.ADD_FRIEND.getType(),
                MessageTypeEnum.MEDIA_CHAT.getType()
        }, messageTypeEnum.getType())) {
            if (UserContactTypeEnum.USER == contactTypeEnum) {
                sessionId = StringTools.getChatSessionId4User(new String[]{sendUserId, contactId});
            } else {
                sessionId = StringTools.getChatSessionId4Group(contactId);
            }
        } else {
            sessionId = chatMessage.getSessionId();
        }

        // 填充消息基础字段并插入数据库
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setSendTime(curTime);
        chatMessage.setContactType(contactTypeEnum.getType());
        chatMessage.setStatus(status);
        chatMessageMapper.insert(chatMessage);

        // 更新会话最后接收时间和最新消息内容
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(curTime);
        chatSession.setLastMessage(lastMessage);
        chatSessionMapper.insertOrUpdate(chatSession);

        List<ChatSessionUser> chatSessionUserList = new ArrayList<>();

        // 若是单聊，则分别更新双方的会话用户信息；若是群聊则查询所有成员
        if (UserContactTypeEnum.USER == contactTypeEnum) {
            ChatSessionUser applySessionUser = new ChatSessionUser();
            applySessionUser.setUserId(sendUserId);
            applySessionUser.setContactId(contactId);
            applySessionUser.setSessionId(sessionId);
            applySessionUser.setLastReceiveTime(curTime);
            applySessionUser.setLastMessage(lastMessage);

            ChatSessionUser contactSessionUser = new ChatSessionUser();
            contactSessionUser.setUserId(contactId);
            contactSessionUser.setContactId(sendUserId);
            contactSessionUser.setSessionId(sessionId);
            contactSessionUser.setLastReceiveTime(curTime);
            contactSessionUser.setLastMessage(lastMessage);

            chatSessionUserList.add(applySessionUser);
            chatSessionUserList.add(contactSessionUser);
            chatSessionUserMapper.insertOrUpdateBatch(chatSessionUserList);
        } else {
            ChatSessionUserQuery sessionUserQuery = new ChatSessionUserQuery();
            sessionUserQuery.setContactId(contactId);
            chatSessionUserList = chatSessionUserMapper.selectList(sessionUserQuery);
        }

        // 构造返回 DTO 并发送消息
        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setContactType(contactTypeEnum.getType());
        messageSendDto.setMemberCount(chatSessionUserList.size());
        messageSendDto.setLastMessage(lastMessage);
        messageHandler.sendMessage(messageSendDto);
        return messageSendDto;
    }


        /**
     * 保存用户发送的文件（包括图片、视频等）到服务器，并更新消息状态。
     * <p>
     * 此方法会校验消息是否存在以及是否属于当前用户，检查文件大小限制，
     * 将文件保存至指定目录，并根据需要保存封面图。完成后更新数据库中的消息状态，
     * 并通过消息处理器通知相关用户。
     *
     * @param userId    发送用户的ID，用于权限验证
     * @param messageId 消息ID，用于获取消息信息及命名存储文件
     * @param file      用户上传的主文件（如文档、图片或视频）
     * @param cover     视频/图片的封面图文件（仅在媒体类型为图像或视频时使用）
     * @throws BusinessException 当文件大小超出限制、消息不存在或不属于该用户、IO异常等情况时抛出业务异常
     */
    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        // 获取并校验聊天消息信息
        ChatMessage chatMessage = this.getChatMessageByMessageId(messageId);
        if (chatMessage == null || !userId.equals(chatMessage.getSendUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 根据文件类型获取系统设置中对应的文件大小上限
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        Long fileSizeLimit;
        if (MediaFileTypeEnum.FILE.getType().equals(chatMessage.getFileType())) {
            fileSizeLimit = sysSettingDto.getMaxFileSize() * Constants.FILE_SIZE_MB;
        } else if (MediaFileTypeEnum.IMAGE.getType().equals(chatMessage.getFileType())) {
            fileSizeLimit = sysSettingDto.getMaxImageSize() * Constants.FILE_SIZE_MB;
        } else {
            fileSizeLimit = sysSettingDto.getMaxVideoSize() * Constants.FILE_SIZE_MB;
        }

        // 判断上传文件是否超过大小限制
        if (fileSizeLimit < file.getSize()) {
            throw new BusinessException("文件大小不能超过" + fileSizeLimit / Constants.FILE_SIZE_MB + "MB");
        }

        // 创建目标文件夹路径
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFolder = new File(folder);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        // 保存主文件
        File targetFile = new File(targetFolder, messageId.toString());
        try {
            file.transferTo(targetFile);

            // 若是图片或视频，则同时保存其封面图
            if (MediaFileTypeEnum.IMAGE.getType().equals(chatMessage.getFileType()) ||
                    MediaFileTypeEnum.VIDEO.getType().equals(chatMessage.getFileType())) {
                File coverFile = new File(targetFolder, messageId + Constants.COVER_IMAGE_SUFFIX);
                cover.transferTo(coverFile);
            }

            // 更新消息状态为已发送
            ChatMessage updateInfo = new ChatMessage();
            updateInfo.setStatus(MessageStatusEnum.SENDED.getStatus());
            this.updateChatMessageByMessageId(updateInfo, messageId);

            // 构造发送DTO对象并通过消息处理器广播给客户端
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
            messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
            messageSendDto.setSendTime(System.currentTimeMillis());
            messageHandler.sendMessage(messageSendDto);

        } catch (Exception e) {
            log.error("保存文件失败", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }


    @Override
    public File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long messageId, Boolean showCover) {
        ChatMessage chatMessage = this.getChatMessageByMessageId(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        if (chatMessage.getContactType().equals(UserContactTypeEnum.USER.getType())) {
            UserContact userContact = userContactMapper.selectByUserIdAndContactId(tokenUserInfoDto.getUserId(), chatMessage.getSendUserId());
            if (userContact == null || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.CODE_902);
            }
        }
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFile;
        boolean needCover = Boolean.TRUE.equals(showCover) &&
                (MediaFileTypeEnum.IMAGE.getType().equals(chatMessage.getFileType()) ||
                        MediaFileTypeEnum.VIDEO.getType().equals(chatMessage.getFileType()));
        if (needCover) {
            targetFile = new File(folder, messageId + Constants.COVER_IMAGE_SUFFIX);
        } else {
            targetFile = new File(folder, messageId.toString());
        }
        if (!targetFile.exists()) {
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        return targetFile;
    }

    @Override
    public File buildDownloadFile(TokenUserInfoDto tokenUserInfoDto, String fileId, Boolean showCover) {
        if (!StringTools.isNumber(fileId)) {
            String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
            String avatarPath = appConfig.getProjectFolder() + avatarFolderName + fileId + Constants.IMAGE_SUFFIX;
            if (Boolean.TRUE.equals(showCover)) {
                avatarPath = avatarPath + Constants.COVER_IMAGE_SUFFIX;
            }
            File avatarFile = new File(avatarPath);
            if (!avatarFile.exists()) {
                throw new BusinessException(ResponseCodeEnum.CODE_602);
            }
            return avatarFile;
        }
        return downloadFile(tokenUserInfoDto, Long.parseLong(fileId), showCover);
    }

    @Override
    public MessageSendDto buildSystemMessage(String contactId, TokenUserInfoDto tokenUserInfoDto, MessageTypeEnum messageTypeEnum, String messageContent) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setMessageType(messageTypeEnum.getType());
        return saveMessage(chatMessage, tokenUserInfoDto);
    }
}
