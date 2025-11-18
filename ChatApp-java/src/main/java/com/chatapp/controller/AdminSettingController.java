package com.chatapp.controller;

import com.chatapp.annotation.GlobalInterceptor;
import com.chatapp.entity.dto.SysSettingDto;
import com.chatapp.entity.vo.ResponseVO;
import com.chatapp.service.SysSettingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;

@RestController("adminSettingController")
@RequestMapping("/admin")
public class AdminSettingController extends ABaseController {
    @Resource
    private SysSettingService sysSettingService;

    @RequestMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(SysSettingDto sysSettingDto,
                                     MultipartFile robotFile,
                                     MultipartFile robotCover) throws IOException {
        sysSettingService.saveSysSetting(sysSettingDto, robotFile, robotCover);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting() {
        SysSettingDto sysSettingDto = sysSettingService.getSysSetting();
        return getSuccessResponseVO(sysSettingDto);
    }
}
