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

    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
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

        chatMessage.setSessionId(sessionId);
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setSendTime(curTime);
        chatMessage.setContactType(contactTypeEnum.getType());
        chatMessage.setStatus(status);
        chatMessageMapper.insert(chatMessage);

        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(curTime);
        chatSession.setLastMessage(lastMessage);
        chatSessionMapper.insertOrUpdate(chatSession);

        List<ChatSessionUser> chatSessionUserList = new ArrayList<>();
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

        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setContactType(contactTypeEnum.getType());
        messageSendDto.setMemberCount(chatSessionUserList.size());
        messageSendDto.setLastMessage(lastMessage);
        messageHandler.sendMessage(messageSendDto);
        return messageSendDto;
    }

    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = this.getChatMessageByMessageId(messageId);
        if (chatMessage == null || !userId.equals(chatMessage.getSendUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        Long fileSizeLimit;
        if (MediaFileTypeEnum.FILE.getType().equals(chatMessage.getFileType())) {
            fileSizeLimit = sysSettingDto.getMaxFileSize() * Constants.FILE_SIZE_MB;
        } else if (MediaFileTypeEnum.IMAGE.getType().equals(chatMessage.getFileType())) {
            fileSizeLimit = sysSettingDto.getMaxImageSize() * Constants.FILE_SIZE_MB;
        } else {
            fileSizeLimit = sysSettingDto.getMaxVideoSize() * Constants.FILE_SIZE_MB;
        }
        if (fileSizeLimit < file.getSize()) {
            throw new BusinessException("文件大小不能超过" + fileSizeLimit / Constants.FILE_SIZE_MB + "MB");
        }
        String folder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFolder = new File(folder);
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        File targetFile = new File(targetFolder, messageId.toString());
        try {
            file.transferTo(targetFile);
            if (MediaFileTypeEnum.IMAGE.getType().equals(chatMessage.getFileType()) ||
                    MediaFileTypeEnum.VIDEO.getType().equals(chatMessage.getFileType())) {
                File coverFile = new File(targetFolder, messageId + Constants.COVER_IMAGE_SUFFIX);
                cover.transferTo(coverFile);
            }
            ChatMessage updateInfo = new ChatMessage();
            updateInfo.setStatus(MessageStatusEnum.SENDED.getStatus());
            this.updateChatMessageByMessageId(updateInfo, messageId);
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
