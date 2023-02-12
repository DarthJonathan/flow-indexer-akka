package dev.lucasgrey.flow.indexer.actors.block

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import dev.lucasgrey.flow.indexer.actors.block.command.{BlockCommands, blockCommandHandler}
import dev.lucasgrey.flow.indexer.actors.block.event.{BlockEvents, blockEventHandler}
import dev.lucasgrey.flow.indexer.actors.block.state.BlockState

import scala.concurrent.duration.DurationInt

object BlockActor {

  def apply(blockHeight: Long): Behavior[BlockCommands] = {
    EventSourcedBehavior[BlockCommands, BlockEvents, BlockState] (
      emptyState = BlockState(
        BlockHeight = 0L
      ),
      eventHandler = blockEventHandler,
      commandHandler = blockCommandHandler,
      persistenceId = PersistenceId("blockHeight", blockHeight.toString)
    )
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }

}
