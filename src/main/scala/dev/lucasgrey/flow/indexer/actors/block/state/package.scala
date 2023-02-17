package dev.lucasgrey.flow.indexer.actors.block

import dev.lucasgrey.flow.indexer.model.{FlowBlock, FlowBlockHeader}
import dev.lucasgrey.flow.indexer.serializable.JsonSerializable

package object state {

  sealed trait BlockState extends JsonSerializable

  case object NotInitialized extends BlockState

  case class Initialized(
    flowBlockHeader: FlowBlockHeader,
    flowBlock: Option[FlowBlock],
    isSealed: Boolean
  ) extends BlockState
}
