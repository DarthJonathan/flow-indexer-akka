package dev.lucasgrey.flow.indexer.utils

import akka.actor.typed.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import dev.lucasgrey.flow.indexer.model.FlowId

import scala.concurrent.ExecutionContext

class FlowHelper(
  val flowClient: FlowClient
)(implicit val executionContext: ExecutionContext,
  val materializer: Materializer,
  val actorSystem: ActorSystem[_]
) {
  def extractCollections(collectionIdList: List[FlowId]) = {
    Source(collectionIdList)
      .mapAsync(4) { collectionId => flowClient.getTransactionList(collectionId) }
      .runWith(Sink.seq)
  }

  def extractTransactions(transactionIds: List[FlowId]) = {
    Source(transactionIds)
      .mapAsync(4) { transactionId => {
        for {
          transaction <- flowClient.getTransactionById(transactionId)
          transactionResult <- flowClient.getTransactionResultById(transactionId)
          res = transaction.copy(transactionResult = Some(transactionResult))
        } yield res
      }
      }
      .runWith(Sink.seq)
  }
}
