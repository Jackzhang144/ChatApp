package com.chatapp.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessageQuery extends BaseParam {

    private Long messageId;

    private String sessionId;

    private String sessionIdFuzzy;

    private Integer messageType;

    private String messageContent;

    private String messageContentFuzzy;

    private String sendUserId;

    private String sendUserIdFuzzy;

    private String sendUserNickName;

    private String sendUserNickNameFuzzy;

    private Long sendTime;

    private String contactId;

    private String contactIdFuzzy;

    private Integer contactType;

    private Long fileSize;

    private String fileName;

    private String fileNameFuzzy;

    private Integer fileType;

    private Integer status;

    private List<String> contactIdList;

    private Long lastReceiveTime;
}
