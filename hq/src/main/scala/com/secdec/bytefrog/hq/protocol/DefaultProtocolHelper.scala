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

package com.codedx.codepulse.hq.protocol

import com.codedx.codepulse.agent.common.message._
import com.secdec.bytefrog.hq.protocol.DataMessageParserV2

object DefaultProtocolHelper extends ProtocolHelper {

	def latestProtocolVersion = 4

	/** Returns a `MessageProtocol` instance associated with the given `version`, as
	  * an option.
	  *
	  * @return `Some` with the `MessageProtocol` instance, if one exists, or `None` if
	  * no such class exists.
	  */
	def getMessageProtocol(version: Int): Option[MessageProtocol] = version match {
		case 1 => Some(new MessageProtocolV1)
		case 2 => Some(new MessageProtocolV2)
		case 3 => Some(new MessageProtocolV3)
		case 4 => Some(new MessageProtocolV4)
		case _ => None
	}

	/** Returns a `ControlMessageSneder` instance associated with the given `version`,
	  * as an option.
	  *
	  * @return `Some` with the appropriate `ControlMessageSender` if it exists, or
	  * `None` if no such class exists.
	  */
	def getControlMessageSender(version: Int): Option[ControlMessageSender] = version match {
		case 1 => Some(ControlMessageSenderV1)
		case 2 => Some(ControlMessageSenderV2)
		case 3 => Some(ControlMessageSenderV2)
		case 4 => Some(ControlMessageSenderV2)
		case _ => None
	}

	def getControlMessageReader(version: Int): Option[ControlMessageReader] = version match {
		case 1 => Some(ControlMessageReaderV1)
		case 2 => Some(ControlMessageReaderV1)
		case 3 => Some(ControlMessageReaderV1)
		case 4 => Some(ControlMessageReaderV1)
		case _ => None
	}

	def getDataMessageParser(version: Int): Option[DataMessageParser] = version match {
		case 1 => Some(DataMessageParserV1)
		case 2 => Some(DataMessageParserV1)
		case 3 => Some(DataMessageParserV2)
		case 4 => Some(DataMessageParserV2)
		case _ => None
	}
}