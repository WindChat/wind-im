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
package com.windchat.im.web.admin.service.impl;

import java.util.List;
import java.util.Set;

import com.windchat.im.web.admin.service.IGroupService;
import org.springframework.stereotype.Service;

import com.windchat.im.business.dao.UserGroupDao;
import com.windchat.im.business.impl.site.SiteConfig;
import com.windchat.im.storage.bean.GroupMemberBean;
import com.windchat.im.storage.bean.GroupProfileBean;
import com.windchat.im.storage.bean.SimpleGroupBean;

/**
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2018-04-17 18:59:24
 */
@Service("groupManageService")
public class GroupManageService implements IGroupService {

	@Override
	public GroupProfileBean getGroupProfile(String siteGroupId) {
		GroupProfileBean groupProfile = UserGroupDao.getInstance().getGroupProfile(siteGroupId);
		groupProfile.setDefaultState(isUserDefaultGroup(groupProfile.getGroupId()) ? 1 : 0);
		return groupProfile;
	}

	private boolean isUserDefaultGroup(String siteGroupId) {
		Set<String> defaultGroups = SiteConfig.getUserDefaultGroups();
		if (defaultGroups != null && defaultGroups.size() > 0) {
			return defaultGroups.contains(siteGroupId);
		}
		return false;
	}

	@Override
	public boolean updateGroupProfile(GroupProfileBean bean) {
		return UserGroupDao.getInstance().updateGroupProfile(bean);
	}

	@Override
	public List<SimpleGroupBean> getGroupList(int pageNum, int pageSize) {
		return UserGroupDao.getInstance().getGroupList(pageNum, pageSize);
	}

	@Override
	public List<GroupMemberBean> getGroupMembers(String siteGroupId, int pageNum, int pageSize) {
		return UserGroupDao.getInstance().getGroupMemberList(siteGroupId, pageNum, pageSize);
	}

	@Override
	public List<GroupMemberBean> getNonGroupMembers(String siteGroupId, int pageNum, int pageSize) {
		return UserGroupDao.getInstance().getNonGroupMemberList(siteGroupId, pageNum, pageSize);
	}

	@Override
	public boolean addGroupMembers(String siteGroupId, List<String> newMemberList) {
		return UserGroupDao.getInstance().addGroupMember(null, siteGroupId, newMemberList);
	}

	@Override
	public boolean removeGroupMembers(String siteGroupId, List<String> groupMemberList) {
		return UserGroupDao.getInstance().deleteGroupMember(siteGroupId, groupMemberList);
	}

	@Override
	public boolean dismissGroup(String siteGroupId) {
		return UserGroupDao.getInstance().deleteGroup(siteGroupId);
	}

}
