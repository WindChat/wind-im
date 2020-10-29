
[WindChat-风信](https://gitee.com/wind-chat/wind-im/blob/master/README.md)
====

<img src="https://images.gitee.com/uploads/images/2020/1030/003214_2ce45893_1566564.png" alt="WindChat.png" width=200>


[![License](https://img.shields.io/badge/license-apache2-blue.svg)](LICENSE)

**源码仓库**

> * **GitHub地址** : https://github.com/WindChat/wind-im.git
> * **Gitee地址** : https://gitee.com/wind-chat/wind-im.git



简介
----

WindChat 是一款开源免费私有IM聊天软件，由核心开发者SAM2O2O等维护，主要面向企业定制IM，开源、免费。

特性：

* 单聊、群聊（含文字、图片、语音等）
* 端到端的加密消息（服务端不可解密，服务器可通过配置关闭此特性）
* 匿名注册、实名注册，以及注册邀请码机制（只允许特定用户注册）
* 扩展机制
* 等


<p align="center">
  <img align="center" src="https://images.gitee.com/uploads/images/2019/1126/104318_e96d4636_1566564.jpeg" width="200"  /> &nbsp; <img align="center" src="https://images.gitee.com/uploads/images/2019/1126/104318_3d9b5edb_1566564.jpeg" width="200"  /> &nbsp; <img align="center" src="https://images.gitee.com/uploads/images/2019/1126/104318_3751606d_1566564.jpeg" width="200"  /> &nbsp;
</p>


一、快速体验
----

**1. 启动服务器**

    * git clone https://gitee.com/wind-chat/wind-im.git
    
    
WindChat 开始支持personal（个人版）与team版，默认状态下使用personal

支持的启动参数：`java -jar windchat-server.jar -h`

WindChat Personal版本 命令：

    * 版本升级：`java -jar windchat-server.jar -upgrade` ，此命令在服务与sqlite数据库版本不一致时执行，正常情况无需执行

    * 启动命令：`java -jar windchat-server.jar`
    
WindChat Team版本 命令：
    
    * 生成Team版本所需配置模版：`java -jar windchat-server.jar -team`
    
    * 修改配置文件: 上一步会生成 windchat-server.config 使用mysql数据库需在[windchat-server.config]配置文件中配置mysql参数：
                主库（数据库编码需要设置utf8mb4）：
                    windchat.mysql.host=localhost //数据库的地址
                    windchat.mysql.port=3306        //数据库端口
                    windchat.mysql.database=openzaly    //数据库名称
                    windchat.mysql.username=root        //mysql数据库访问用户
                    windchat.mysql.password=1234567890  //mysql数据库密码
                
                从库（如果需要使用主从模式，配置这里，不需要从库则不需要配置）数据库编码需要设置utf8mb4：
                    windchat.mysql.slave.host=localhost
                    windchat.mysql.slave.port=3306
                    windchat.mysql.slave.database=openzaly
                    windchat.mysql.slave.username=root
                    windchat.mysql.slave.password=1234567890
                
                其他mysql参数为使用mysql连接池的配置参数，如若涉及性能优化可开启配置项。
                
     * 迁移数据库命令：WindChat支持使用者把Personal版本的sqlite中的数据迁移到Team版本的mysql数据库
                     如果执行这一步需要在windchat-server.config配置文件中配置：
                        `windchat.sqlite.url=openzalyDB.sqlite3` 这里指定sqlite数据库文件的位置
                     
                     继续执行迁移命令：
                        `java -jar windchat-server.jar -migrate`
        
     * 启动命令：`java -jar windchat-server.jar`      
        

**2. 下载客户端**

Android  客户端：
> Android 端暂未开源，敬请期待

iOS  客户端：
> iOS 端暂未开源，敬请期待


**3. 访问站点**

> * 生成账号（手机账号与匿名均可）
> * 输入站点服务器
> * 首次登陆为管理员，邀请码：000000
> * 别的用户登陆后可以互加好友，开始聊天。

* 匿名账号，账号保存在设备本地，用户不会填写手机信息，任何地方都获取不到。

> **站点注册方式默认为匿名，进入站点后，请根据情况第一时间修改为 实名 或者 开启邀请码，防止恶意用户进入**


二、源码编译安装
----

- 本地安装Java1.8+
- 需要本地有mvn，直接使用mvn编译即可。



三、扩展开发
----

WindChat 具有灵活、强大的扩展机制 `(“管理平台” 就是一个扩展)`。通过嵌入WEB页面，与后端的扩展API进行交互， 可以很轻松的构建丰富的业务功能，如：

* 附近交友
* 线上打卡
* 店铺点评
* 在线游戏
* 等等等等

你的聊天服务器，将摇身一变，成为一个强大的社交软件平台。

> 扩展机制处于技术预览阶段，如果你希望在自己的业务中开发自己的扩展，可以联系我们（ mail: an.guoyue254@gmail.com ），我们将免费提供文档与技术答疑。



四、技术贡献者
----

> 以加入时间排序

* sisishiliu（Akaxin/DuckChat创始人）
* SAM2O2O
* childeYin


**向我们提问**

>官方微信：<img src="https://images.gitee.com/uploads/images/2020/1030/003752_de901b73_1566564.jpeg" width=200>

> 联系邮件：an.guoyue254@gmail.com


**打赏**

开源本不易，如果你觉得对你有帮助，请捐赠以表示支持，感谢！

<img src="https://images.gitee.com/uploads/images/2020/1030/003539_1f1b9765_1566564.jpeg" width=200>

<img src="https://images.gitee.com/uploads/images/2020/0511/223222_9ac22d03_1566564.png" width=200>
