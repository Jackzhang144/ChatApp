package com.easychat.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserInfoVO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 0:女 1:男
     */
    private Integer sex;

    private Integer joinType;

    /**
     * 个性签名
     */
    private String personalSignature;

    private String areaCode;

    private String areaName;

    private String token;

    private Boolean admin;

    private Integer contactStatus;
}
