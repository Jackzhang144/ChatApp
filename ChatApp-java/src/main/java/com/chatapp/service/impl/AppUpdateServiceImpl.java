package com.chatapp.service.impl;

import com.chatapp.entity.config.AppConfig;
import com.chatapp.entity.constants.Constants;
import com.chatapp.entity.enums.AppUpdateFileTypeEnum;
import com.chatapp.entity.enums.AppUpdateSatusEnum;
import com.chatapp.entity.enums.PageSize;
import com.chatapp.entity.enums.ResponseCodeEnum;
import com.chatapp.entity.po.AppUpdate;
import com.chatapp.entity.query.AppUpdateQuery;
import com.chatapp.entity.query.SimplePage;
import com.chatapp.entity.vo.PaginationResultVO;
import com.chatapp.exception.BusinessException;
import com.chatapp.mappers.AppUpdateMapper;
import com.chatapp.service.AppUpdateService;
import com.chatapp.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service("appUpdateService")
public class AppUpdateServiceImpl implements AppUpdateService {

    @Resource
    private AppConfig appConfig;

    @Resource
    private AppUpdateMapper<AppUpdate, AppUpdateQuery> appUpdateMapper;

    @Override
    public List<AppUpdate> findListByParam(AppUpdateQuery param) {
        return this.appUpdateMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(AppUpdateQuery param) {
        return this.appUpdateMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<AppUpdate> findListByPage(AppUpdateQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<AppUpdate> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(AppUpdate bean) {
        return this.appUpdateMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<AppUpdate> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.appUpdateMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(AppUpdate bean, AppUpdateQuery param) {
        StringTools.checkParam(param);
        return this.appUpdateMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(AppUpdateQuery param) {
        StringTools.checkParam(param);
        return this.appUpdateMapper.deleteByParam(param);
    }

    @Override
    public AppUpdate getAppUpdateById(Integer id) {
        return this.appUpdateMapper.selectById(id);
    }

    @Override
    public Integer updateAppUpdateById(AppUpdate bean, Integer id) {
        return this.appUpdateMapper.updateById(bean, id);
    }

    @Override
    public Integer deleteAppUpdateById(Integer id) {
        AppUpdate dbInfo = this.getAppUpdateById(id);
        if (dbInfo == null) {
            return 0;
        }
        if (!AppUpdateSatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return this.appUpdateMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUpdate(AppUpdate appUpdate, MultipartFile file) throws IOException {
        AppUpdateFileTypeEnum fileTypeEnum = AppUpdateFileTypeEnum.getByType(appUpdate.getFileType());
        if (fileTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        if (appUpdate.getId() != null) {
            AppUpdate dbInfo = this.getAppUpdateById(appUpdate.getId());
            if (dbInfo == null || !AppUpdateSatusEnum.INIT.getStatus().equals(dbInfo.getStatus())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        AppUpdateQuery updateQuery = new AppUpdateQuery();
        updateQuery.setOrderBy("id desc");
        updateQuery.setSimplePage(new SimplePage(0, 1));
        List<AppUpdate> appUpdateList = appUpdateMapper.selectList(updateQuery);
        if (!appUpdateList.isEmpty()) {
            AppUpdate lastest = appUpdateList.get(0);
            long dbVersion = Long.parseLong(lastest.getVersion().replace(".", ""));
            long currentVersion = Long.parseLong(appUpdate.getVersion().replace(".", ""));
            if (appUpdate.getId() == null && currentVersion <= dbVersion) {
                throw new BusinessException("当前版本必须大于历史版本");
            }
            if (appUpdate.getId() != null && currentVersion >= dbVersion && !appUpdate.getId().equals(lastest.getId())) {
                throw new BusinessException("当前版本必须大于历史版本");
            }

            AppUpdate versionDb = appUpdateMapper.selectByVersion(appUpdate.getVersion());
            if (appUpdate.getId() != null && versionDb != null && !versionDb.getId().equals(appUpdate.getId())) {
                throw new BusinessException("版本号已存在");
            }
        }

        if (appUpdate.getId() == null) {
            appUpdate.setCreateTime(new Date());
            appUpdate.setStatus(AppUpdateSatusEnum.INIT.getStatus());
            appUpdateMapper.insert(appUpdate);
        } else {
            appUpdateMapper.updateById(appUpdate, appUpdate.getId());
        }

        if (file != null) {
            File folder = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            file.transferTo(new File(folder.getAbsolutePath() + "/" + appUpdate.getId() + Constants.APP_EXE_SUFFIX));
        }
    }

    @Override
    public void postUpdate(Integer id, Integer status, String grayscaleUid) {
        AppUpdateSatusEnum statusEnum = AppUpdateSatusEnum.getByStatus(status);
        if (statusEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        AppUpdate appUpdate = this.getAppUpdateById(id);
        if (appUpdate == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (AppUpdateSatusEnum.GRAYSCALE == statusEnum && StringTools.isEmpty(grayscaleUid)) {
            throw new BusinessException("灰度发布时必须填写灰度UID");
        }
        AppUpdate update = new AppUpdate();
        update.setStatus(statusEnum.getStatus());
        update.setGrayscaleUid(grayscaleUid);
        appUpdateMapper.updateById(update, id);
    }

    @Override
    public AppUpdate getLatestUpdate(String appVersion, String uid) {
        return appUpdateMapper.selectLatestUpdate(appVersion, uid);
    }

    @Override
    public File getUpdateFile(Integer id) {
        AppUpdate appUpdate = this.getAppUpdateById(id);
        if (appUpdate == null) {
            return null;
        }
        File file = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_FOLDER + appUpdate.getId() + Constants.APP_EXE_SUFFIX);
        if (!file.exists()) {
            return null;
        }
        return file;
    }
}
