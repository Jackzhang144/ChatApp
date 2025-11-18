package com.chatapp.service.impl;

import com.chatapp.entity.dto.MessageSendDto;
import com.chatapp.entity.enums.MessageTypeEnum;
import com.chatapp.entity.enums.PageSize;
import com.chatapp.entity.enums.UserContactStatusEnum;
import com.chatapp.entity.enums.UserContactTypeEnum;
import com.chatapp.entity.po.ChatSessionUser;
import com.chatapp.entity.po.UserContact;
import com.chatapp.entity.query.ChatSessionUserQuery;
import com.chatapp.entity.query.SimplePage;
import com.chatapp.entity.query.UserContactQuery;
import com.chatapp.entity.vo.PaginationResultVO;
import com.chatapp.mappers.ChatSessionUserMapper;
import com.chatapp.mappers.UserContactMapper;
import com.chatapp.service.ChatSessionUserService;
import com.chatapp.utils.StringTools;
import com.chatapp.websocket.MessageHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("chatSessionUserService")
public class ChatSessionUserServiceImpl implements ChatSessionUserService {

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Override
    public List<ChatSessionUser> findListByParam(ChatSessionUserQuery param) {
        return this.chatSessionUserMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(ChatSessionUserQuery param) {
        return this.chatSessionUserMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatSessionUser> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(ChatSessionUser bean) {
        return this.chatSessionUserMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<ChatSessionUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<ChatSessionUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(ChatSessionUser bean, ChatSessionUserQuery param) {
        StringTools.checkParam(param);
        return this.chatSessionUserMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(ChatSessionUserQuery param) {
        StringTools.checkParam(param);
        return this.chatSessionUserMapper.deleteByParam(param);
    }

    @Override
    public ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.selectByUserIdAndContactId(userId, contactId);
    }

    @Override
    public Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
        return this.chatSessionUserMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    @Override
    public Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    @Override
    public void updateRedundanceInfo(String contactName, String contactId) {
        if (StringTools.isEmpty(contactName)) {
            return;
        }
        ChatSessionUser updateInfo = new ChatSessionUser();
        updateInfo.setContactName(contactName);

        ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
        chatSessionUserQuery.setContactId(contactId);
        this.chatSessionUserMapper.updateByParam(updateInfo, chatSessionUserQuery);

        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (contactTypeEnum == UserContactTypeEnum.GROUP) {
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactType(contactTypeEnum.getType());
            messageSendDto.setContactId(contactId);
            messageSendDto.setExtendData(contactName);
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageHandler.sendMessage(messageSendDto);
            return;
        }

        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactType(UserContactTypeEnum.USER.getType());
        userContactQuery.setContactId(contactId);
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = userContactMapper.selectList(userContactQuery);
        for (UserContact userContact : userContactList) {
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactType(contactTypeEnum.getType());
            messageSendDto.setContactId(userContact.getUserId());
            messageSendDto.setExtendData(contactName);
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageSendDto.setSendUserId(contactId);
            messageSendDto.setSendUserNickName(contactName);
            messageHandler.sendMessage(messageSendDto);
        }
    }
}
