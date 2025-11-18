package com.easychat.service.impl;

import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.enums.*;
import com.easychat.entity.po.UserContact;
import com.easychat.entity.po.UserContactApply;
import com.easychat.entity.query.SimplePage;
import com.easychat.entity.query.UserContactApplyQuery;
import com.easychat.entity.query.UserContactQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.exception.BusinessException;
import com.easychat.mappers.UserContactApplyMapper;
import com.easychat.mappers.UserContactMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.service.UserContactApplyService;
import com.easychat.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 联系人申请 业务接口实现
 */
@Service("userContactApplyService")
public class UserContactApplyServiceImpl implements UserContactApplyService {

    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContactApply> findListByParam(UserContactApplyQuery param) {
        return this.userContactApplyMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserContactApplyQuery param) {
        return this.userContactApplyMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserContactApply> findListByPage(UserContactApplyQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserContactApply> list = this.findListByParam(param);
        PaginationResultVO<UserContactApply> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContactApply bean) {
        return this.userContactApplyMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContactApply> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContactApply> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactApplyMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserContactApply bean, UserContactApplyQuery param) {
        StringTools.checkParam(param);
        return this.userContactApplyMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserContactApplyQuery param) {
        StringTools.checkParam(param);
        return this.userContactApplyMapper.deleteByParam(param);
    }

    /**
     * 根据ApplyId获取对象
     */
    @Override
    public UserContactApply getUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.selectByApplyId(applyId);
    }

    /**
     * 根据ApplyId修改
     */
    @Override
    public Integer updateUserContactApplyByApplyId(UserContactApply bean, Integer applyId) {
        return this.userContactApplyMapper.updateByApplyId(bean, applyId);
    }

    /**
     * 根据ApplyId删除
     */
    @Override
    public Integer deleteUserContactApplyByApplyId(Integer applyId) {
        return this.userContactApplyMapper.deleteByApplyId(applyId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId获取对象
     */
    @Override
    public UserContactApply getUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId修改
     */
    @Override
    public Integer updateUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(UserContactApply bean, String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.updateByApplyUserIdAndReceiveUserIdAndContactId(bean, applyUserId, receiveUserId, contactId);
    }

    /**
     * 根据ApplyUserIdAndReceiveUserIdAndContactId删除
     */
    @Override
    public Integer deleteUserContactApplyByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserId, String contactId) {
        return this.userContactApplyMapper.deleteByApplyUserIdAndReceiveUserIdAndContactId(applyUserId, receiveUserId, contactId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(String userId, Integer applyId, Integer status) {

        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        if (statusEnum == null || statusEnum == UserContactApplyStatusEnum.INIT) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //经典防呆防坏设计，需要防止通过接口调用，做一些通过前端做不了的事情
        UserContactApply applyInfo = this.userContactApplyMapper.selectByApplyId(applyId);
        if (null == applyInfo || !userId.equals(applyInfo.getReceiveUserId())) {
            // 判断调接口的是否为申请接收者
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserContactApply updateInfo = new UserContactApply();
        updateInfo.setStatus(status);
        updateInfo.setLastApplyTime(System.currentTimeMillis());

        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setApplyId(applyId);
        applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());

        Integer count = this.userContactApplyMapper.updateByParam(updateInfo, applyQuery);

        if (count == 0) {
            // 只能从 0 更新到别的数字，如果更新失败则代表申请已被处理过
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 申请通过
        if (UserContactApplyStatusEnum.PASS.getStatus().equals(status)){
            addContact(applyInfo.getApplyUserId(), applyInfo.getReceiveUserId(), applyInfo.getContactId(), applyInfo.getContactType(), applyInfo.getApplyInfo());
            return;
        }

        // 被拉黑
        if (UserContactApplyStatusEnum.BLACKLIST.getStatus().equals(status)){
            Date curDate = new Date();
            UserContact userContact = new UserContact();
            userContact.setUserId(applyInfo.getApplyUserId());
            userContact.setContactId(applyInfo.getContactId());
            userContact.setContactType(applyInfo.getContactType());
            userContact.setCreateTime(curDate);
            userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
            userContact.setLastUpdateTime(curDate);
            // 如果以前申请过，那就update，如果没有，那就insert
            this.userContactMapper.insertOrUpdate(userContact);
        }
    }

    @Override
    public PaginationResultVO<UserContactApply> loadApplyPage(String receiveUserId, Integer pageNo) {
        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setOrderBy("last_apply_time desc");
        applyQuery.setReceiveUserId(receiveUserId);
        applyQuery.setPageNo(pageNo);
        applyQuery.setPageSize(PageSize.SIZE15.getSize());
        applyQuery.setQueryContactInfo(true);
        return this.findListByPage(applyQuery);
    }

    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        // 群聊人数
        if (contactType.equals(UserContactTypeEnum.GROUP.getType())){
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = this.userContactMapper.selectCount(userContactQuery);
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupMemberCount()){
                throw new BusinessException("成员已满，无法加入");
            }
            Date curDate = new Date();
            // 同意，双方添加好友
            List<UserContact> contactList = new ArrayList<>();
            // 申请人添加对方
            UserContact userContact = new UserContact();
            userContact.setUserId(applyUserId);
            userContact.setContactId(contactId);
            userContact.setContactType(contactType);
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            contactList.add(userContact);
            // 如果是申请好友，接收人添加申请人，群组不用添加对方为好友
            if (contactType.equals(UserContactTypeEnum.USER.getType())){
                userContact = new UserContact();
                userContact.setUserId(receiveUserId);
                userContact.setContactId(applyUserId);
                userContact.setContactType(contactType);
                userContact.setCreateTime(curDate);
                userContact.setLastUpdateTime(curDate);
                userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
                contactList.add(userContact);
            }
            // 批量插入
            this.userContactMapper.insertBatch(contactList);
            // TODO 如果是好友，接收人也添加申请人为好友，添加缓存

            // TODO 创建会话
        }
    }
}
