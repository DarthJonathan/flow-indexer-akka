package dev.lucasgrey.flow.indexer.actors.payment.command

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.Effect
import com.stripe.Stripe
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.model.PaymentModel
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import dev.lucasgrey.flow.indexer.utils.StripeUtilities

import scala.collection.immutable.HashMap
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object CommandController extends ConfigHolder {

  sealed trait Command

  final case class StartPayment(orderId: String, paymentAmount: Float) extends Command

  final case class UpdatePayment(paymentStatus:Integer, replyTo: ActorRef[StatusReply[PaymentModel]]) extends Command

  final case class GetPaymentInfo(replyTo: ActorRef[StatusReply[PaymentModel]]) extends Command

  type CommandHandler = (PaymentState, Command) => Effect[Event, PaymentState]

  val commandHandler: CommandHandler = (state, command) => {
    command match {
      case StartPayment(orderId, paymentAmount) =>
        //Generate URL
        val paymentSession = for {
          paymentSession <- Future.successful(null)
        } yield paymentSession
        val result = Await.result(paymentSession, 10.seconds)
        val additionalInfo: Map[Any, Any] = HashMap("paymentLinkDetails" -> result)
        Effect.persist(AcceptPayment(paymentAmount, state.paymentId, orderId, result, additionalInfo))
      case cmd: UpdatePayment =>
        Effect.persist(PaymentInProcess(state.paymentId))
          .thenRun(updatedInfo => cmd.replyTo ! StatusReply.Success(PaymentModel(
            amount = updatedInfo.paymentAmount,
            paymentId = updatedInfo.paymentId,
            paymentStatus = updatedInfo.paymentState,
            orderId = updatedInfo.orderId,
            paymentLink = updatedInfo.paymentUrl
          )
        ))
      case cmd: GetPaymentInfo =>
        cmd.replyTo ! StatusReply.Success(PaymentModel(
          amount = state.paymentAmount,
          paymentId = state.paymentId,
          paymentStatus = state.paymentState,
          orderId = state.orderId,
          paymentLink = state.paymentUrl
        ))
        Effect.none
    }
  }
}
