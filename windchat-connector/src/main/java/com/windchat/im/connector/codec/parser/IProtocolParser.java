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

import java.util.List;

import com.windchat.im.connector.codec.protocol.MessageDecoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * Decoder中使用IProtocolParser，将Byte转成protocolPacket,并交给out传递给InboundHandler
 * 
 * @author Sam{@link an.guoyue254@gmail.com}
 * @since 2017.09.27
 */
public interface IProtocolParser {

	void readAndOut(Channel ch, ByteBuf inByte, List<Object> out, MessageDecoder decoder) throws Exception;

}
