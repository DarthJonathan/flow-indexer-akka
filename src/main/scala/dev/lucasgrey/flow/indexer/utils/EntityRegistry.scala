package dev.lucasgrey.flow.indexer.utils

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.Materializer
import dev.lucasgrey.flow.indexer.actors.block.BlockActor

import scala.concurrent.ExecutionContext

class EntityRegistry(val sharding: ClusterSharding) (
  implicit val executionContext: ExecutionContext,
  val materializer: Materializer,
  val actorSystem: ActorSystem[_]
) {

  def getBlockActorByHeight(height: String) = {
    sharding.entityRefFor(BlockActor.EntityKey, height)
  }
}
