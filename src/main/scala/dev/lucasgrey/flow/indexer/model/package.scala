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
    script: String,
    arguments: List[String],
    referenceBlockId: FlowId,
    payer: FlowAccount,
    gasLimit: Long,
    proposalKey: ProposalKey,
    authorizers: List[FlowAccount],
    payloadSignatures: List[FlowSingleSignature],
    envelopeSignatures: List[FlowSingleSignature]
  )

  case class ProposalKey(
    signature: FlowSignature,
    keyId: Long,
    sequenceNo: Long
  )

  case class FlowSingleSignature(
    signature: FlowSignature,
    keyId: Long,
    address: FlowAccount
  )

  type FlowId = String
  type FlowSignature = String
  type FlowAccount = String
}
