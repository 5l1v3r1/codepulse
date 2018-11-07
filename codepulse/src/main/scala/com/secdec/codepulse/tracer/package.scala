/*
 * Code Pulse: A real-time code coverage testing tool. For more information
 * see http://code-pulse.com
 *
 * Copyright (C) 2014 Applied Visions - http://securedecisions.avi.com
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

package com.secdec.codepulse

import scala.language.implicitConversions

import akka.actor.{ ActorRef, ActorSystem, Props }
import com.secdec.codepulse.data.model.ProjectDataProvider
import com.secdec.codepulse.data.model.slick.SlickH2ProjectDataProvider
import com.secdec.codepulse.dependencycheck.ScanSettings
import com.secdec.codepulse.events.GeneralEventBus
import com.secdec.codepulse.input.InputFileProcessor
import com.secdec.codepulse.input.dependencycheck.DependencyCheckPostProcessor
import com.secdec.codepulse.input.project.ProjectInputActor

package object tracer {

	class BootVar[T] {
		private var _value: Option[T] = None
		def apply() = _value getOrElse {
			throw new IllegalStateException("pack Code Pulse has not booted yet")
		}
		private[tracer] def set(value: T) = {
			_value = Some(value)
		}
	}

	implicit def bootVarToInstance[T](v: BootVar[T]): T = v.apply()

	val actorSystem = new BootVar[ActorSystem]
	val projectManager = new BootVar[ProjectManager]
	val projectDataProvider = new BootVar[ProjectDataProvider]
	val transientTraceDataProvider = new BootVar[TransientTraceDataProvider]
	val treeBuilderManager = new BootVar[TreeBuilderManager]
	val projectFileUploadServer = new BootVar[ProjectFileUploadHandler]
	val apiServer = new BootVar[APIServer]
	val traceConnectionLooper = new BootVar[TraceConnectionLooper.API]
	val traceConnectionAcknowledger = new BootVar[TraceConnectionAcknowledger]
	val generalEventBus = new BootVar[GeneralEventBus]
	val projectInput = new BootVar[ActorRef]
	val inputFileProcessor = new BootVar[ActorRef]

	def boot() {
		val as = ProjectManager.defaultActorSystem

		val dataProvider = new SlickH2ProjectDataProvider(ProjectDataProvider.DefaultStorageDir, as)
		projectDataProvider set dataProvider
		transientTraceDataProvider set new TransientTraceDataProvider

		val tbm = new TreeBuilderManager(dataProvider)
		treeBuilderManager set tbm

		val tm = new ProjectManager(as)

		traceConnectionAcknowledger set TraceConnectionAcknowledgerActor.create(as)

		val looper = TraceConnectionLooper.create(as, traceConnectionAcknowledger())
		looper.start()
		traceConnectionLooper set looper

		actorSystem set as
		generalEventBus set new GeneralEventBus
		projectManager set tm
		projectFileUploadServer set new ProjectFileUploadHandler(tm, generalEventBus).initializeServer
		apiServer set new APIServer(tm, tbm, generalEventBus).initializeServer

		val projectInputActor = actorSystem actorOf Props[ProjectInputActor]
		generalEventBus.subscribe(projectInputActor, "Failed")
		generalEventBus.subscribe(projectInputActor, "ProcessDataAvailable")
		projectInput set projectInputActor

		val inputFileProcessorActor = actorSystem actorOf Props(new InputFileProcessor(generalEventBus))
		generalEventBus.subscribe(inputFileProcessorActor, "DataInputAvailable")
		inputFileProcessor set inputFileProcessorActor
	}
}