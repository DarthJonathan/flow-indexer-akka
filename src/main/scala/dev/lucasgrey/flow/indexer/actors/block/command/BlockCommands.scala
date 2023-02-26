package dev.lucasgrey.flow.indexer.actors.block.command

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.BlockEvent
import dev.lucasgrey.flow.indexer.actors.block.state.{BlockState, Initialized}
import dev.lucasgrey.flow.indexer.FlowIndexerApplication.registerBlockCmdHandler.handleRegisterBlock
import dev.lucasgrey.flow.indexer.model.{FlowBlock, FlowBlockHeader, FlowTransaction}
import dev.lucasgrey.flow.indexer.utils.ActorInspection.{InspectCommandWrapper, InspectableEntity}

object BlockCommands {

  sealed trait BlockCommand extends InspectableEntity

  final case class BlockExistsCmd(replyTo: ActorRef[Boolean]) extends BlockCommand
  final case class RegisterBlock(block: FlowBlock, transactionList: List[FlowTransaction]) extends BlockCommand

  type BlockCommandHandler = (BlockState, InspectableEntity) => Effect[BlockEvent, BlockState]

  val blockCommandHandler: BlockCommandHandler = InspectCommandWrapper((state, cmd) => {
      cmd match {
        case cmd: BlockExistsCmd => Effect.reply(cmd.replyTo)(state.isInstanceOf[Initialized])
        case cmd: RegisterBlock => handleRegisterBlock(state, cmd)
        case _ => throw new RuntimeException("Unhandled block command!")
      }
    })
}
