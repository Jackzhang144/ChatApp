package com.chatapp.service;

import com.chatapp.entity.po.AppUpdate;
import com.chatapp.entity.query.AppUpdateQuery;
import com.chatapp.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AppUpdateService {

    List<AppUpdate> findListByParam(AppUpdateQuery param);

    Integer findCountByParam(AppUpdateQuery param);

    PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery param);

    Integer add(AppUpdate bean);

    Integer addBatch(List<AppUpdate> listBean);

    Integer addOrUpdateBatch(List<AppUpdate> listBean);

    Integer updateByParam(AppUpdate bean, AppUpdateQuery param);

    Integer deleteByParam(AppUpdateQuery param);

    AppUpdate getAppUpdateById(Integer id);

    Integer updateAppUpdateById(AppUpdate bean, Integer id);

    Integer deleteAppUpdateById(Integer id);

    void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException;

    void postUpdate(Integer id, Integer status, String grayscaleUid);

    AppUpdate getLatestUpdate(String appVersion, String uid);

    File getUpdateFile(Integer id);
}
