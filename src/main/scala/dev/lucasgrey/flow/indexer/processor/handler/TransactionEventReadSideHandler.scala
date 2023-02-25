package dev.lucasgrey.flow.indexer.processor.handler

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.slick.SlickHandler
import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, NewBlockRegistered}
import dev.lucasgrey.flow.indexer.dao.transaction.{TransactionData, TransactionDataRepository}
import slick.dbio.DBIO

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class TransactionEventReadSideHandler(
  transactionDataRepository: TransactionDataRepository
)(implicit val materializer: Materializer, val executionContext: ExecutionContext) extends SlickHandler[EventEnvelope[BlockEvent]] with StrictLogging {
  override def process(envelopes: EventEnvelope[BlockEvent]): DBIO[Done] = {
    envelopes.event match {
      case NewBlockRegistered(height, _, block, trxList) =>
        logger.info(s"Transaction read side processor received height to be stored $height")
        DBIO.sequence(
          trxList.map(trx => {
            transactionDataRepository.upsert(
              TransactionData(
                transactionId = trx.transactionId,
                blockId = block.id,
                blockHeight = block.height,
                script = trx.script,
                arguments = trx.arguments,
                referenceBlockId = trx.referenceBlockId,
                payer = trx.payer,
                gasLimit = trx.gasLimit,
                proposalKey = trx.proposalKey,
                authorizers = trx.authorizers,
                payloadSignatures = trx.payloadSignatures,
                envelopeSignatures = trx.envelopeSignatures,
                transactionResult = trx.transactionResult,
                timestamp = Instant.now()
              )
            )}
        ))
          .map(_ => Done)

      case _ =>
        logger.debug("Unsupported event received by block read side handler, skipping event!")
        DBIO.successful(Done)
    }
  }
}