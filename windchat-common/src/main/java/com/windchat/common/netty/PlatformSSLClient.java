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

import com.windchat.common.command.RedisCommand;
import com.windchat.common.netty.codec.MessageDecoder;
import com.windchat.common.netty.codec.MessageEncoder;
import com.windchat.common.ssl.ZalySSLContext;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.FailedFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.SucceededFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

/**
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2018-01-19 18:14:16
 */
public class PlatformSSLClient {
	private static final Logger logger = LoggerFactory.getLogger(PlatformSSLClient.class);
	private volatile ChannelPromise channelPromise;
	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
	private final Bootstrap clientBoot = new Bootstrap();
	private Promise<IRedisCommandResponse> responsePromise;
	private static final Exception CONNECT_EXCEPTION = new Exception("client connect to server error");
	private PlatformClientHandler nettyClientHandler;

	private String peerHost;
	private int peerPort;

	public PlatformSSLClient() {
		try {
			clientBoot.option(ChannelOption.TCP_NODELAY, true);
			clientBoot.group(eventLoopGroup);
			clientBoot.channel(NioSocketChannel.class);
			clientBoot.option(ChannelOption.TCP_NODELAY, true);
			clientBoot.handler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel channel) throws Exception {
					// use ssl
					SslContext sslContext = ZalySSLContext.getSSLContext();
					SSLEngine sslEngine = sslContext.newEngine(channel.alloc(), peerHost, peerPort);
					channel.pipeline().addLast(new SslHandler(sslEngine));

					channel.pipeline().addLast(new MessageEncoder());
					channel.pipeline().addLast(new MessageDecoder());
					channel.pipeline().addLast("timeout", new IdleStateHandler(20, 20, 0, TimeUnit.SECONDS));
					channel.pipeline().addLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS));

					nettyClientHandler = new PlatformClientHandler(PlatformSSLClient.this);
					channel.pipeline().addLast(nettyClientHandler);
				}

			});
		} catch (Exception e) {
			shutDownGracefully();
			logger.error("init netty client error.", e);
		}
	}

	public Future<Void> connect(String address, int port) {
		final Future<Void> connectionFuture;
		this.peerHost = address;
		this.peerPort = port;
		
		synchronized (clientBoot) {
			if (this.channelPromise == null) {
				try {
					final ChannelFuture connectFuture = this.clientBoot.connect(address, port).sync();
					this.channelPromise = connectFuture.channel().newPromise();

				} catch (Exception e) {
					logger.error("connect to WindChat platform error.", e);
				}

			}
			connectionFuture = this.channelPromise;
		}
		// logger.info("connect to server connectionFuture={}", connectionFuture);
		return connectionFuture;
	}

	public void shutDownGracefully() {
		try {
			if (eventLoopGroup != null) {
				eventLoopGroup.shutdownGracefully();
				eventLoopGroup.terminationFuture().sync();
			}
		} catch (InterruptedException e) {
			logger.error("shutdown netty client error.", e);
		}
	}

	public Future<IRedisCommandResponse> sendRedisCommand(final RedisCommand redisCommand) {
		final Future<IRedisCommandResponse> responseFuture;
		if (channelPromise != null) {
			final ChannelPromise readyPromise = this.channelPromise;

			final DefaultPromise<IRedisCommandResponse> responsePromise = new DefaultPromise<IRedisCommandResponse>(
					readyPromise.channel().eventLoop());
			// 提交一个事件
			readyPromise.channel().eventLoop().submit(new Runnable() {
				@Override
				public void run() {
					// 将这个结果赋值给responsePromise
					PlatformSSLClient.this.responsePromise = responsePromise;
				}
			});

			readyPromise.channel().writeAndFlush(redisCommand).addListener(new GenericFutureListener<ChannelFuture>() {
				@Override
				public void operationComplete(final ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						// 如果失败了，直接将promise返回
						responsePromise.tryFailure(future.cause());
						logger.error("send push message error: {},cause={}", redisCommand, future.cause());
					} else {
						// logger.info("write data to platform success");
					}
				}
			});
			responseFuture = responsePromise;
		} else {
			logger.error("send push error because client is not connected: {}", redisCommand.toString());
			responseFuture = new FailedFuture<IRedisCommandResponse>(GlobalEventExecutor.INSTANCE, CONNECT_EXCEPTION);
		}
		return responseFuture;
	}

	// 提交一次tcp请求结果
	protected void handleResponse(final IRedisCommandResponse response) {
		try {
			this.responsePromise.setSuccess(response);
		} catch (Exception e) {
			logger.error("handlePushNotificationResponse error!", e);
		}
	}

	public void disconnect() {
		// logger.info("close tcp socket, Disconnecting.");
		synchronized (this.clientBoot) {
			this.channelPromise = null;
			final Future<Void> channelCloseFuture;
			if (this.channelPromise != null) {
				channelCloseFuture = this.channelPromise.channel().close();
			} else {
				channelCloseFuture = new SucceededFuture<Void>(GlobalEventExecutor.INSTANCE, null);
			}
			channelCloseFuture.addListener(new GenericFutureListener<Future<Void>>() {
				@Override
				public void operationComplete(final Future<Void> future) throws Exception {
					PlatformSSLClient.this.clientBoot.config().group().shutdownGracefully();
				}
			});
		}
		// logger.info("close netty tcp socket connection");
	}
}