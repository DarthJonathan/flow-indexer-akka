package dev.lucasgrey.flow.indexer.dao.transaction

import dev.lucasgrey.flow.indexer.model.{FlowSingleSignature, ProposalKey, TransactionResult}

import java.time.Instant

case class TransactionData (
   transactionId: String,
   blockId: String,
   blockHeight: Long,
   script: String,
   arguments: List[String],
   referenceBlockId: String,
   payer: String,
   gasLimit: Long,
   proposalKey: ProposalKey,
   authorizers: List[String],
   payloadSignatures: List[FlowSingleSignature],
   envelopeSignatures: List[FlowSingleSignature],
   transactionResult: Option[TransactionResult],
   timestamp: Instant
)
