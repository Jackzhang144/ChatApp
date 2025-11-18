package com.chatapp.entity.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSession implements Serializable {

    private String sessionId;

    private String lastMessage;

    private Long lastReceiveTime;
}
