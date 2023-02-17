package dev.lucasgrey.flow.indexer.actors.block.command

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.Effect
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.BlockEvent
import dev.lucasgrey.flow.indexer.actors.block.state.BlockState
import com.nftco.flow.sdk.{Flow, FlowAccessApi, FlowBlockHeader}
import dev.lucasgrey.flow.indexer.FlowIndexerApplication.registerBlockCmdHandler.handleRegisterBlock

object BlockCommands {
  sealed trait BlockCommand
  final case class GetBlockHeader(replyTo: ActorRef[StatusReply[FlowBlockHeader]]) extends BlockCommand
  final case class RegisterBlock(blockHeader: FlowBlockHeader) extends BlockCommand

  type BlockCommandHandler = (BlockState, BlockCommand) => Effect[BlockEvent, BlockState]

  val blockCommandHandler: BlockCommandHandler = (state, cmd) => {
    cmd match {
      case RegisterBlock(blockHeader) => handleRegisterBlock(state, blockHeader)
      case _ => ???
    }
  }
}
