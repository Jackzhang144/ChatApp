package com.easychat.entity.vo;

import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.po.UserContact;
import lombok.Data;

import java.util.List;

@Data
public class GroupInfoVO {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;
}
