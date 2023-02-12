package dev.lucasgrey.flow.indexer.actors.block.event

import dev.lucasgrey.flow.indexer.actors.block.state.BlockState

sealed trait BlockEvents

type BlockEventHandler = (BlockState, BlockEvents) => BlockState

val blockEventHandler: BlockEventHandler = (state, event) => {
  event match {
    case _ => ???
  }
}
