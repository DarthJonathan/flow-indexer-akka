package dev.lucasgrey.flow.indexer.dao.transaction

import dev.lucasgrey.flow.indexer.dao.transaction.TransactionTable.{schemaName, tableName}
import dev.lucasgrey.flow.indexer.model.{FlowSingleSignature, ProposalKey, TransactionResult}
import dev.lucasgrey.flow.indexer.utils.PostgresProfileExtended.api._

import java.time.Instant

class TransactionTable(tag: Tag) extends Table[TransactionData] (tag, Some(schemaName), tableName) {
  def transactionId = column[String] ("transaction_id", O.PrimaryKey)
  def blockId = column[String] ("block_id")
  def blockHeight = column[Long] ("height")
  def proposalKey = column[ProposalKey]("proposal_key")
  def transactionStatus = column[Option[TransactionResult]] ("transaction_status")
  def authorizers = column[List[String]] ("authorizers")
  def gasLimit = column[Long] ("gas_limit")
  def payer = column[String] ("payer")
  def referenceBlockId = column[String] ("reference_block_id")
  def arguments = column[List[String]] ("arguments")
  def script = column[String] ("script")
  def payloadSignature = column[List[FlowSingleSignature]] ("flow_signature")
  def envelopedSignature = column[List[FlowSingleSignature]] ("envelope_signature")
  def timestamp = column[Instant]("timestamp")

  def * = (
    transactionId,
    blockId,
    blockHeight,
    script,
    arguments,
    referenceBlockId,
    payer,
    gasLimit,
    proposalKey,
    authorizers,
    payloadSignature,
    envelopedSignature,
    transactionStatus,
    timestamp
  ) <> (TransactionData.tupled, TransactionData.unapply)
}

object TransactionTable {
  private val schemaName = "indexer"
  private val tableName = "transactions"
}
