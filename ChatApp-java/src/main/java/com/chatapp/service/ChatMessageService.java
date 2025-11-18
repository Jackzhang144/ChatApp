package com.chatapp.service;

import com.chatapp.entity.dto.MessageSendDto;
import com.chatapp.entity.dto.TokenUserInfoDto;
import com.chatapp.entity.enums.MessageTypeEnum;
import com.chatapp.entity.po.ChatMessage;
import com.chatapp.entity.query.ChatMessageQuery;
import com.chatapp.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface ChatMessageService {

    List<ChatMessage> findListByParam(ChatMessageQuery param);

    Integer findCountByParam(ChatMessageQuery param);

    PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param);

    Integer add(ChatMessage bean);

    Integer addBatch(List<ChatMessage> listBean);

    Integer addOrUpdateBatch(List<ChatMessage> listBean);

    Integer updateByParam(ChatMessage bean, ChatMessageQuery param);

    Integer deleteByParam(ChatMessageQuery param);

    ChatMessage getChatMessageByMessageId(Long messageId);

    Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId);

    Integer deleteChatMessageByMessageId(Long messageId);

    MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);

    void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover);

    File downloadFile(TokenUserInfoDto tokenUserInfoDto, Long messageId, Boolean showCover);

    File buildDownloadFile(TokenUserInfoDto tokenUserInfoDto, String fileId, Boolean showCover);

    MessageSendDto buildSystemMessage(String contactId, TokenUserInfoDto tokenUserInfoDto, MessageTypeEnum messageTypeEnum, String messageContent);
}
