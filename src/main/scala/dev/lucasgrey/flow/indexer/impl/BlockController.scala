package dev.lucasgrey.flow.indexer.impl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import akka.pattern.StatusReply
import akka.util.Timeout
import dev.lucasgrey.flow.indexer.actors.block.BlockActor
import dev.lucasgrey.flow.indexer.utils.ActorInspection.InspectEntityCmd
import dev.lucasgrey.flow.indexer.utils.EntityRegistry

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class BlockController(
//  entityRegistry: EntityRegistry
)(implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) {

  implicit val timeout: Timeout = 10.seconds

  def inspectBlockEntity(requestContext: RequestContext): Future[RouteResult] = {
//    val entity = entityRegistry.getBlockActorByHeight(46600000)
//    for {
//      stateResult <- entity ? (replyTo => InspectEntityCmd(replyTo))
//    }
    for {
      _ <- Future.unit
    }
    yield Complete(
      HttpResponse(
        status = 200,
        entity = HttpEntity(
          ContentTypes.`application/json`,
          ""
        )
      )
    )
  }
}
