package dev.lucasgrey.flow.indexer

import dev.lucasgrey.flow.indexer.serializable.JsonSerializable
import io.circe.Json

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
    collectionGuarantee: List[FlowCollectionGuarantee],
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

  case class FlowCollectionGuarantee(
    collectionId: FlowId,
    signatureList: List[FlowId]
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
    envelopeSignatures: List[FlowSingleSignature],
    transactionResult: Option[TransactionResult] = None
  ) extends JsonSerializable

  case class TransactionResult (
    status: String,
    block: FlowId,
    events: List[FlowEvents]
  ) extends JsonSerializable

  case class FlowEvents (
    eventType: String,
    transactionId: FlowId,
    transactionIndex: Int,
    eventIndex: Int,
    payload: String
  ) extends JsonSerializable

  case class ProposalKey(
    address: FlowSignature,
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
