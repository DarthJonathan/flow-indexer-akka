package dev.lucasgrey.flow.indexer

import dev.lucasgrey.flow.indexer.serializable.JsonSerializable
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.Instant

package object model {
  case class FlowBlockHeader(
    height: Long,
    id: FlowId,
    parentId: FlowId
  ) extends JsonSerializable

  object FlowBlockHeader {
    implicit val decoder: Decoder[FlowBlockHeader] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowBlockHeader] = deriveEncoder
  }

  case class FlowBlock(
    height: Long,
    id: FlowId,
    parentId: FlowId,
    timestamp: Instant,
    collectionGuarantee: List[FlowCollectionGuarantee],
    seals: List[FlowSeals],
    signatures: List[FlowSignature]
  ) extends JsonSerializable

  object FlowBlock {
    implicit val decoder: Decoder[FlowBlock] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowBlock] = deriveEncoder
  }

  case class FlowSeals(
    id: FlowId,
    executionReceiptId: FlowId,
    executionReceiptSignatures: List[FlowSignature],
    resultApprovalSignatures: List[FlowSignature]
  ) extends JsonSerializable

  object FlowSeals {
    implicit val decoder: Decoder[FlowSeals] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowSeals] = deriveEncoder
  }

  case class FlowCollection(
    collectionId: FlowId,
    transactionList: List[FlowId]
  ) extends JsonSerializable

  object FlowCollection {
    implicit val decoder: Decoder[FlowCollection] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowCollection] = deriveEncoder
  }

  case class FlowCollectionGuarantee(
    collectionId: FlowId,
    signatureList: List[FlowId]
  ) extends JsonSerializable

  object FlowCollectionGuarantee {
    implicit val decoder: Decoder[FlowCollectionGuarantee] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowCollectionGuarantee] = deriveEncoder
  }

  case class FlowTransaction (
    transactionId: FlowId,
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

  object FlowTransaction {
    implicit val decoder: Decoder[FlowTransaction] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowTransaction] = deriveEncoder
  }

  case class TransactionResult (
    status: String,
    block: FlowId,
    events: List[FlowEvents]
  ) extends JsonSerializable

  object TransactionResult {
    implicit val decoder: Decoder[TransactionResult] = deriveDecoder
    implicit val encoder: Encoder.AsObject[TransactionResult] = deriveEncoder
  }

  case class FlowEvents (
    eventType: String,
    transactionId: FlowId,
    transactionIndex: Int,
    eventIndex: Int,
    payload: String
  ) extends JsonSerializable

  object FlowEvents {
    implicit val decoder: Decoder[FlowEvents] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowEvents] = deriveEncoder
  }

  case class ProposalKey(
    address: FlowSignature,
    keyId: Long,
    sequenceNo: Long
  ) extends JsonSerializable

  object ProposalKey {
    implicit val decoder: Decoder[ProposalKey] = deriveDecoder
    implicit val encoder: Encoder.AsObject[ProposalKey] = deriveEncoder
  }

  case class FlowSingleSignature(
    signature: FlowSignature,
    keyId: Long,
    address: FlowAccount
  ) extends JsonSerializable

  object FlowSingleSignature {
    implicit val decoder: Decoder[FlowSingleSignature] = deriveDecoder
    implicit val encoder: Encoder.AsObject[FlowSingleSignature] = deriveEncoder
  }

  type FlowId = String
  type FlowSignature = String
  type FlowAccount = String
}
