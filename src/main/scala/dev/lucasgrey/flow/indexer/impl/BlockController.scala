package dev.lucasgrey.flow.indexer.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import dev.lucasgrey.flow.indexer.dao.transaction.TransactionDataRepository
import dev.lucasgrey.flow.indexer.utils.EntityRegistry
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class BlockController(
  entityRegistry: EntityRegistry,
  transactionDataRepository: TransactionDataRepository
)(implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) {

  implicit val timeout: Timeout = 10.seconds

  def getTransactionById(trxId: String): Route = {
    val res = transactionDataRepository.findTransactionById(trxId)
      .map(trx => {
        HttpResponse(
          status = 200,
          entity = HttpEntity(
            ContentTypes.`application/json`,
            trx.asJson.toString
          ))
      })

    complete(res)
  }
}
