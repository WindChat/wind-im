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
package com.windchat.im.business.service;

import com.windchat.im.business.bean.ApiActions;
import com.windchat.im.business.impl.IRequestService;
import com.windchat.im.business.impl.tai.ApiDeviceService;
import com.windchat.im.business.impl.tai.ApiFileService;
import com.windchat.im.business.impl.tai.ApiFriendService;
import com.windchat.im.business.impl.tai.ApiGroupService;
import com.windchat.im.business.impl.tai.ApiPluginService;
import com.windchat.im.business.impl.tai.ApiSecretChatService;
import com.windchat.im.business.impl.tai.ApiSiteService;
import com.windchat.im.business.impl.tai.ApiUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * API业务请求，分发工厂
 *
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2017.10.24 18:25:31
 */
public class ApiServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(ApiServiceFactory.class);

    private static Map<String, IRequestService> serviceMap = new LinkedHashMap<String, IRequestService>();
    private static List<IRequestService> serviceList = new LinkedList<IRequestService>();

    static {
        serviceList.add(new ApiSiteService());
        serviceList.add(new ApiUserService());
        serviceList.add(new ApiFriendService());
        serviceList.add(new ApiGroupService());
        serviceList.add(new ApiSecretChatService());
        serviceList.add(new ApiFileService());
        serviceList.add(new ApiDeviceService());
        serviceList.add(new ApiPluginService());

        init();
    }

    private static void init() {
        for (IRequestService requestService : serviceList) {
            ApiActions serviceApiAction = requestService.getClass().getDeclaredAnnotation(ApiActions.class);
            if (serviceApiAction != null) {
                serviceMap.put(serviceApiAction.action(), requestService);
            }
        }
    }

    public static IRequestService getService(String serviceAction) {
        return serviceMap.get(serviceAction);
    }

}
