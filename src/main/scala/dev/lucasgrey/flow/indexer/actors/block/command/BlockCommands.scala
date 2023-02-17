package dev.lucasgrey.flow.indexer.actors.block.command

import akka.Done
import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.Effect
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.BlockEvent
import dev.lucasgrey.flow.indexer.actors.block.state.BlockState
import dev.lucasgrey.flow.indexer.FlowIndexerApplication.registerBlockCmdHandler.handleRegisterBlock
import dev.lucasgrey.flow.indexer.model.FlowBlockHeader

object BlockCommands {
  sealed trait BlockCommand
  final case class GetBlockHeader(replyTo: ActorRef[StatusReply[FlowBlockHeader]]) extends BlockCommand
  final case class RegisterBlock(blockHeader: FlowBlockHeader, replyTo: ActorRef[StatusReply[Done]]) extends BlockCommand

  type BlockCommandHandler = (BlockState, BlockCommand) => Effect[BlockEvent, BlockState]

  val blockCommandHandler: BlockCommandHandler = (state, cmd) => {
    cmd match {
      case cmd: RegisterBlock => handleRegisterBlock(state, cmd)
      case _ => ???
    }
  }
}
