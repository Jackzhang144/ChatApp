package com.chatapp.service;

import com.chatapp.entity.po.ChatSession;
import com.chatapp.entity.query.ChatSessionQuery;
import com.chatapp.entity.vo.PaginationResultVO;

import java.util.List;

public interface ChatSessionService {

    List<ChatSession> findListByParam(ChatSessionQuery param);

    Integer findCountByParam(ChatSessionQuery param);

    PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery param);

    Integer add(ChatSession bean);

    Integer addBatch(List<ChatSession> listBean);

    Integer addOrUpdateBatch(List<ChatSession> listBean);

    Integer updateByParam(ChatSession bean, ChatSessionQuery param);

    Integer deleteByParam(ChatSessionQuery param);

    ChatSession getChatSessionBySessionId(String sessionId);

    Integer updateChatSessionBySessionId(ChatSession bean, String sessionId);

    Integer deleteChatSessionBySessionId(String sessionId);
}
