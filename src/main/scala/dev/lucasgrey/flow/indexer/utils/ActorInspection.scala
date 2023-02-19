package dev.lucasgrey.flow.indexer.utils

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.Effect
import dev.lucasgrey.flow.indexer.serializable.JsonSerializable
import io.circe.Encoder
import io.circe.syntax._
import io.circe.generic.auto._

object ActorInspection {

  type CommandHandler[T, K] = (T, InspectableEntity) => Effect[K, T]

  trait InspectableEntity extends JsonSerializable

  case class InspectEntityCmd (replyTo: ActorRef[StatusReply[String]]) extends InspectableEntity

  def InspectCommandWrapper[T, K](commandHandler: CommandHandler[T, K])(implicit encoder: Encoder[T]): CommandHandler[T, K] = {

    def handleInspectEntity(state: T, reply: ActorRef[StatusReply[String]]): Effect[K, T] = {
      Effect.reply(reply)(StatusReply.success(state.asJson.spaces2))
    }

    val blockCommandHandler: CommandHandler[T, K]= (state, cmd) => {
      cmd match {
        case x: InspectEntityCmd => handleInspectEntity(state, x.replyTo)
        case _ => commandHandler(state, cmd)
      }
    }

    blockCommandHandler
  }
}
