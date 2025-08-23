package com.easychat.controller;

import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.enums.GroupStatusEnum;
import com.easychat.entity.enums.UserContactStatusEnum;
import com.easychat.entity.po.GroupInfo;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.vo.GroupInfoVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.exception.BusinessException;
import com.easychat.service.GroupInfoService;
import com.easychat.service.UserContactService;
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

    @Resource
    private UserContactService userContactService;

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
        // 从请求中获取当前用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);

        // 构造群组查询条件，查询当前用户作为群主的群组
        GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfoQuery.setOrderBy("create_time desc");

        // 执行群组查询
        List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(groupInfoQuery);

        // 返回成功响应结果
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
        // 获取群组详细信息并验证用户是否有权限查看
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);

        // 查询群组成员数量
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        Integer memberCount = this.userContactService.findCountByParam(userContactQuery);
        groupInfo.setMemberCount(memberCount);

        return getSuccessResponseVO(groupInfo);
    }

    /**
     * 获取群组详细信息的通用方法
     *
     * @param request HTTP请求对象，用于获取用户身份信息
     * @param groupId 群组ID
     * @return GroupInfo 群组信息对象
     * @throws BusinessException 当用户不在群聊中或群聊不存在/已解散时抛出异常
     */
    private GroupInfo getGroupDetailCommon(HttpServletRequest request, String groupId) {
        // 获取当前用户信息
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);

        // 检查用户是否在群聊中且群聊状态正常
        UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(), groupId);
        if (null == userContact || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException("你不在群聊或者群聊不存在或已解散");
        }

        // 获取群组信息并验证群组状态
        GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
        if (null == groupInfo || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("群聊不存在或已解散");
        }

        return groupInfo;
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
        // 获取群组基本信息并验证权限
        GroupInfo groupInfo = getGroupDetailCommon(request, groupId);

        // 查询群组成员列表，包含用户详细信息，按加入时间升序排列
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setContactId(groupId);
        // 启用关联查询
        userContactQuery.setQueryUserInfo(true);

        userContactQuery.setOrderBy("create_time asc");
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList = this.userContactService.findListByParam(userContactQuery);

        // 构造群聊信息VO对象
        GroupInfoVO groupInfoVO = new GroupInfoVO();
        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setUserContactList(userContactList);

        return getSuccessResponseVO(groupInfoVO);
    }


}