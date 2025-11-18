package com.chatapp.service.impl;

import com.chatapp.entity.config.AppConfig;
import com.chatapp.entity.constants.Constants;
import com.chatapp.entity.dto.SysSettingDto;
import com.chatapp.redis.RedisComponent;
import com.chatapp.service.SysSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class SysSettingServiceImpl implements SysSettingService {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public void saveSysSetting(SysSettingDto sysSettingDto, MultipartFile robotFile, MultipartFile robotCover) throws IOException {
        if (robotFile != null) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists() && !targetFileFolder.mkdirs()) {
                log.warn("Failed to create directory {}", targetFileFolder.getAbsolutePath());
            }
            String filePath = targetFileFolder.getPath() + "/" + Constants.ROBOT_UID + Constants.IMAGE_SUFFIX;
            robotFile.transferTo(new File(filePath));
            if (robotCover != null) {
                robotCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
            }
        }
        redisComponent.saveSysSetting(sysSettingDto);
    }

    @Override
    public SysSettingDto getSysSetting() {
        return redisComponent.getSysSetting();
    }
}
