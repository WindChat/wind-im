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
package com.windchat.im.boot.config;

/**
 * 站点服务，相关配置字段
 *
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2018-01-24 19:49:19
 */
public interface ConfigKey {
    // 站点版本
    public String SITE_VERSION = "windchat.version";
    // 站点服务地址，提供用户和站点之间连接使用
    public String SITE_ADDRESS = "windchat.address";
    // 站点服务监听端口
    public String SITE_PORT = "windchat.port";
    // 站点启动的http服务地址，内部扩展功能访问使用
    public String PLUGIN_API_ADDRESS = "windchat.api.address";
    // http服务监听端口
    public String PLUGIN_API_PORT = "windchat.api.port";
    // 站点管理扩展地址&&端口
    public String SITE_ADMIN_ADDRESS = "windchat.admin.address";
    public String SITE_ADMIN_PORT = "windchat.admin.port";
    // 站点管理员
    public String SITE_ADMINISTRATORS = "windchat.administrators";
    // 站点管理员首次登陆站点，设置的邀请码
    public String SITE_ADMIN_UIC = "windchat.uic";
    // 存放站点图片，音频相关文件路径
    public String SITE_BASE_DIR = "windchat.baseDir";
    // 最大成员人数
    public String GROUP_MEMBERS_COUNT = "group.members.count";

}
