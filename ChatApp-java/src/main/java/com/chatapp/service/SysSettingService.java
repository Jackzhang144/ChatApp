package com.chatapp.service;

import com.chatapp.entity.dto.SysSettingDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SysSettingService {

    void saveSysSetting(SysSettingDto sysSettingDto, MultipartFile robotFile, MultipartFile robotCover) throws IOException;

    SysSettingDto getSysSetting();
}
