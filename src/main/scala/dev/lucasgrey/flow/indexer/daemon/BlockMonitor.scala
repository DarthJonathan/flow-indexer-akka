package dev.lucasgrey.flow.indexer.daemon

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.stream.{Materializer, OverflowStrategy, ThrottleMode}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.BlockActor
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.RegisterBlock
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.utils.FlowClient

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class BlockMonitor(
  val flowClient: FlowClient
)(implicit val executionContext: ExecutionContext, val materializer: Materializer, val actorSystem: ActorSystem[Nothing])
  extends StrictLogging {

  import BlockMonitor._
  implicit val timeout: Timeout = 10.seconds

  def StartPolling(): Future[Done] = {
//    Source.unfoldAsync[(Long, Long), Long]((startHeight, startHeight+1)) {
//      case (currHeight, limitHeight) =>
//        for {
//          latestHeight <- if(currHeight + 1 >= limitHeight) {
//            logger.debug("Near current height, getting latest height again")
//            getLatestHeight
//          } else {
//            logger.debug("Way longer than current height, catching up!")
//            Future.successful(limitHeight)
//          }
//          res = if (currHeight >= latestHeight) {
//            logger.debug("Catches up with latest height waiting for 1s")
//            Thread.sleep(1000)
//            Some((currHeight, latestHeight), currHeight)
//          } else {
//            Some((currHeight + 1, latestHeight), currHeight)
//          }
//        } yield res
//    }
//      .buffer(1000, OverflowStrategy.backpressure)
      Source.single(startHeight)
      .throttle(50, 2000.millis, 10, ThrottleMode.Shaping)
      .mapAsync(2) { height =>
        for {
          blockHeader <- flowClient.getBlockHeaderByHeight(height)
          res <- ActorSystem(BlockActor.apply(blockHeader.height), "blockActor") ? (replyTo => RegisterBlock(blockHeader, replyTo))
        } yield res
        logger.info(s"got Height $height")
        Future.unit
      }
      .runWith(Sink.ignore)
  }

  private def getLatestHeight: Future[Long] = {
    flowClient.getLatestBlockHeader.map(_.height)
  }
}

object BlockMonitor extends ConfigHolder {
  val startHeight: Long = config.getString("start-height").toLong
}

