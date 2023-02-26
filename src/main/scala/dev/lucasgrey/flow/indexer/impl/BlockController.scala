package dev.lucasgrey.flow.indexer.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import dev.lucasgrey.flow.indexer.dao.transaction.{TransactionData, TransactionDataRepository}
import dev.lucasgrey.flow.indexer.utils.EntityRegistry
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

class BlockController(
  entityRegistry: EntityRegistry,
  transactionDataRepository: TransactionDataRepository
)(implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) {

  implicit val timeout: Timeout = 10.seconds
  implicit val encoder: Encoder.AsObject[TransactionData] = deriveEncoder

  def getTransactionById(trxId: String): Route = {
    onComplete(
      for {
        maybeTransaction <- transactionDataRepository.findTransactionById(trxId)
        trx = maybeTransaction.map(_.asJson.spaces2).getOrElse(null.toString)
      } yield trx
    ) {
      case Failure(exception) => complete(InternalServerError, s"Exception in getting $trxId data, exception ${exception.getMessage}")
      case Success(value) => complete(value)
    }
  }
}
