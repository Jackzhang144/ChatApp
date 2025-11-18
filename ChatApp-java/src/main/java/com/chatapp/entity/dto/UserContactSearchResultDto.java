package com.chatapp.entity.dto;

import com.chatapp.entity.enums.UserContactStatusEnum;
import lombok.Data;

@Data
public class UserContactSearchResultDto {
    private String contactId;
    private String contactType;
    private String nickName;
    private Integer status;
    private String statusName;
    private Integer sex;
    private String areaName;

    public String getStatusName() {
        UserContactStatusEnum statusEnum = UserContactStatusEnum.getByStatus(status);
        return statusEnum == null ? statusName : statusEnum.getDesc();
    }
}
