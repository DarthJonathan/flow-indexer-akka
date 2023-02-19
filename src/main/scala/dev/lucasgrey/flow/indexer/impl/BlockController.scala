package dev.lucasgrey.flow.indexer.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import dev.lucasgrey.flow.indexer.utils.ActorInspection.InspectEntityCmd
import dev.lucasgrey.flow.indexer.utils.EntityRegistry
import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class BlockController(
  entityRegistry: EntityRegistry
)(implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) {

  implicit val timeout: Timeout = 10.seconds

  def inspectBlockEntity(height: Long): Route = {
    val res = for {
      stateResult <- entityRegistry.getBlockActorByHeight(height) ? (replyTo => InspectEntityCmd(replyTo))
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
