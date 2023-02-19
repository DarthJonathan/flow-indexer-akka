package dev.lucasgrey.flow.indexer.actors.block

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.blockCommandHandler
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, blockEventHandler}
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, NotInitialized}
import dev.lucasgrey.flow.indexer.utils.ActorInspection.InspectableEntity

import scala.concurrent.duration.DurationInt

object BlockActor {

  val tags = Vector.tabulate(4)(i => s"blocks-$i")
  val EntityKey: EntityTypeKey[InspectableEntity] = EntityTypeKey[InspectableEntity]("BlockActor")

  def apply(blockHeight: String, projectionTag: String): Behavior[InspectableEntity] = {
    EventSourcedBehavior[InspectableEntity, BlockEvent, BlockState] (
      emptyState = NotInitialized,
      eventHandler = blockEventHandler,
      commandHandler = blockCommandHandler,
      persistenceId = PersistenceId("blockHeight", blockHeight)
    )
      .withTagger(_ => Set(projectionTag))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 10, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }
}
