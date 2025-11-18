package com.chatapp.entity.dto;

import com.chatapp.entity.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDto implements Serializable {

    // 最大群组数
    private Integer maxGroupCount = 5;
    // 群组最多人数
    private Integer maxGroupMemberCount = 500;
    // 图片大小
    private Integer maxImageSize = 2;
    // 视频大小
    private Integer maxVideoSize = 5;
    // 文件大小
    private Integer maxFileSize = 5;
    // 机器人ID
    private String robotUid = Constants.ROBOT_UID;
    // 机器人昵称
    private String robotNickName = "ChatApp";
    // 欢迎语
    private String robotWelcome = "欢迎使用ChatApp";
}
