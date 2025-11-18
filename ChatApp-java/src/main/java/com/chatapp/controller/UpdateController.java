package com.chatapp.controller;

import com.chatapp.annotation.GlobalInterceptor;
import com.chatapp.entity.config.AppConfig;
import com.chatapp.entity.constants.Constants;
import com.chatapp.entity.po.AppUpdate;
import com.chatapp.entity.vo.AppUpdateVO;
import com.chatapp.entity.vo.ResponseVO;
import com.chatapp.service.AppUpdateService;
import com.chatapp.utils.CopyTools;
import com.chatapp.utils.FileDownloadUtils;
import com.chatapp.utils.StringTools;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@RestController("updateController")
@RequestMapping("/update")
@Validated
@Slf4j
public class UpdateController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private AppUpdateService appUpdateService;

    @RequestMapping("/checkVersion")
    @GlobalInterceptor
    public ResponseVO loadAllCategory(String appVersion, String uid) {
        if (StringTools.isEmpty(appVersion)) {
            return getSuccessResponseVO(null);
        }
        AppUpdate appUpdate = appUpdateService.getLatestUpdate(appVersion, uid);
        if (appUpdate == null) {
            return getSuccessResponseVO(null);
        }
        AppUpdateVO updateVO = CopyTools.copy(appUpdate, AppUpdateVO.class);
        File file = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + appUpdate.getId() + Constants.APP_EXE_SUFFIX);
        updateVO.setSize(file.length());
        updateVO.setUpdateList(Arrays.asList(appUpdate.getUpdateDescArray()));
        String fileName = Constants.APP_NAME + appUpdate.getVersion() + Constants.APP_EXE_SUFFIX;
        updateVO.setFileName(fileName);
        return getSuccessResponseVO(updateVO);
    }

    @RequestMapping("/download")
    @GlobalInterceptor
    public void download(HttpServletResponse response, @NotNull Integer id) {
        File file = appUpdateService.getUpdateFile(id);
        FileDownloadUtils.writeFileToResponse(file, response);
    }
}
