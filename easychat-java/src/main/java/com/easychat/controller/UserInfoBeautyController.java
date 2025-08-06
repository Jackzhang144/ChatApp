package com.easychat.controller;

import java.util.List;

import com.easychat.entity.query.UserInfoBeautyQuery;
import com.easychat.entity.po.UserInfoBeauty;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.service.UserInfoBeautyService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 靓号表 Controller
 */
@RestController("userInfoBeautyController")
@RequestMapping("/userInfoBeauty")
public class UserInfoBeautyController extends ABaseController{

	@Resource
	private UserInfoBeautyService userInfoBeautyService;
	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList(UserInfoBeautyQuery query){
		return getSuccessResponseVO(userInfoBeautyService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(UserInfoBeauty bean) {
		userInfoBeautyService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserInfoBeauty> listBean) {
		userInfoBeautyService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserInfoBeauty> listBean) {
		userInfoBeautyService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Id查询对象
	 */
	@RequestMapping("/getUserInfoBeautyById")
	public ResponseVO getUserInfoBeautyById(Integer id) {
		return getSuccessResponseVO(userInfoBeautyService.getUserInfoBeautyById(id));
	}

	/**
	 * 根据Id修改对象
	 */
	@RequestMapping("/updateUserInfoBeautyById")
	public ResponseVO updateUserInfoBeautyById(UserInfoBeauty bean,Integer id) {
		userInfoBeautyService.updateUserInfoBeautyById(bean,id);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Id删除
	 */
	@RequestMapping("/deleteUserInfoBeautyById")
	public ResponseVO deleteUserInfoBeautyById(Integer id) {
		userInfoBeautyService.deleteUserInfoBeautyById(id);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email查询对象
	 */
	@RequestMapping("/getUserInfoBeautyByEmail")
	public ResponseVO getUserInfoBeautyByEmail(String email) {
		return getSuccessResponseVO(userInfoBeautyService.getUserInfoBeautyByEmail(email));
	}

	/**
	 * 根据Email修改对象
	 */
	@RequestMapping("/updateUserInfoBeautyByEmail")
	public ResponseVO updateUserInfoBeautyByEmail(UserInfoBeauty bean,String email) {
		userInfoBeautyService.updateUserInfoBeautyByEmail(bean,email);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email删除
	 */
	@RequestMapping("/deleteUserInfoBeautyByEmail")
	public ResponseVO deleteUserInfoBeautyByEmail(String email) {
		userInfoBeautyService.deleteUserInfoBeautyByEmail(email);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId查询对象
	 */
	@RequestMapping("/getUserInfoBeautyByUserId")
	public ResponseVO getUserInfoBeautyByUserId(String userId) {
		return getSuccessResponseVO(userInfoBeautyService.getUserInfoBeautyByUserId(userId));
	}

	/**
	 * 根据UserId修改对象
	 */
	@RequestMapping("/updateUserInfoBeautyByUserId")
	public ResponseVO updateUserInfoBeautyByUserId(UserInfoBeauty bean,String userId) {
		userInfoBeautyService.updateUserInfoBeautyByUserId(bean,userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId删除
	 */
	@RequestMapping("/deleteUserInfoBeautyByUserId")
	public ResponseVO deleteUserInfoBeautyByUserId(String userId) {
		userInfoBeautyService.deleteUserInfoBeautyByUserId(userId);
		return getSuccessResponseVO(null);
	}
}