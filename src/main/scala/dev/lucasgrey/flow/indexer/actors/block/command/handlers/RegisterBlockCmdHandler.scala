package dev.lucasgrey.flow.indexer.actors.block.command.handlers

import akka.persistence.typed.scaladsl.Effect
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.RegisterBlock
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, NewBlockRegisteredEvt}
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, Initialized}

class RegisterBlockCmdHandler extends StrictLogging {
  def handleRegisterBlock(state: BlockState, cmd: RegisterBlock): Effect[BlockEvent, BlockState] = {
    if (state.isInstanceOf[Initialized]) {
      logger.info(s"Block height ${cmd.block.height} already exist, skipping")
      Effect.none
    } else {
      logger.info(s"Block height ${cmd.block.height} newly registered")
      Effect.persist(NewBlockRegisteredEvt(
        height = cmd.block.height,
        transactionList = cmd.transactionList,
        block = cmd.block
      ))
    }
  }

}
