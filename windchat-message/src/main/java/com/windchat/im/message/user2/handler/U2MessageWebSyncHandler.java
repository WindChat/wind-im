/**
 * Copyright 2018-2028 Akaxin Group
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.windchat.im.message.user2.handler;

import com.akaxin.proto.core.CoreProto;
import com.akaxin.proto.site.ImCtsMessageProto;
import com.windchat.common.command.Command;
import com.windchat.common.crypto.AESCrypto;
import com.windchat.common.http.ZalyHttpClient;
import com.windchat.common.logs.LogUtils;
import com.windchat.common.utils.GsonUtils;
import com.windchat.im.message.bean.WebBean;
import com.windchat.im.storage.api.IMessageDao;
import com.windchat.im.storage.bean.U2MessageBean;
import com.windchat.im.storage.service.MessageDaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * sync message to web
 *
 * @author an.guoyue254@gmail.com
 * @since 2020-10-12 22:45:20
 */
public class U2MessageWebSyncHandler extends AbstractU2Handler<Command> {
    private static final Logger logger = LoggerFactory.getLogger(U2MessageWebSyncHandler.class);

    private static final int programId = 100;
    private static final String programSecret = "gXDayjtSHtTk33Tfo4ILruYmYblO7nJe";

    public Boolean handle(Command command) {
        try {

            switch (command.getMsgType()) {
                case CoreProto.MsgType.TEXT_VALUE: {
                    ImCtsMessageProto.ImCtsMessageRequest request = ImCtsMessageProto.ImCtsMessageRequest
                            .parseFrom(command.getParams());
                    final String siteUserId = command.getSiteUserId();
                    final String proxySiteUserId = request.getText().getSiteUserId();
                    final String siteFriendId = request.getText().getSiteFriendId();
                    final String msgId = request.getText().getMsgId();
                    final String textBody = request.getText().getText().toString(StandardCharsets.UTF_8);

                    // 构建Json格式消息体
                    String action = "duckchat.message.send";
                    Map<String, Object> data = new HashMap<String, Object>(3) {{
                        put("action", action);
                        put("body", new HashMap<String, Object>(2) {{
                            put("@type", "type.googleapis.com/plugin.DuckChatMessageSendRequest");
                            put("message", new HashMap<String, Object>(6) {{
                                put("msgId", msgId);
                                put("fromUserId", siteUserId);
                                put("toUserId", siteFriendId);
                                put("type", "MessageText");
                                put("roomType", "MessageRoomU2");
                                put("text", new HashMap<String, Object>(1) {{
                                    put("body", textBody);
                                }});
                            }});
                        }});
                        put("timeServer", System.currentTimeMillis());
                    }};
                    // 构建HTTP URL
                    String requestUrl = "http://localhost:8000//?action=" + action + "&body_format=json&miniProgramId=" + programId;
                    // AES 加密

                    byte[] encrptData = AESCrypto.encrypt(programSecret.getBytes(), GsonUtils.toJson(data).getBytes());

                    // Http Post请求
                    ZalyHttpClient.getInstance().postBytes(requestUrl, encrptData);
                }
                break;
                default:
                    break;
            }
            return true;
        } catch (Exception e) {
            LogUtils.requestErrorLog(logger, command, this.getClass(), e);
        }

        return false;
    }

}
