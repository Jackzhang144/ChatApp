package com.chatapp.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppUpdateQuery extends BaseParam {

    private Integer id;

    private String version;

    private String versionFuzzy;

    private String updateDesc;

    private String updateDescFuzzy;

    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    private Integer status;

    private String grayscaleUid;

    private String grayscaleUidFuzzy;

    private Integer fileType;

    private String outerLink;

    private String outerLinkFuzzy;
}
