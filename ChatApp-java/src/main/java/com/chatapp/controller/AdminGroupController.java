package com.chatapp.controller;

import com.chatapp.annotation.GlobalInterceptor;
import com.chatapp.entity.enums.ResponseCodeEnum;
import com.chatapp.entity.po.GroupInfo;
import com.chatapp.entity.query.GroupInfoQuery;
import com.chatapp.entity.vo.PaginationResultVO;
import com.chatapp.entity.vo.ResponseVO;
import com.chatapp.exception.BusinessException;
import com.chatapp.service.GroupInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;

@RestController("adminGroupController")
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController {

    @Resource
    private GroupInfoService groupInfoService;

    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadGroup(GroupInfoQuery groupInfoQuery) {
        groupInfoQuery.setQueryGroupOwnerName(true);
        groupInfoQuery.setQueryMemberCount(true);
        PaginationResultVO resultVO = groupInfoService.findListByPage(groupInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup(@NotEmpty String groupId) {
        GroupInfo groupInfo = groupInfoService.getGroupInfoByGroupId(groupId);
        if (null == groupInfo) {
            throw new BusinessException(ResponseCodeEnum.CODE_200);
        }
        groupInfoService.dissolutionGroup(groupInfo.getGroupOwnerId(), groupId);
        return getSuccessResponseVO(null);
    }
}
