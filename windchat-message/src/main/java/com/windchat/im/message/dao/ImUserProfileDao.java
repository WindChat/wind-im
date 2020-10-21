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
package com.windchat.im.message.dao;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.windchat.im.storage.api.IUserDeviceDao;
import com.windchat.im.storage.api.IUserProfileDao;
import com.windchat.im.storage.bean.SimpleUserBean;
import com.windchat.im.storage.service.DeviceDaoService;
import com.windchat.im.storage.service.UserProfileDaoService;

/**
 * 
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2018-01-26 18:26:24
 */
public class ImUserProfileDao {
	private static final Logger logger = LoggerFactory.getLogger(ImUserProfileDao.class);
	private IUserProfileDao userProfileDao = new UserProfileDaoService();
	private IUserDeviceDao deviceProfileDao = new DeviceDaoService();

	private ImUserProfileDao() {
	}

	static class SingletonHolder {
		private static ImUserProfileDao instance = new ImUserProfileDao();
	}

	public static ImUserProfileDao getInstance() {
		return SingletonHolder.instance;
	}

	public SimpleUserBean getSimpleUserProfile(String siteUserId) {
		return getSimpleUserProfile(siteUserId, false);
	}

	public SimpleUserBean getSimpleUserProfile(String siteUserId, boolean isMaster) {
		try {
			return userProfileDao.getSimpleProfileById(siteUserId, isMaster);
		} catch (SQLException e) {
			logger.error("get simple user profile by siteUserId error", e);
		}
		return null;
	}

	public List<String> getUserToken(String siteUserId) {
		try {
			return deviceProfileDao.getUserTokens(siteUserId);
		} catch (SQLException e) {
			logger.error("get user token error.", e);
		}
		return null;
	}

	public String getGlobalUserId(String siteUserId) {
		try {
			return userProfileDao.getGlobalUserIdBySiteUserId(siteUserId);
		} catch (SQLException e) {
			logger.error("get user globalUserId error.", e);
		}
		return null;
	}

	public boolean isMute(String siteUserId) throws SQLException {
		try {
			return userProfileDao.isMute(siteUserId);
		} catch (Exception e) {
			logger.error("get user mute error", e);
		}
		return true;
	}
}
