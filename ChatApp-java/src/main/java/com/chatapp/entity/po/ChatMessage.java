package com.chatapp.entity.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatMessage implements Serializable {

    private Long messageId;

    private String sessionId;

    private Integer messageType;

    private String messageContent;

    private String sendUserId;

    private String sendUserNickName;

    private Long sendTime;

    private String contactId;

    private Integer contactType;

    private Long fileSize;

    private String fileName;

    private Integer fileType;

    private Integer status;
}
