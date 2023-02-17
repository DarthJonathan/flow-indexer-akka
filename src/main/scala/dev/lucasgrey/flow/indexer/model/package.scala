package dev.lucasgrey.flow.indexer

import dev.lucasgrey.flow.indexer.serializable.JsonSerializable

import java.time.Instant

package object model {
  case class FlowBlock(
    height: Long,
    id: FlowId,
    parentId: FlowId,
    timestamp: Instant,
    collectionGuarantee: Option[List[(FlowId, List[FlowSignature])]],
    seals: Option[List[FlowSeals]],
    signatures: Option[List[FlowSignature]]
  ) extends JsonSerializable

  case class FlowSeals(
    id: FlowId,
    executionReceiptId: FlowId,
    executionReceiptSignatures: List[FlowSignature],
    resultApprovalSignatures: List[FlowSignature]
  ) extends JsonSerializable


  type FlowId = Seq[Byte]
  type FlowSignature = Seq[Byte]
}
