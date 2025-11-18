package com.chatapp.entity.query;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 靓号表参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfoBeautyQuery extends BaseParam {


    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 邮箱
     */
    private String email;

    private String emailFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 0：未使用 1：已使用
     */
    private Integer status;
}
