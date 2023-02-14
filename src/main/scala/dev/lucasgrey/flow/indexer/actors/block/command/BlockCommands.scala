package dev.lucasgrey.flow.indexer.actors.block.command

import akka.persistence.typed.scaladsl.Effect
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.BlockEvent
import dev.lucasgrey.flow.indexer.actors.block.state.BlockState

object BlockCommands {
  sealed trait BlockCommand

  type BlockCommandHandler = (BlockState, BlockCommand) => Effect[BlockEvent, BlockState]

  val blockCommandHandler: BlockCommandHandler = (state, cmd) => {
    cmd match {
      case _ => ???
    }
  }

}
