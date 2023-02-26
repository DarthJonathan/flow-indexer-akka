package dev.lucasgrey.flow.indexer.actors.block.event

import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, Initialized}
import dev.lucasgrey.flow.indexer.model.{FlowBlock, FlowBlockHeader, FlowTransaction}
import dev.lucasgrey.flow.indexer.serializable.JsonSerializable

object BlockEvents extends StrictLogging {

  sealed trait BlockEvent extends JsonSerializable
  case class NewBlockRegistered(height: Long, block: FlowBlock, transactionList: List[FlowTransaction]) extends BlockEvent

  type BlockEventHandler = (BlockState, BlockEvent) => BlockState

  val blockEventHandler: BlockEventHandler = (_, event) => {
    event match {
      case x: NewBlockRegistered =>
        logger.info(s"Registered new block height ${x.height}")
        Initialized(
          flowBlock = Some(x.block),
          transactionList = x.transactionList,
          isSealed = false
        )
      case _ => ???
    }
  }

}