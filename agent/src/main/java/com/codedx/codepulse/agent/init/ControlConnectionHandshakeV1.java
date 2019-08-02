/* Code Pulse: a real-time code coverage tool, for more information, see <http://code-pulse.com/>
 *
 * Copyright (C) 2014-2017 Code Dx, Inc. <https://codedx.com/>
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

package com.codedx.codepulse.agent.init;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.codedx.codepulse.agent.common.message.NotSupportedException;
import com.codedx.codepulse.agent.control.ConfigurationReader;
import com.codedx.codepulse.agent.errors.ErrorHandler;
import com.codedx.codepulse.agent.common.config.RuntimeAgentConfigurationV1;
import com.codedx.codepulse.agent.common.connect.Connection;
import com.codedx.codepulse.agent.common.message.MessageConstantsV1;
import com.codedx.codepulse.agent.common.message.MessageProtocol;

/**
 * Implements control connection handshake for protocol version 4.
 * @author RobertF
 */
public class ControlConnectionHandshakeV1 implements ControlConnectionHandshake
{
	private final MessageProtocol protocol;
	private final ConfigurationReader configReader;

	public ControlConnectionHandshakeV1(MessageProtocol protocol, ConfigurationReader configReader)
	{
		this.protocol = protocol;
		this.configReader = configReader;
	}

	@Override
	public RuntimeAgentConfigurationV1 performHandshake(Connection connection, int projectId) throws IOException
	{
		DataOutputStream outStream = connection.output();
		DataInputStream inStream = connection.input();

		// say hello!
		try {
			if (projectId == 0) {
				protocol.writeHello(outStream);
			} else {
				protocol.writeProjectHello(outStream, projectId);
			}
		} catch (NotSupportedException e) {
			ErrorHandler.handleError("protocol error: project-hello message is not supported");
			return null;
		}
		outStream.flush();

		byte reply = inStream.readByte();
		switch (reply)
		{
		case MessageConstantsV1.MsgConfiguration:
			return configReader.readConfiguration(inStream);
		case MessageConstantsV1.MsgError:
			ErrorHandler.handleError(String.format("received error from handshake: %s",
					inStream.readUTF()));
			return null;
		default:
			ErrorHandler.handleError("protocol error: invalid or unexpected control message");
			return null;
		}
	}
}
