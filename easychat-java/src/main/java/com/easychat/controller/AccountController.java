package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.redis.RedisComponent;
import com.easychat.entity.vo.UserInfoVO;
import com.easychat.service.AccountService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private AccountService accountService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        return getSuccessResponseVO(accountService.generateCheckCode());
    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode) {
        accountService.register(checkCodeKey, email, password, nickName, checkCode);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            @NotEmpty @Email String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCode) {
        UserInfoVO userInfoVO = accountService.login(checkCodeKey, email, password, checkCode);
        return getSuccessResponseVO(userInfoVO);
    }

    @GlobalInterceptor
    @RequestMapping("/getSysSetting")
    public ResponseVO getSysSetting() {
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }


}
