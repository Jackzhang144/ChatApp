package com.easychat.service;

import com.easychat.entity.vo.UserInfoVO;

import java.util.Map;

/**
 * 账户相关业务接口
 */
public interface AccountService {

    /**
     * 生成并缓存图形验证码
     * @return 包含验证码图片和缓存key的信息
     */
    Map<String, String> generateCheckCode();

    /**
     * 校验验证码并注册
     * @param checkCodeKey 验证码缓存key
     * @param email 邮箱
     * @param password 密码
     * @param nickName 昵称
     * @param checkCode 用户输入验证码
     */
    void register(String checkCodeKey, String email, String password, String nickName, String checkCode);

    /**
     * 校验验证码并登录
     * @param checkCodeKey 验证码缓存key
     * @param email 邮箱
     * @param password 密码
     * @param checkCode 用户输入验证码
     * @return 登录后的用户信息
     */
    UserInfoVO login(String checkCodeKey, String email, String password, String checkCode);
}

