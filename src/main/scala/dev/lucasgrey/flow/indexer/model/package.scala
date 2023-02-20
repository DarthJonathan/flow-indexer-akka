package dev.lucasgrey.flow.indexer

import dev.lucasgrey.flow.indexer.serializable.JsonSerializable

import java.time.Instant

package object model {
  case class FlowBlockHeader(
    height: Long,
    id: FlowId,
    parentId: FlowId
  ) extends JsonSerializable

  case class FlowBlock(
    height: Long,
    id: FlowId,
    parentId: FlowId,
    timestamp: Instant,
    collectionGuarantee: List[FlowCollection],
    seals: List[FlowSeals],
    signatures: List[FlowSignature]
  ) extends JsonSerializable

  case class FlowSeals(
    id: FlowId,
    executionReceiptId: FlowId,
    executionReceiptSignatures: List[FlowSignature],
    resultApprovalSignatures: List[FlowSignature]
  ) extends JsonSerializable

  case class FlowCollection(
    collectionId: FlowId,
    transactionList: List[FlowId]
  ) extends JsonSerializable

  case class FlowTransaction (
    //HeavyData
    script: String,
    arguments: List[String],
    referenceBlockId: FlowId,
    payer: FlowAccount,
    gasLimit: Long,
    authorizers: List[FlowAccount],
    payloadSignatures: List[],
    envelopeSignatures: List[]
  )

  case class ProposalKey(
    address: List[]
  )

  type FlowId = String
  type FlowSignature = String
  type FlowAccount = String
}
