package dev.lucasgrey.flow.indexer.actors.payment.event

import akka.event.slf4j.Logger
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.serializable.JsonSerializable
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PaymentStateEnum extends Enumeration {
  type paymentState = Value
  val CREATED, WAITING_PAYMENT, PAID, FAILED, CHALLENGE = Value
}

object PaymentEventHandler extends ConfigHolder{

  case class PaymentState(paymentAmount: Float,
                          paymentId: String,
                          paymentState: PaymentStateEnum.paymentState,
                          orderId: String,
                          paymentUrl: String,
                          additionalInfo: Map[Any,Any]
                         ) extends JsonSerializable

  sealed trait Event extends JsonSerializable

  val LOGGER = LoggerFactory.getLogger(PaymentEventHandler.toString)

  case class AcceptPayment(paymentAmount: Float, paymentId: String, orderId: String, paymentUrl: String, additionalInfo: Map[Any, Any]) extends Event

  case class PaymentInProcess(paymentId: String) extends Event

  case class PaymentSuccess(paymentId: String) extends Event

  case class PaymentFailure(paymentId: String) extends Event

  type EventHandler = (PaymentState, Event) => PaymentState

  val eventHandler: EventHandler = (state, event) => {
    event match {
      case AcceptPayment(paymentAmount, paymentId, orderId, paymentUrl, additionalInfo) =>
        LOGGER.info(s"Received accept payment !")
        for {
          paymentState <- Future(1+2)
        } yield paymentState

        PaymentState (paymentAmount = paymentAmount, paymentId = paymentId, PaymentStateEnum.CREATED, orderId = orderId, paymentUrl = paymentUrl, additionalInfo = additionalInfo)
      case PaymentInProcess(paymentId) =>
        PaymentState(state.paymentAmount, paymentId = paymentId, PaymentStateEnum.WAITING_PAYMENT, orderId = state.orderId, paymentUrl = state.paymentUrl, additionalInfo = state.additionalInfo)
      case PaymentSuccess(paymentId) =>
        PaymentState(state.paymentAmount, paymentId = paymentId, PaymentStateEnum.PAID, orderId = state.orderId, paymentUrl = state.paymentUrl, additionalInfo = state.additionalInfo)
      case PaymentFailure(paymentId) =>
        PaymentState(state.paymentAmount, paymentId = paymentId, PaymentStateEnum.FAILED, orderId = state.orderId, paymentUrl = state.paymentUrl, additionalInfo = state.additionalInfo)
    }
  }
}
