package dev.lucasgrey.flow.indexer.actors.block

import dev.lucasgrey.flow.indexer.model.{FlowBlock, FlowTransaction}
import dev.lucasgrey.flow.indexer.serializable.JsonSerializable
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

package object state {

  sealed trait BlockState extends JsonSerializable

  case object NotInitialized extends BlockState

  case class Initialized(
    flowBlock: Option[FlowBlock],
    transactionList: List[FlowTransaction]
  ) extends BlockState

  implicit val stateEncoder: Encoder[BlockState] = Encoder.instance {
    case NotInitialized => "Not Initialized".asJson
    case initialized @ Initialized(_, _) => initialized.asJson
  }
}
