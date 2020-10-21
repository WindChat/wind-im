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
package com.windchat.im.connector.codec.parser;

import java.util.ArrayList;
import java.util.List;

import com.windchat.common.command.RedisCommand;
import com.windchat.common.resp.AbstractParameter;
import com.windchat.common.resp.RedisBytesParameter;
import com.windchat.im.connector.codec.protocol.MessageDecoder;
import com.windchat.im.connector.codec.protocol.ReplaySignal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2017-09-27 11:40:49
 */
public class ProtocolParser implements IProtocolParser {

	public void readAndOut(Channel ch, ByteBuf inByte, List<Object> out, MessageDecoder decoder) throws Exception {
		switch (decoder.state()) {
		case START_POINT:
			List<AbstractParameter> paramsList = null;

			byte firstByte = inByte.readByte();
			if (firstByte == '*') {
				List<Byte> sizeBytes = new ArrayList<Byte>();
				while (true) {
					byte curent = inByte.readByte();
					if (curent == '\r' && inByte.readByte() == '\n') {
						break;
					}
					sizeBytes.add(curent);
				}

				byte[] tempBytes = new byte[sizeBytes.size()];
				for (int i = 0; i < sizeBytes.size(); i++) {
					tempBytes[i] = sizeBytes.get(i);
				}

				paramsList = new ArrayList<AbstractParameter>(Integer.parseInt(new String(tempBytes)));

				while (true) {
					if (inByte.readByte() == '$') {
						List<Byte> interBytes = new ArrayList<Byte>();
						while (true) {
							byte curent = inByte.readByte();
							if (curent == '\r' && inByte.readByte() == '\n') {
								break;
							}
							interBytes.add(curent);

						}

						byte[] tempInnerByte = new byte[interBytes.size()];
						for (int j = 0; j < interBytes.size(); j++) {
							tempInnerByte[j] = interBytes.get(j);
						}

						int readByteSize = Integer.parseInt(new String(tempInnerByte));
						// System.out.println("String length=" + readByteSize);

						byte[] dataBuffer = new byte[readByteSize];
						inByte.readBytes(dataBuffer);
						paramsList.add(new RedisBytesParameter(dataBuffer));

						// System.out.println("read String ==" + new String(dataBuffer));

						if (inByte.readByte() == '\r' && inByte.readByte() == '\n') {
							// System.out.println("success!...");
						} else {
							System.out.println("error!.....");
						}

						if (paramsList.size() == Integer.parseInt(new String(tempBytes))) {
							// System.out.println("receive data from client ......");
							break;
						}

					}
				}

				decoder.checkpoint(ReplaySignal.START_POINT);
				out.add(buildRedisCommand(paramsList));
			}

			break;

		default:
			throw new Exception("parsing protocol exceptions");
		}
	}

	private RedisCommand buildRedisCommand(List<AbstractParameter> redisParamsList) {
		return new RedisCommand().addAll(redisParamsList);
	}

}
