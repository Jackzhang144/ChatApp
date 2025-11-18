package com.chatapp.entity.po;

import lombok.Data;

import java.io.Serializable;


/**
 * 靓号表
 */
@Data
public class UserInfoBeauty implements Serializable {


    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 0：未使用 1：已使用
     */
    private Integer status;
}
