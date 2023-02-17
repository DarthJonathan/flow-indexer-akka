package dev.lucasgrey.flow.indexer.actors.block.command.handlers

import akka.persistence.typed.scaladsl.Effect
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, NewBlockRegistered, blockEventHandler}
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, Initialized}
import dev.lucasgrey.flow.indexer.model.FlowBlock

import java.time.ZoneOffset

class RegisterBlockCmdHandler(
//  accessAPI: FlowAccessApi
) extends StrictLogging {

  def handleRegisterBlock(state: BlockState): Effect[BlockEvent, BlockState] = {
    if (state.isInstanceOf[Initialized]) {
      logger.info(s"Block height ${flowBlockHeader.getHeight} already exist, skipping")
      Effect.none
    } else {
      //Load block initial data
      val flowBlock = accessAPI.getBlockById(flowBlockHeader.getId)
      val modelBlock = FlowBlock(
        height = flowBlock.getHeight,
        id = flowBlock.getId.getBytes,
        parentId = flowBlock.getParentId.getBytes,
        timestamp = flowBlock.getTimestamp.toInstant(ZoneOffset.UTC),
        collectionGuarantee = None,
        seals = None,
        signatures = None
      )
      Effect.persist(NewBlockRegistered(
        height = flowBlockHeader.getHeight,
        blockHeader = flowBlockHeader,
        block = modelBlock
      ))
    }
  }

}
