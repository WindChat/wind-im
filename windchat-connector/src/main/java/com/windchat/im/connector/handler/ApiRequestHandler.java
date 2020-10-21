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
package com.windchat.im.connector.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.windchat.common.channel.ChannelSession;
import com.windchat.common.command.Command;
import com.windchat.common.command.CommandResponse;
import com.windchat.common.command.RedisCommand;
import com.windchat.common.constant.CommandConst;
import com.windchat.common.constant.ErrorCode2;
import com.windchat.common.constant.RequestAction;
import com.windchat.common.utils.StringHelper;
import com.akaxin.proto.core.CoreProto;
import com.windchat.im.business.service.ApiRequestService;
import com.windchat.im.connector.constant.AkxProject;
import com.windchat.im.storage.api.IUserSessionDao;
import com.windchat.im.storage.bean.SimpleAuthBean;
import com.windchat.im.storage.service.UserSessionDaoService;
import com.google.protobuf.ByteString;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 使用TCP处理API请求,TCP代处理HTTP请求
 * 
 * @author Sam
 * @since 2017.10.19
 *
 * @param <Command>
 */
public class ApiRequestHandler extends AbstractCommonHandler<Command, CommandResponse> {
	private static final Logger logger = LoggerFactory.getLogger(ApiRequestHandler.class);

	public CommandResponse handle(Command command) {
		try {
			ChannelSession channelSession = command.getChannelSession();
			if (channelSession == null || channelSession.getChannel() == null) {
				logger.error("{} client={} api request handler error.channelSession={}", AkxProject.PLN,
						command.getClientIp(), channelSession);
				// NULL，则不需要再次关闭channel
				return null;
			}

			switch (RequestAction.getAction(command.getAction())) {
			case API_SITE_CONFIG:
			case API_SITE_REGISTER:
			case API_SITE_LOGIN:
				break;
			default: {
				Map<Integer, String> header = command.getHeader();
				String sessionId = header.get(CoreProto.HeaderKey.CLIENT_SOCKET_SITE_SESSION_ID_VALUE);
				// logger.debug("{} client={} api request sessionId={}", AkxProject.PLN,
				// command.getClientIp(), sessionId);

				if (!StringUtils.isNotEmpty(sessionId)) {
					this.tellClientSessionError(channelSession.getChannel());
					logger.error("{} client={} api request with sessionId is NULL", AkxProject.PLN,
							command.getClientIp());
					return customResponse(ErrorCode2.ERROR_SESSION);
				}

				IUserSessionDao sessionDao = new UserSessionDaoService();
				SimpleAuthBean authBean = sessionDao.getUserSession(sessionId);
				// logger.debug("{} client={} api session auth result {}", authBean.toString());

				if (authBean == null || StringUtils.isAnyEmpty(authBean.getSiteUserId(), authBean.getDeviceId())) {
					this.tellClientSessionError(channelSession.getChannel());
					logger.error("{} client={} api session auth fail.authBean={}", AkxProject.PLN,
							command.getClientIp(), authBean);
					return customResponse(ErrorCode2.ERROR_SESSION);
				}

				command.setSiteUserId(authBean.getSiteUserId());
				command.setDeviceId(authBean.getDeviceId());
			}
			}
			// 执行业务操作
			return this.doApiRequest(channelSession.getChannel(), command);
		} catch (Exception e) {
			logger.error(StringHelper.format("{} client={} api request error.", AkxProject.PLN, command.getClientIp()),
					e);
		}

		return customResponse(ErrorCode2.ERROR);
	}

	private CommandResponse doApiRequest(final Channel channel, Command command) {
		// response
		CoreProto.TransportPackageData.Builder packageBuilder = CoreProto.TransportPackageData.newBuilder();

		CommandResponse comamndResponse = new ApiRequestService().process(command);

		// 1.header
		Map<Integer, String> header = new HashMap<Integer, String>();
		// 站点业务版本（proto版本）
		header.put(CoreProto.HeaderKey.SITE_SERVER_VERSION_VALUE, CommandConst.SITE_VERSION);
		packageBuilder.putAllHeader(header);

		// 2.errCode
		int protoVersion = command.getProtoVersion();
		CoreProto.ErrorInfo.Builder errBuilder = CoreProto.ErrorInfo.newBuilder();
		// 兼容 “error.alert”
		if (protoVersion < 5 && "error.alert".equals(comamndResponse.getErrCode())) {
			errBuilder.setCode("error.alter");
		} else {
			errBuilder.setCode(comamndResponse.getErrCode());
		}

		if (StringUtils.isNotEmpty(comamndResponse.getErrInfo())) {
			errBuilder.setInfo(comamndResponse.getErrInfo());
		}
		packageBuilder.setErr(errBuilder.build());

		// 3.data
		if (comamndResponse.getParams() != null) {
			packageBuilder.setData(ByteString.copyFrom(comamndResponse.getParams())).build();
		}
		// 协议版本 CommandConst.PROTOCOL_VERSION=1.0
		String protocolVersion = CommandConst.PROTOCOL_VERSION;
		String action = comamndResponse.getAction() == null ? CommandConst.ACTION_RES : comamndResponse.getAction();
		channel.writeAndFlush(
				new RedisCommand().add(protocolVersion).add(action).add(packageBuilder.build().toByteArray()))
				.addListener(new GenericFutureListener<Future<? super Void>>() {

					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						channel.close();
					}
				});
		// future.await(timeoutMillis);
		return comamndResponse;
	}

	private void tellClientSessionError(final Channel channel) {
		// response
		CoreProto.TransportPackageData.Builder packageBuilder = CoreProto.TransportPackageData.newBuilder();
		// header
		Map<Integer, String> header = new HashMap<Integer, String>();
		// 站点业务版本（proto版本）
		header.put(CoreProto.HeaderKey.SITE_SERVER_VERSION_VALUE, CommandConst.SITE_VERSION);
		packageBuilder.putAllHeader(header);
		// errCode
		CoreProto.ErrorInfo errInfo = CoreProto.ErrorInfo.newBuilder().setCode(ErrorCode2.ERROR_SESSION.getCode())
				.setInfo(ErrorCode2.ERROR_SESSION.getInfo()).build();
		packageBuilder.setErr(errInfo);

		// 协议版本 CommandConst.PROTOCOL_VERSION=1.0
		String protocolVersion = CommandConst.PROTOCOL_VERSION;
		String action = CommandConst.ACTION_RES;
		channel.writeAndFlush(
				new RedisCommand().add(protocolVersion).add(action).add(packageBuilder.build().toByteArray()))
				.addListener(new GenericFutureListener<Future<? super Void>>() {

					public void operationComplete(Future<? super Void> future) throws Exception {
						channel.close();
					}
				});
	}

}
