package com.chatapp.service.impl;

import com.chatapp.entity.constants.Constants;
import com.chatapp.entity.vo.UserInfoVO;
import com.chatapp.exception.BusinessException;
import com.chatapp.redis.RedisUtils;
import com.chatapp.service.AccountService;
import com.chatapp.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 账户相关业务实现
 */
@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;

    @Override
    public Map<String, String> generateCheckCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        String redisKey = buildRedisKey(checkCodeKey);
        redisUtils.setex(redisKey, code, Constants.REDIS_TIME_1MIN * 10);

        Map<String, String> result = new HashMap<>();
        result.put("checkCode", captcha.toBase64());
        result.put("checkCodeKey", checkCodeKey);
        return result;
    }

    @Override
    public void register(String checkCodeKey, String email, String password, String nickName, String checkCode) {
        try {
            validateCheckCode(checkCodeKey, checkCode);
            userInfoService.register(email, nickName, password);
        } finally {
            removeCheckCode(checkCodeKey);
        }
    }

    @Override
    public UserInfoVO login(String checkCodeKey, String email, String password, String checkCode) {
        try {
            validateCheckCode(checkCodeKey, checkCode);
            return userInfoService.login(email, password);
        } finally {
            removeCheckCode(checkCodeKey);
        }
    }

    private void validateCheckCode(String checkCodeKey, String checkCode) {
        String redisKey = buildRedisKey(checkCodeKey);
        Object cacheCode = redisUtils.get(redisKey);
        if (cacheCode == null || checkCode == null || !checkCode.equalsIgnoreCase(cacheCode.toString())) {
            throw new BusinessException("图片验证码不正确");
        }
    }

    private void removeCheckCode(String checkCodeKey) {
        redisUtils.delete(buildRedisKey(checkCodeKey));
    }

    private String buildRedisKey(String checkCodeKey) {
        return Constants.REDIS_KEY_CHECK_CODE + checkCodeKey;
    }
}

