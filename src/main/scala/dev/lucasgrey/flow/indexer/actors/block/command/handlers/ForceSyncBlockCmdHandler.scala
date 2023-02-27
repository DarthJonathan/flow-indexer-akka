package dev.lucasgrey.flow.indexer.actors.block.command.handlers

import akka.persistence.typed.scaladsl.Effect
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.{ForceSyncBlock, RegisterBlock}
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, ForceSyncBlockEvt, NewBlockRegisteredEvt}
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, Initialized}

class ForceSyncBlockCmdHandler extends StrictLogging {
  def handleForceSyncBlock(state: BlockState, cmd: ForceSyncBlock): Effect[BlockEvent, BlockState] = {
    if (state.isInstanceOf[Initialized]) {
      logger.info(s"Block height ${cmd.block.height} already exist, forcing sync")
      Effect.persist(ForceSyncBlockEvt(
        height = cmd.block.height,
        transactionList = cmd.transactionList,
        block = cmd.block
      ))
    } else {
      logger.info(s"Block height ${cmd.block.height} newly registered - was a missing block")
      Effect.persist(NewBlockRegisteredEvt(
        height = cmd.block.height,
        transactionList = cmd.transactionList,
        block = cmd.block
      ))
    }
  }

}
