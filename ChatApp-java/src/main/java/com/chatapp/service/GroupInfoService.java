package com.chatapp.service;

import com.chatapp.entity.dto.TokenUserInfoDto;
import com.chatapp.entity.enums.MessageTypeEnum;
import com.chatapp.entity.po.GroupInfo;
import com.chatapp.entity.query.GroupInfoQuery;
import com.chatapp.entity.vo.GroupInfoVO;
import com.chatapp.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


/**
 *  业务接口
 */
public interface GroupInfoService {

    /**
     * 根据条件查询列表
     */
    List<GroupInfo> findListByParam(GroupInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(GroupInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param);

    /**
     * 新增
     */
    Integer add(GroupInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<GroupInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<GroupInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(GroupInfo bean, GroupInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(GroupInfoQuery param);

    /**
     * 根据GroupId查询对象
     */
    GroupInfo getGroupInfoByGroupId(String groupId);


    /**
     * 根据GroupId修改
     */
    Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId);


    /**
     * 根据GroupId删除
     */
    Integer deleteGroupInfoByGroupId(String groupId);

    /**
     * 创建群组
     * @param groupInfo 群组信息
     * @param avatarFile 头像文件
     * @param avatarCover 头像缩略图
     */
    void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;

    /**
     * 查询我创建的群聊
     * @param userId 当前用户id
     * @return 群聊列表
     */
    List<GroupInfo> loadMyGroup(String userId);

    /**
     * 获取群聊详情（含成员数量）并校验权限
     * @param userId 当前用户id
     * @param groupId 群聊id
     * @return 群聊详情
     */
    GroupInfo getGroupDetail(String userId, String groupId);

    /**
     * 查询群聊信息及群成员列表
     * @param userId 当前用户id
     * @param groupId 群聊id
     * @return 群聊信息VO
     */
    GroupInfoVO getGroupInfoForChat(String userId, String groupId);

    void dissolutionGroup(String userId, String groupId);

    void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);

    void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String contactIds, Integer opType);
}
