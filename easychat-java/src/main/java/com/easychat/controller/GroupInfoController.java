package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.vo.GroupInfoVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.GroupInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 *  Controller
 */
@RestController("groupInfoController")
@RequestMapping("/group")
@Validated
public class GroupInfoController extends ABaseController {

    @Resource
    private GroupInfoService groupInfoService;

    /**
     * 保存群组信息
     * @param request HTTP请求对象，用于获取用户token信息
     * @param groupId 群组ID
     * @param groupName 群组名称，不能为空
     * @param groupNotice 群组公告
     * @param joinType 加入类型，不能为空
     * @param avatarFile 群组头像文件
     * @param avatarCover 群组头像封面文件
     * @return ResponseVO 响应结果对象
     * @throws IOException IO异常
     */
    @GlobalInterceptor
    @RequestMapping("/saveGroup")
    public ResponseVO saveGroup(HttpServletRequest request,
                                String groupId,
                                @NotEmpty String groupName,
                                String groupNotice,
                                @NotNull Integer joinType,
                                MultipartFile avatarFile,
                                MultipartFile avatarCover) throws IOException {

        // 获取token用户信息
        TokenUserInfoDto tokenUserInfo = getTokenUserInfo(request);

        // 构建群组信息对象
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupOwnerId(tokenUserInfo.getUserId());
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);

        // 调用服务保存群组信息
        this.groupInfoService.saveGroup(groupInfo, avatarFile, avatarCover);

        return getSuccessResponseVO(null);
    }


    /**
     * 加载当前用户创建的群组列表
     *
     * @param request HTTP请求对象，用于获取用户token信息
     * @return ResponseVO 响应对象，包含用户创建的群组列表数据
     */
    @GlobalInterceptor
    @RequestMapping("/loadMyGroup")
    public ResponseVO loadMyGroup(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        List<GroupInfo> groupInfoList = this.groupInfoService.loadMyGroup(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(groupInfoList);
    }


    /**
     * 获取群组详细信息
     *
     * @param request HTTP请求对象，用于获取用户身份信息
     * @param groupId 群组ID，不能为空
     * @return ResponseVO 响应对象，包含群组详细信息
     */
    @GlobalInterceptor
    @RequestMapping("/getGroupInfo")
    public ResponseVO getGroupInfo(HttpServletRequest request, @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        GroupInfo groupInfo = this.groupInfoService.getGroupDetail(tokenUserInfoDto.getUserId(), groupId);
        return getSuccessResponseVO(groupInfo);
    }

    /**
     * 获取群聊信息用于聊天界面显示
     *
     * @param request HTTP请求对象，用于获取用户身份信息
     * @param groupId 群组ID，不能为空
     * @return ResponseVO 响应对象，包含群组信息和群成员列表
     */
    @GlobalInterceptor
    @RequestMapping("/getGroupInfo4Chat")
    public ResponseVO getGroupInfo4Chat(HttpServletRequest request, @NotEmpty String groupId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        GroupInfoVO groupInfoVO = this.groupInfoService.getGroupInfoForChat(tokenUserInfoDto.getUserId(), groupId);
        return getSuccessResponseVO(groupInfoVO);
    }


}
