package dev.lucasgrey.flow.indexer.daemon

import akka.{Done, NotUsed}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.nftco.flow.sdk.{Flow, FlowAccessApi, FlowBlockHeader}
import dev.lucasgrey.flow.indexer.actors.block.BlockActor
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.{BlockCommand, RegisterBlock}
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import kotlin.reflect.jvm.internal.impl.resolve.scopes.MemberScope.Empty

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class BlockMonitor(
  val accessAPI: FlowAccessApi
) {

  import BlockMonitor._

  implicit val actorSystem: ActorSystem[Empty] = ActorSystem(Behaviors.empty, "actor")
  implicit val executionContext: ExecutionContext = actorSystem.executionContext
  implicit val timeout: Timeout = 10.seconds

  def StartPolling(): Future[Done] = {
    Source.unfold(startHeight) { currentHeight =>
      if (currentHeight >= getLatestHeight) {
        Thread.sleep(100)
        None
      } else {
        Some(currentHeight + 1, currentHeight)
      }
    }
      .mapAsync(1) { height =>
        for {
          blockHeader <- getBlockHeader(height)
          res = ActorSystem(BlockActor.apply(blockHeader.getHeight), "blockActor") ! RegisterBlock(blockHeader)
        } yield res
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
}

