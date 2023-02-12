package dev.lucasgrey.flow.indexer.actors.block.command

import akka.persistence.typed.scaladsl.Effect
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents
import dev.lucasgrey.flow.indexer.actors.block.state.BlockState

sealed trait BlockCommands


type BlockCommandHandler = (BlockState, BlockCommands) => Effect[BlockEvents, BlockState]

val blockCommandHandler: BlockCommandHandler = (state, cmd) => {
  cmd match {
    case _ => ???
  }
}
