package dev.lucasgrey.flow.indexer.actors.block.event

import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, Initialized}
import dev.lucasgrey.flow.indexer.model.{FlowBlock, FlowBlockHeader}
import dev.lucasgrey.flow.indexer.serializable.JsonSerializable

object BlockEvents extends StrictLogging {

  sealed trait BlockEvent extends JsonSerializable
  case class NewBlockRegistered(height: Long, blockHeader: FlowBlockHeader, block: FlowBlock) extends BlockEvent

  type BlockEventHandler = (BlockState, BlockEvent) => BlockState

  val blockEventHandler: BlockEventHandler = (state, event) => {
    event match {
      case x:NewBlockRegistered =>
        logger.info(s"Registered new block height ${x.height}")
        Initialized(
          flowBlockHeader = x.blockHeader,
          flowBlock = Some(x.block),
          isSealed = false
        )
      case _ => ???
    }
  }

}