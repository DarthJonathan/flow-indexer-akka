package dev.lucasgrey.flow.indexer.actors.payment

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, RetentionCriteria}
import dev.lucasgrey.flow.indexer.actors.payment.command.CommandController.{Command, commandHandler}
import dev.lucasgrey.flow.indexer.actors.payment.event.PaymentEventHandler.{Event, PaymentState, eventHandler}
import dev.lucasgrey.flow.indexer.actors.payment.event.PaymentStateEnum

import scala.collection.immutable.HashMap
import scala.concurrent.duration.DurationInt

object PaymentActor {
  def apply(paymentId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, PaymentState](
      PersistenceId("blockHeight", paymentId),
      PaymentState(0, paymentId, PaymentStateEnum.CREATED, orderId = "", paymentUrl = "", additionalInfo = new HashMap[Any, Any]()),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 3))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(200.millis, 5.seconds, 0.1))
  }
}
