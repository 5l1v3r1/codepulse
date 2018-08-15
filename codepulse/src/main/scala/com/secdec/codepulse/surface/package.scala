package com.secdec.codepulse

import akka.actor.{ActorRef, ActorSystem, Props}
import com.secdec.codepulse.components.surface.Updates
import com.secdec.codepulse.events.GeneralEventBus
import com.secdec.codepulse.input.surface.SurfaceDetectorPostProcessor

package object surface {
  class BootVar[T] {
    private var _value: Option[T] = None
    def apply() = _value getOrElse {
      throw new IllegalStateException("depCode Pulse has not booted yet")
    }
    private[surface] def set(value: T) = {
      _value = Some(value)
    }
  }

  val surfaceDetectorUpdates = new BootVar[ActorRef]
  val surfaceDetectorPostProcessor = new BootVar[ActorRef]

  def boot(actorSystem: ActorSystem, eventBus: GeneralEventBus) {

    val surfaceDetectorPostProcessorActor = actorSystem actorOf Props(new SurfaceDetectorPostProcessor(eventBus))
    eventBus.subscribe(surfaceDetectorPostProcessorActor, "ProcessDataAvailable")
    surfaceDetectorPostProcessor set surfaceDetectorPostProcessorActor

    val updates = actorSystem actorOf Props[Updates]
    eventBus.subscribe(updates, "Running")
    eventBus.subscribe(updates, "Finished")
    eventBus.subscribe(updates, "Failed")
    eventBus.subscribe(updates, "Unknown")
    surfaceDetectorUpdates set updates
  }
}
