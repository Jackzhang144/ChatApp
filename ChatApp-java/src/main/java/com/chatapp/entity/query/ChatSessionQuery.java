package com.chatapp.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSessionQuery extends BaseParam {

    private String sessionId;

    private String sessionIdFuzzy;

    private String lastMessage;

    private String lastMessageFuzzy;

    private Long lastReceiveTime;
}
