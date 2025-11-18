package com.chatapp.entity.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenUserInfoDto implements Serializable {

    private String token;
    private String userId;
    private String nickName;
    private Boolean admin;
}
