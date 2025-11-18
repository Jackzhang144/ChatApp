package com.chatapp.controller;

import com.chatapp.annotation.GlobalInterceptor;
import com.chatapp.entity.dto.SysSettingDto;
import com.chatapp.entity.vo.ResponseVO;
import com.chatapp.entity.vo.SysSettingVO;
import com.chatapp.entity.vo.UserInfoVO;
import com.chatapp.redis.RedisComponent;
import com.chatapp.service.AccountService;
import com.chatapp.utils.CopyTools;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        return getSuccessResponseVO(CopyTools.copy(sysSettingDto, SysSettingVO.class));
    }


}
