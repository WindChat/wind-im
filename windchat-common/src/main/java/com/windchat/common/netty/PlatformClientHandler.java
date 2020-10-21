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
package com.windchat.common.netty;

import com.akaxin.proto.core.CoreProto;
import com.windchat.common.command.Command;
import com.windchat.common.command.RedisCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2018-01-19 18:54:30
 */
public class PlatformClientHandler extends SimpleChannelInboundHandler<RedisCommand> {
	private static final Logger logger = LoggerFactory.getLogger(PlatformClientHandler.class);

	private final PlatformSSLClient nettyClient;

	public PlatformClientHandler(PlatformSSLClient nettyClient) {
		this.nettyClient = nettyClient;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// logger.info("------Close channel of tcp socket.-----");
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RedisCommand redisCmd) throws Exception {
		String version = redisCmd.getParameterByIndex(0);
		String action = redisCmd.getParameterByIndex(1);
		byte[] params = redisCmd.getBytesParamByIndex(2);

		CoreProto.TransportPackageData packageData = CoreProto.TransportPackageData.parseFrom(params);
		CoreProto.ErrorInfo errInfo = packageData.getErr();
		Command command = new Command();
		command.setHeader(packageData.getHeaderMap());
		command.setParams(packageData.getData().toByteArray());

		// logger.info("netty client channel handler command={}", command.toString());

		PlatformClientHandler.this.nettyClient
				.handleResponse(new RedisCommandResponse(redisCmd, errInfo.getCode(), errInfo.getInfo()));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause != null) {
			logger.error("netty client channel exeception happen.", cause);
		}
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	}

}
