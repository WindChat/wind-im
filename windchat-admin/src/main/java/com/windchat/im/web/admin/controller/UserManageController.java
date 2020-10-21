/**
 * Copyright 2018-2028 WindChat Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.windchat.im.web.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.windchat.im.web.admin.exception.ManagerException;
import com.windchat.im.web.admin.exception.UserPermissionException;
import com.windchat.im.web.admin.service.IConfigService;
import com.windchat.im.web.admin.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.windchat.common.utils.StringHelper;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.core.UserProto.UserStatus;
import com.windchat.im.storage.bean.SimpleUserBean;
import com.windchat.im.storage.bean.UserProfileBean;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 后台管理-用户管理
 */
@Controller
@RequestMapping("user")
public class UserManageController extends AbstractController {
	private static final Logger logger = LoggerFactory.getLogger(UserManageController.class);

	@Resource(name = "userManageService")
	private IUserService userService;
	@Autowired
	private IConfigService configService;

	// 进入用户管理首页
	@RequestMapping("/index")
	public ModelAndView toIndex(@RequestBody byte[] bodyParam) {
		ModelAndView modelAndView = new ModelAndView("user/index");
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			if (!isManager(getRequestSiteUserId(pluginPackage))) {
				throw new UserPermissionException("Current user is not a manager");
			}
			List<String> defaultFriendList = configService.getUserDefaultFriendList();
			List<UserProfileBean> userProfileBeans = new ArrayList<>();
			modelAndView.addObject("userDefaultSize", "0");
			if (defaultFriendList != null && defaultFriendList.size() > 0) {
				for (String siteUserId : defaultFriendList) {
					UserProfileBean userProfile = userService.getUserProfile(siteUserId);
					userProfileBeans.add(userProfile);
				}
				modelAndView.addObject("userList", userProfileBeans);
				modelAndView.addObject("userDefaultSize", String.valueOf(defaultFriendList.size()));
			}
			return modelAndView;
		} catch (InvalidProtocolBufferException e) {
			logger.error("to User Manage error", e);
		} catch (UserPermissionException e) {
			logger.error("to User Manage error : " + e.getMessage());
		}
		return new ModelAndView("error");
	}

	/**
	 * 设置官方用户，默认为所有新用户的好友
	 * 
	 * @param bodyParam
	 * @return
	 */
	@RequestMapping("/setUserDefaultFriend")
	@ResponseBody
	public String setUserDefaultFriend(@RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			if (!isManager(getRequestSiteUserId(pluginPackage))) {
				throw new UserPermissionException("Current user is not a manager");
			}
			Map<String, String> reqMap = getRequestDataMap(pluginPackage);
			String friendSiteUserId = reqMap.get("siteUserId");
			boolean result = configService.setUserDefaultFriends(friendSiteUserId);
			if (result) {
				return SUCCESS;
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("set user as default friend error", e);
		} catch (UserPermissionException e) {
			logger.error("set user as default friend error", e);
			return NO_PERMISSION;
		} catch (Exception e) {
			logger.error("set user as default friend error", e);
		}
		return ERROR;
	}

	@RequestMapping("/deleteUserDefaultFriend")
	@ResponseBody
	public String deleteUserDefaultFriend(@RequestBody byte[] bodyParam) {
		try {
			logger.info("siteUserId={} deleteUserDefaultFriend userId={} ");
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);
			if (!isManager(siteUserId)) {
				throw new UserPermissionException("Current user is not a manager");
			}
			Map<String, String> reqMap = getRequestDataMap(pluginPackage);
			String friendSiteUserId = reqMap.get("siteUserId");
			boolean flag = configService.deleteUserDefaultFriends(friendSiteUserId);

			logger.info("siteUserId={} deleteUserDefaultFriend userId={} ", siteUserId, friendSiteUserId);
			if (flag) {
				return SUCCESS;
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("delete user as default friend error ", e);
		} catch (UserPermissionException e) {
			logger.error("delete user as default friend error : ", e);
			return NO_PERMISSION;
		} catch (Exception e) {
			logger.error("delete user as default friend error", e);
		}
		return ERROR;
	}

	/**
	 * 设置用户为管理员
	 * 
	 * @param bodyParam
	 * @return
	 */
	@RequestMapping("/setSiteManager")
	@ResponseBody
	public String setSiteManager(@RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			if (!isAdmin(getRequestSiteUserId(pluginPackage))) {
				throw new UserPermissionException("Current user is not admin");
			}
			Map<String, String> reqMap = getRequestDataMap(pluginPackage);
			String managerUserId = reqMap.get("siteUserId");
			boolean result = configService.addUserManager(managerUserId);
			if (result) {
				return SUCCESS;
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("set site manager error : ", e.getMessage());
		} catch (UserPermissionException e) {
			logger.error("set site manager error : " + e.getMessage());
			return NO_PERMISSION;
		}
		return ERROR;
	}

	/**
	 * 删除用户管理员身份
	 * 
	 * @param bodyParam
	 * @return
	 */
	@RequestMapping("/deleteSiteManager")
	@ResponseBody
	public String deleteSiteManager(@RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			if (!isAdmin(getRequestSiteUserId(pluginPackage))) {
				throw new UserPermissionException("Current user is not admin");
			}
			Map<String, String> reqMap = getRequestDataMap(pluginPackage);
			String managerUserId = reqMap.get("siteUserId");
			boolean result = configService.deleteUserManager(managerUserId);
			if (result) {
				return SUCCESS;
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("delete site manager error : ", e.getMessage());
		} catch (UserPermissionException e) {
			logger.error("delete site manager error : " + e.getMessage());
			return NO_PERMISSION;
		}
		return ERROR;
	}

	// 用户个人资料展示界面，此界面编辑用户资料，并执行更新
	@RequestMapping("/profile")
	public ModelAndView toUserProfile(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		ModelAndView modelAndView = new ModelAndView("user/profile");

		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String currentUserId = getRequestSiteUserId(pluginPackage);

			if (!isManager(currentUserId)) {
				throw new UserPermissionException("Current user is not a manager");
			}

			Map<String, String> reqMap = getRequestDataMap(pluginPackage);
			String siteUserId = reqMap.get("site_user_id");
			UserProfileBean bean = userService.getUserProfile(siteUserId);

			modelAndView.addObject("siteUserId", bean.getSiteUserId());
			modelAndView.addObject("siteLoginId", bean.getSiteLoginId());
			modelAndView.addObject("userName", bean.getUserName());
			modelAndView.addObject("userPhoto", bean.getUserPhoto());
			modelAndView.addObject("userIntroduce", bean.getSelfIntroduce());
			modelAndView.addObject("userStatus", bean.getUserStatus());
			modelAndView.addObject("regTime", bean.getRegisterTime());
			modelAndView.addObject("defaultState", bean.getDefaultState());
			boolean isManager = isManager(siteUserId);
			modelAndView.addObject("managerState", isManager ? 1 : 0);
			return modelAndView;
		} catch (InvalidProtocolBufferException e) {
			logger.error(StringHelper.format("siteUserId={} get user profile error"), e);
		} catch (UserPermissionException e) {
			logger.error("get user profile error : " + e.getMessage());
		}
		return new ModelAndView("error");
	}

	@RequestMapping("/refresh")
	@ResponseBody
	public Map<String, Object> refreshPage(@RequestBody byte[] bodyParam) {
		HashMap<String, Object> dataMap = new HashMap<>();

		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			if (!isManager(getRequestSiteUserId(pluginPackage))) {
				throw new UserPermissionException("Current user is not a manager");
			}
			List<String> userDefault = configService.getUserDefaultFriendList();
			if (userDefault == null || userDefault.size() <= 0) {
				dataMap.put("size", 0);
				return dataMap;
			}
			ArrayList<Map<String, Object>> data = new ArrayList<>();
			for (String s : userDefault) {
				UserProfileBean bean = userService.getUserProfile(s);
				HashMap<String, Object> userMap = new HashMap<>();
				userMap.put("siteUserId", bean.getSiteUserId());
				userMap.put("userName", bean.getUserName());
				userMap.put("userPhoto", bean.getUserPhoto());
				userMap.put("userStatus", bean.getUserStatus());
				data.add(userMap);
			}
			dataMap.put("size", data.size());
			dataMap.put("data", data);
		} catch (InvalidProtocolBufferException e) {
			logger.error("refresh user list error", e);
		} catch (UserPermissionException e) {
			logger.error("refresh user list error : " + e.getMessage());
		}

		return dataMap;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/userList")
	@ResponseBody
	public Map<String, Object> getSiteUsers(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		Map<String, Object> results = new HashMap<String, Object>();
		boolean nodata = true;

		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);

			if (!isManager(siteUserId)) {
				throw new UserPermissionException("Current user is not a manager");
			}
			Map<String, String> dataMap = getRequestDataMap(pluginPackage);
			int pageNum = Integer.valueOf(dataMap.get("page"));

			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			List<SimpleUserBean> userList = userService.getUserList(pageNum, PAGE_SIZE);
			if (userList != null && userList.size() > 0) {
				nodata = false;

				List<String> defaultFriendList = configService.getUserDefaultFriendList();
				if (defaultFriendList != null && defaultFriendList.size() > 0) {
					for (SimpleUserBean bean : userList) {
						boolean contains = defaultFriendList.contains(bean.getUserId());
						Map<String, Object> userMap = new HashMap<String, Object>();
						if (contains) {
							continue;
						}
						userMap.put("siteUserId", bean.getUserId());
						userMap.put("siteLoginId", bean.getSiteLoginId());
						userMap.put("userName", bean.getUserName());
						userMap.put("userPhoto", bean.getUserPhoto());
						userMap.put("userStatus", bean.getUserStatus());

						data.add(userMap);
					}
				} else {
					for (SimpleUserBean bean : userList) {
						Map<String, Object> userMap = new HashMap<String, Object>();
						userMap.put("siteUserId", bean.getUserId());
						userMap.put("siteLoginId", bean.getSiteLoginId());
						userMap.put("userName", bean.getUserName());
						userMap.put("userPhoto", bean.getUserPhoto());
						userMap.put("userStatus", bean.getUserStatus());

						data.add(userMap);
					}
				}

			}

			results.put("userData", data);

		} catch (InvalidProtocolBufferException e) {
			logger.error("get site user list error", e);
		} catch (UserPermissionException e) {
			logger.error("get site user list error : " + e.getMessage());
		}
		results.put("loading", nodata);
		return results;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateProfile")
	@ResponseBody
	public String updateProfile(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);

			// 权限控制
			if (!isManager(siteUserId)) {
				throw new UserPermissionException("Current user is not a manager");
			}

			// 参数校验
			Map<String, String> reqMap = getRequestDataMap(pluginPackage);
			String toUserId = reqMap.get("siteUserId");
			String siteLoginId = reqMap.get("siteLoginId");
			String userName = trim(reqMap.get("userName"));

			if (StringUtils.isEmpty(toUserId)) {
				throw new UserPermissionException("update user's siteUserId is null");
			}

			if (StringUtils.isNotEmpty(userName) && userName.length() > 16) {
				throw new ManagerException("userName={} length={} longer than [1,16] error");
			}

			if (StringUtils.isNotEmpty(siteLoginId)) {
				Matcher match = Pattern.compile("^[A-Za-z][A-Za-z0-9]{0,15}$").matcher(siteLoginId);
				if (!match.matches()) {
					throw new ManagerException("user's siteLoginId formatting error");
				}
			}

			UserProfileBean originalBean = userService.getUserProfile(toUserId);
			if (originalBean == null || StringUtils.isEmpty(originalBean.getSiteUserId())) {
				throw new UserPermissionException("update user's profile is null from DB");
			}

			UserProfileBean bean = new UserProfileBean();
			bean.setSiteUserId(toUserId);
			bean.setSiteLoginId(siteLoginId);
			bean.setUserName(userName);
			bean.setUserPhoto(trim(reqMap.get("userPhoto")));
			bean.setSelfIntroduce(trim(reqMap.get("userIntroduce")));

			boolean result = userService.updateProfile(bean);
			logger.info(
					"siteUserId={} update user={} \n\t userName/nickName:{} -> {} \n\t siteLoginId:{} -> {} \n\t userPhoto:{} -> {}",
					siteUserId, toUserId, originalBean.getUserName(), bean.getUserName(), originalBean.getSiteLoginId(),
					bean.getSiteLoginId(), originalBean.getUserPhoto(), bean.getUserPhoto());

			return result ? SUCCESS : ERROR;

		} catch (InvalidProtocolBufferException e) {
			logger.error("update profile error", e);
		} catch (UserPermissionException e) {
			logger.error("update profile error : " + e.getMessage());
			return NO_PERMISSION;
		} catch (ManagerException e) {
			logger.error("update profile error:" + e.getMessage());
		}

		return ERROR;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/sealup")
	@ResponseBody
	public String sealup(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);

			if (!isManager(siteUserId)) {
				throw new UserPermissionException("Current user is not a manager");
			}
			Map<String, String> reqMap = getRequestDataMap(pluginPackage);
			String reqUserId = reqMap.get("site_user_id");
			String reqStatus = reqMap.get("user_status");
			int status = UserStatus.NORMAL_VALUE;
			if ("1".equals(reqStatus)) {
				status = UserStatus.SEALUP_VALUE;
			}

			logger.info("siteUserId={} sealup siteUserId={} to status={}", siteUserId, reqUserId, status);
			if (userService.sealUpUser(reqUserId, status)) {
				return SUCCESS;
			}
		} catch (InvalidProtocolBufferException e) {
			logger.error("sealup user error", e);
		} catch (UserPermissionException e) {
			logger.error("sealup user error : ", e);
			return NO_PERMISSION;
		} catch (Exception e) {
			logger.error("sealup user error : ", e);
		}
		return ERROR;
	}

	@RequestMapping(method = RequestMethod.POST, value = "delUser")
	@ResponseBody
	public String deleteUser(HttpServletRequest request, @RequestBody byte[] bodyParam) {
		try {
			PluginProto.ProxyPluginPackage pluginPackage = PluginProto.ProxyPluginPackage.parseFrom(bodyParam);
			String siteUserId = getRequestSiteUserId(pluginPackage);

			if (isManager(siteUserId)) {
				Map<String, String> reqMap = getRequestDataMap(pluginPackage);
				String delUserID = reqMap.get("siteUserId");
				if (userService.delUser(delUserID)) {
					return SUCCESS;
				}
			} else {
				return NO_PERMISSION;
			}
		} catch (Exception e) {
			logger.error("del User error", e);
		}
		return ERROR;
	}

}
