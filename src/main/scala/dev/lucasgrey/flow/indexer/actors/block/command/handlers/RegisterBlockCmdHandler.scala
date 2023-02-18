package dev.lucasgrey.flow.indexer.actors.block.command.handlers

import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.Effect
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.command.BlockCommands.RegisterBlock
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, NewBlockRegistered}
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, Initialized}
import dev.lucasgrey.flow.indexer.utils.FlowClient

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

class RegisterBlockCmdHandler(
  flowClient: FlowClient
)(implicit val execution: ExecutionContext) extends StrictLogging {

  def handleRegisterBlock(state: BlockState, cmd: RegisterBlock): Effect[BlockEvent, BlockState] = {
    if (state.isInstanceOf[Initialized]) {
      logger.info(s"Block height ${cmd.blockHeader.height} already exist, skipping")
      Effect.none
    } else {
      logger.info(s"Block height ${cmd.blockHeader.height} newly registered")

      val block = Await.result(flowClient.getBlockByHeight(cmd.blockHeader.height), 10.seconds)

      Effect.persist(NewBlockRegistered(
        height = cmd.blockHeader.height,
        blockHeader = cmd.blockHeader,
        block = block
      )).thenReply(cmd.replyTo)(_ => StatusReply.Ack)
    }
  }

}
