package com.chatapp.entity.po;

import com.chatapp.entity.enums.DateTimePatternEnum;
import com.chatapp.utils.DateUtil;
import com.chatapp.utils.StringTools;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class AppUpdate implements Serializable {

    private Integer id;

    private String version;

    private String updateDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer status;

    private String grayscaleUid;

    private Integer fileType;

    private String outerLink;

    private String[] updateDescArray;

    public String[] getUpdateDescArray() {
        if (!StringTools.isEmpty(updateDesc)) {
            return updateDesc.split("\\|");
        }
        return updateDescArray;
    }

    public void setUpdateDescArray(String[] updateDescArray) {
        this.updateDescArray = updateDescArray;
    }

    @Override
    public String toString() {
        return "自增ID:" + (id == null ? "空" : id) + "，版本号:" + (version == null ? "空" : version) + "，更新描述:" + (updateDesc == null ? "空" : updateDesc) + "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，状态:" + (status == null ? "空" : status) + "，灰度uid:" + (grayscaleUid == null ? "空" : grayscaleUid) + "，文件类型:" + (fileType == null ? "空" : fileType) + "，外链地址:" + (outerLink == null ? "空" : outerLink);
    }
}
