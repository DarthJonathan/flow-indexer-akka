package dev.lucasgrey.flow.indexer.actors.block.event

import dev.lucasgrey.flow.indexer.actors.block.state.BlockState

object BlockEvents {

  sealed trait BlockEvent

  type BlockEventHandler = (BlockState, BlockEvent) => BlockState

  val blockEventHandler: BlockEventHandler = (state, event) => {
    event match {
      case _ => ???
    }
  }

}