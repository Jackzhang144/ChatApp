package com.chatapp.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSessionUserQuery extends BaseParam {

    private String userId;

    private String userIdFuzzy;

    private String contactId;

    private String contactIdFuzzy;

    private String sessionId;

    private String sessionIdFuzzy;

    private String contactName;

    private String contactNameFuzzy;

    private String lastMessage;

    private String lastMessageFuzzy;

    private Long lastReceiveTime;

    private Boolean queryContactInfo;
}
