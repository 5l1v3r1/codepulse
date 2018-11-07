/*
 * Code Pulse: A real-time code coverage testing tool. For more information
 * see http://code-pulse.com
 *
 * Copyright (C) 2017 Applied Visions - http://securedecisions.avi.com
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

package com.secdec.codepulse.data.dotnet

import java.io.File
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import com.ning.http.client.multipart.FilePart
import com.secdec.codepulse.data.{ MethodSignature, MethodTypeParam }
import dispatch.Defaults._
import dispatch._
import com.typesafe.config.ConfigFactory
import net.liftweb.json._

class SymbolReaderHTTPServiceConnector(assembly: File, symbols: File) extends DotNetBuilder {
	val config = ConfigFactory.load()
	val port = config.getString("cp.userSettings.symbolService.port")
	val symbolService = url(s"http://localhost:$port/api/methods")

	override def Methods: List[(MethodSignature, Int, Int, Int, Int)] = {
		implicit val formats = DefaultFormats

		val request = symbolService.POST.addBodyPart(new FilePart("assemblyFile", assembly)).addBodyPart(new FilePart("symbolsFile", symbols))

		val result = Await.result(Http(request).option, 10 second).head.getResponseBody

		val methodInfos = parse(result).children.map(child => {
			val methodInfo = child.extract[MethodInfo]
			(methodInfo.id, methodInfo.surrogateFor, methodInfo.instructions, methodInfo.sequencePointCount, methodInfo.methodStartLine, methodInfo.methodEndLine, (new MethodSignature(
				methodInfo.fullyQualifiedName,
				methodInfo.containingClass,
				Option(methodInfo.file),
				methodInfo.accessModifiers,
				methodInfo.parameters.map(parameter => MethodTypeParam.ReferenceType(parameter)),
				MethodTypeParam.ReferenceType(methodInfo.returnType)
			)))}).sortBy(_._2)

		val methodSignaturesById = collection.mutable.Map[String, MethodSignature]().empty

		methodInfos.map((methodInfo:(String, String, Int, Int, Int, Int, MethodSignature)) => {
			methodSignaturesById(methodInfo._1) = methodInfo._7
			if (methodInfo._2 != "00000000-0000-0000-0000-000000000000") {
				val method = methodSignaturesById(methodInfo._2)
				val surrogateMethod = methodSignaturesById(methodInfo._1)
				surrogateMethod.surrogateFor = Option(method)
				if (method.file == null) {
					method.file = surrogateMethod.file
				}
			}
			(methodInfo._7, methodInfo._3, methodInfo._4, methodInfo._5, methodInfo._6)
		})
	}
}
