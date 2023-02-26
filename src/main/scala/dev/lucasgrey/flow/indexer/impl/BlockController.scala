package dev.lucasgrey.flow.indexer.impl

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.{ForceSyncBlock, RegisterBlock}
import dev.lucasgrey.flow.indexer.dao.height.BlockHeightRepository
import dev.lucasgrey.flow.indexer.dao.transaction.{TransactionData, TransactionDataRepository}
import dev.lucasgrey.flow.indexer.utils.{EntityRegistry, FlowClient, FlowHelper}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

class BlockController(
  entityRegistry: EntityRegistry,
  transactionDataRepository: TransactionDataRepository,
  blockHeightRepository: BlockHeightRepository,
  flowClient: FlowClient,
  flowHelper: FlowHelper
)(implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) extends StrictLogging{

  implicit val timeout: Timeout = 10.seconds
  implicit val encoder: Encoder.AsObject[TransactionData] = deriveEncoder

  def syncBlock(height: Long, forceSync: Boolean = false): Route = {
    onComplete(
      for {
        isBlockExists <- blockHeightRepository.findHeightExists(height).map(_.isDefined)
        _ <- if (isBlockExists && !forceSync) {
          logger.info(s"got Height $height, exists, skipping!")
          Future.unit
        } else {
          for {
            block <- flowClient.getBlockByHeight(height)
            collectionList <- flowHelper.extractCollections(block.collectionGuarantee.map(_.collectionId))
            transactionList <- flowHelper.extractTransactions(collectionList.flatMap(_.transactionList).toList)
            _ = entityRegistry.getBlockActorByHeight(height.toString) ! ForceSyncBlock(block, transactionList.toList)
          } yield Future.unit
        }
      } yield Future.unit
    ) {
      case Failure(exception) => complete(InternalServerError, s"Sync block height $height with force sync $forceSync failed, ex : ${exception.getMessage}")
      case Success(value) => complete(s"Sync block height $height with force sync $forceSync completed")
    }
  }

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
