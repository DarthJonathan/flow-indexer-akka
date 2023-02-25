package dev.lucasgrey.flow.indexer.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import dev.lucasgrey.flow.indexer.utils.ActorInspection.InspectEntityCmd
import dev.lucasgrey.flow.indexer.utils.EntityRegistry

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class InspectEntityController(
 entityRegistry: EntityRegistry
)(implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) {

  implicit val timeout: Timeout = 10.seconds

  def inspectEntity(entityName: String, entityId: String): Route = {
    val entity = entityName match {
      case "block" => entityRegistry.getBlockActorByHeight(entityId)
    }

    val res = for {
      stateResult <- entity ? (replyTo => InspectEntityCmd(replyTo))
    } yield HttpResponse(
      status = 200,
      entity = HttpEntity(
        ContentTypes.`application/json`,
        stateResult.getValue
      )
    )

    complete(res)
  }
}
