package dev.lucasgrey.flow.indexer.daemon

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.RegisterBlock
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.dao.height.BlockHeightRepository
import dev.lucasgrey.flow.indexer.model.FlowId
import dev.lucasgrey.flow.indexer.utils.{EntityRegistry, FlowClient}
import kamon.Kamon

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class BlockMonitor(
  val flowClient: FlowClient,
  val blockHeightRepository: BlockHeightRepository,
  val entityRegistry: EntityRegistry
)(implicit val executionContext: ExecutionContext,
  val materializer: Materializer,
  val actorSystem: ActorSystem[_]
) extends StrictLogging {

  import BlockMonitor._
  implicit val timeout: Timeout = 10.seconds

  def StartPolling(): Future[Done] = {
    Source.unfoldAsync[(Long, Long), Long]((startHeight, startHeight+1)) {
      case (currHeight, limitHeight) =>
        for {
          latestHeight <- if(currHeight + 1 >= limitHeight) {
            logger.debug("Near current height, getting latest height again")
            getLatestHeight
          } else {
            logger.debug("Way longer than current height, catching up!")
            Future.successful(limitHeight)
          }
          res = if (currHeight >= latestHeight) {
            logger.debug("Catches up with latest height waiting for 1s")
            Thread.sleep(1000)
            Some((currHeight, latestHeight), currHeight)
          } else {
            Some((currHeight + 1, latestHeight), currHeight)
          }
        } yield res
    }
      .buffer(100, OverflowStrategy.backpressure)
      .mapAsync(100) { height =>
        val span = Kamon.spanBuilder("process-height").start()
        for {
          isBlockExists <- blockHeightRepository.findHeightExists(height).map(_.isDefined)
          _ <- if (isBlockExists) {
            logger.info(s"got Height $height, exists, skipping!")
            Future.unit
          } else {
            for {
              block <- flowClient.getBlockByHeight(height)
              collectionList <- extractCollections(block.collectionGuarantee.map(_.collectionId))
              transactionList <- extractTransactions(collectionList.flatMap(_.transactionList).toList)
              _ = entityRegistry.getBlockActorByHeight(height.toString) ! RegisterBlock(block, transactionList.toList)
            } yield Future.unit
          }
          _ = span.finish()
        } yield Future.unit

      }
      .runWith(Sink.ignore)
  }

  private def extractCollections(collectionIdList: List[FlowId]) = {
    Source(collectionIdList)
      .mapAsync(4) { collectionId => flowClient.getTransactionList(collectionId) }
      .runWith(Sink.seq)
  }

  private def extractTransactions(transactionIds: List[FlowId]) = {
    Source(transactionIds)
      .mapAsync(4) { transactionId => {
        for {
          transaction <- flowClient.getTransactionById(transactionId)
          transactionResult <- flowClient.getTransactionResultById(transactionId)
          res = transaction.copy(transactionResult = Some(transactionResult))
        } yield res
      }}
      .runWith(Sink.seq)
  }

  private def getLatestHeight: Future[Long] = {
    flowClient.getLatestBlockHeader.map(_.height)
  }
}

object BlockMonitor extends ConfigHolder {
  val startHeight: Long = config.getString("start-height").toLong
}

