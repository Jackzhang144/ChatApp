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

        /**
     * 根据分页参数查询聊天会话列表
     * @param param 查询参数对象，包含分页信息和查询条件
     * @return 分页结果对象，包含总记录数、分页信息和当前页数据列表
     */
    @Override
    public PaginationResultVO<ChatSession> findListByPage(ChatSessionQuery param) {
        // 获取符合条件的总记录数
        int count = this.findCountByParam(param);

        // 计算分页大小，如果参数中未指定则使用默认值15
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        // 创建分页对象并设置到查询参数中
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);

        // 查询当前页的数据列表
        List<ChatSession> list = this.findListByParam(param);

        // 构造并返回分页结果对象
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }


    @Override
    public Integer add(ChatSession bean) {
        return this.chatSessionMapper.insert(bean);
    }

        /**
     * 批量添加聊天会话记录
     *
     * @param listBean 聊天会话列表，不能为空
     * @return 成功插入的记录数，如果列表为空则返回0
     */
    @Override
    public Integer addBatch(List<ChatSession> listBean) {
        // 检查输入参数是否为空或空集合
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        // 调用Mapper执行批量插入操作
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
