package dev.lucasgrey.flow.indexer.daemon

import akka.{Done, NotUsed}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.{Sink, Source}
import com.nftco.flow.sdk.{Flow, FlowAccessApi, FlowBlockHeader}
import dev.lucasgrey.flow.indexer.config.ConfigHolder

import scala.concurrent.{ExecutionContext, Future}

class BlockMonitor {

  import BlockMonitor._

  implicit val actorSystem: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "actor")
  implicit val executionContext: ExecutionContext = actorSystem.executionContext

  def StartPolling(): Future[Done] = {
    Source.unfold(startHeight) { currentHeight =>
      if (currentHeight == getLatestHeight) {
        Thread.sleep(100)
        None
      } else {
        Some(currentHeight + 1, currentHeight)
      }
    }
      .mapAsync(1) { height =>
        for {
          _ <- Future.unit
          blockHeader = getBlockHeader(height)
        } yield ()
      }
      .runWith(Sink.ignore)
  }

  private def getLatestHeight: Long = {
    accessAPI.getLatestBlockHeader.getHeight
  }

  private def getBlockHeader(blockHeight: Long): Future[FlowBlockHeader] = {
    Future.successful(accessAPI.getBlockHeaderByHeight(blockHeight))
  }
}

object BlockMonitor extends ConfigHolder {
  val startHeight: Long = config.getString("start-height").toLong
  val accessAPI: FlowAccessApi = Flow.newAccessApi("", 0)

}

