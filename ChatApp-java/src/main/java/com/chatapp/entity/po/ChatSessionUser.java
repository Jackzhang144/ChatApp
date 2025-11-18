package com.chatapp.entity.po;

import com.chatapp.entity.enums.UserContactTypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSessionUser implements Serializable {

    private String userId;

    private String contactId;

    private String sessionId;

    private String contactName;

    private String lastMessage;

    private Long lastReceiveTime;

    private Integer contactType;

    private Integer memberCount;

    public Integer getContactType() {
        if (contactId == null) {
            return contactType;
        }
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        return typeEnum == null ? contactType : typeEnum.getType();
    }
}
