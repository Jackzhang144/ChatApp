package com.chatapp.service;

import com.chatapp.entity.po.ChatSessionUser;
import com.chatapp.entity.query.ChatSessionUserQuery;
import com.chatapp.entity.vo.PaginationResultVO;

import java.util.List;

public interface ChatSessionUserService {

    List<ChatSessionUser> findListByParam(ChatSessionUserQuery param);

    Integer findCountByParam(ChatSessionUserQuery param);

    PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery param);

    Integer add(ChatSessionUser bean);

    Integer addBatch(List<ChatSessionUser> listBean);

    Integer addOrUpdateBatch(List<ChatSessionUser> listBean);

    Integer updateByParam(ChatSessionUser bean, ChatSessionUserQuery param);

    Integer deleteByParam(ChatSessionUserQuery param);

    ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId);

    Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId);

    Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId);

    void updateRedundanceInfo(String contactName, String contactId);
}
