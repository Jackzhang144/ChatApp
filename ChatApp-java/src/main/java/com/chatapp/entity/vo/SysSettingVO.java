package com.chatapp.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysSettingVO implements Serializable {

    private Integer maxImageSize = 2;

    private Integer maxVideoSize = 5;

    private Integer maxFileSize = 5;
}
