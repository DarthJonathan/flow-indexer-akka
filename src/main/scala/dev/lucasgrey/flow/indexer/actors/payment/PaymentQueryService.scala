package dev.lucasgrey.flow.indexer.actors.payment

import akka.actor._
import dev.lucasgrey.flow.indexer.actors.payment.event.PaymentStateEnum
import dev.lucasgrey.flow.indexer.model.PaymentModel

import java.util.UUID

object PaymentQueryService {
  case class PaymentQuery(paymentId: String)
}

class PaymentQueryService extends Actor with ActorLogging {

  override def receive: Receive = {
    case PaymentQueryService.PaymentQuery(paymentId) => {
      log.info(s"Received query for payment id ${paymentId}")
      sender() ! PaymentModel(0d, UUID.randomUUID().toString, PaymentStateEnum.CREATED, "", "")
    }
  }
}
