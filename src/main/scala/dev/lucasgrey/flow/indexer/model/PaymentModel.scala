package dev.lucasgrey.flow.indexer.model

import dev.lucasgrey.flow.indexer.actors.payment.event.PaymentStateEnum
import dev.lucasgrey.flow.indexer.serializable.JsonSerializable

case class PaymentModel(amount: Double, paymentId: String, paymentStatus: PaymentStateEnum.paymentState, orderId: String, paymentLink: String) extends JsonSerializable