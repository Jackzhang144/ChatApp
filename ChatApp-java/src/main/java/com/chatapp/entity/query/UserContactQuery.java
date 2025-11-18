package com.chatapp.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 联系人参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserContactQuery extends BaseParam {


    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 联系人ID或者群组ID
     */
    private String contactId;

    private String contactIdFuzzy;

    /**
     * 联系人类型 0:好友 1:群组
     */
    private Integer contactType;

    /**
     * 创建时间
     */
    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 状态 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑
     */
    private Integer status;

    /**
     * 最后更新时间
     */
    private String lastUpdateTime;

    private String lastUpdateTimeStart;

    private String lastUpdateTimeEnd;

    private Boolean queryUserInfo;

    private Boolean queryContactUserInfo;

    private Boolean queryGroupInfo;

    private Boolean excludeMyGroup;

    private Integer[] statusArray;
}
