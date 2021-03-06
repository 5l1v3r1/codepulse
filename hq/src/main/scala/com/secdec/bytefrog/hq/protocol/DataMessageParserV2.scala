/*
 * Copyright 2018 Secure Decisions, a division of Applied Visions, Inc.
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
 *
 * This material is based on research sponsored by the Department of Homeland
 * Security (DHS) Science and Technology Directorate, Cyber Security Division
 * (DHS S&T/CSD) via contract number HHSP233201600058C.
 */

package com.secdec.bytefrog.hq.protocol

import java.io.{DataInputStream, IOException}

import com.codedx.codepulse.agent.common.message.MessageConstantsV3._
import com.codedx.codepulse.hq.protocol.{DataMessageHandler, DataMessageParserV1}

object DataMessageParserV2 extends DataMessageParserV2

/** A DataMessageParser implementation that assumes the data in each input stream
  * was put there by a MessageProtocol Version implementation.
  *
  * This implementation is thread-safe. DataMessageParserV2 keeps no
  * internal state, so it should have no problem calling `parse`
  * on several different input streams at once, provided each
  * `handler` will not have its own concurrency issues.
  */
class DataMessageParserV2 extends DataMessageParserV1 {
  override protected def readOtherMessage(typeId: Byte, stream: DataInputStream, handler: DataMessageHandler, parseDataBreaks: Boolean): Int = {
    (typeId match {
      case MsgMapSourceLocation => readMapSourceLocation(stream, handler)
      case MsgMethodVisit => readMethodVisit(stream, handler)
      case MsgSourceLocationCount => readSourceLocationCount(stream, handler)
      case _ => throw new IOException(s"Unexpected message type id: $typeId")
    }) + 1
  }

  protected def readSourceLocationCount(stream: DataInputStream, handler: DataMessageHandler): Int = {
    //[4 bytes: assigned signature ID]
    val methodId = stream.readInt

    //[4 bytes: source location count]
    val sourceLocationCount = stream.readInt()

    handler.handleSourceLocationCount(methodId, sourceLocationCount)

    8
  }

  protected def readMapSourceLocation(stream: DataInputStream, handler: DataMessageHandler): Int = {
    //[4 bytes: assigned source location ID]
    val sourceLocationId = stream.readInt

    //[4 bytes: assigned signature ID]
    val methodId = stream.readInt

    //[4 bytes: line]
    val startLine = stream.readInt

	  //[4 bytes: line]
    val endLine = stream.readInt

    //[2 bytes: start character]
    val startCharacter = stream.readShort

    //[2 bytes: end character]
    val endCharacter = stream.readShort

    handler.handleMapSourceLocation(methodId, startLine, endLine, startCharacter, endCharacter, sourceLocationId)

    // read 20 bytes
    20
  }


  protected def readMethodVisit(stream: DataInputStream, handler: DataMessageHandler): Int = {
    //[4 bytes: relative timestamp]
    val timestamp = stream.readInt

    //[4 bytes: current sequence]
    val sequenceId = stream.readInt

    //[4 bytes: method signature ID]
    val methodId = stream.readInt

    //[4 bytes: source location ID]
    val sourceLocationId = stream.readInt

    //[2 bytes: thread ID]
    val threadId = stream.readUnsignedShort

    handler.handleMethodVisit(methodId, sourceLocationId, timestamp, sequenceId, threadId)

    // read 18 bytes
    18
  }
}