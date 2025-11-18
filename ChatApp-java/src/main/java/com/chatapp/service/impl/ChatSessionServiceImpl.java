package com.chatapp.service.impl;

import com.chatapp.entity.enums.PageSize;
import com.chatapp.entity.po.ChatSession;
import com.chatapp.entity.query.ChatSessionQuery;
import com.chatapp.entity.query.SimplePage;
import com.chatapp.entity.vo.PaginationResultVO;
import com.chatapp.mappers.ChatSessionMapper;
import com.chatapp.service.ChatSessionService;
import com.chatapp.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("chatSessionService")
public class ChatSessionServiceImpl implements ChatSessionService {

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Override
    public List<ChatSession> findListByParam(ChatSessionQuery param) {
        return this.chatSessionMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(ChatSessionQuery param) {
        return this.chatSessionMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatSession> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(ChatSession bean) {
        return this.chatSessionMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<ChatSession> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<ChatSession> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(ChatSession bean, ChatSessionQuery param) {
        StringTools.checkParam(param);
        return this.chatSessionMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(ChatSessionQuery param) {
        StringTools.checkParam(param);
        return this.chatSessionMapper.deleteByParam(param);
    }

    @Override
    public ChatSession getChatSessionBySessionId(String sessionId) {
        return this.chatSessionMapper.selectBySessionId(sessionId);
    }

    @Override
    public Integer updateChatSessionBySessionId(ChatSession bean, String sessionId) {
        return this.chatSessionMapper.updateBySessionId(bean, sessionId);
    }

    @Override
    public Integer deleteChatSessionBySessionId(String sessionId) {
        return this.chatSessionMapper.deleteBySessionId(sessionId);
    }
}
