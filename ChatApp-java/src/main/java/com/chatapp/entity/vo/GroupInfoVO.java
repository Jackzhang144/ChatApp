package com.chatapp.entity.vo;

import com.chatapp.entity.po.GroupInfo;
import com.chatapp.entity.po.UserContact;
import lombok.Data;

import java.util.List;

@Data
public class GroupInfoVO {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;
}
