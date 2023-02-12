package dev.lucasgrey.flow.indexer.impl

import akka.NotUsed
import akka.actor.typed.javadsl.Behaviors

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import dev.lucasgrey.flow.indexer.actors.payment.PaymentActor
import org.slf4j.LoggerFactory

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

//class PaymentServiceImpl(implicit mat: Materializer) extends PaymentService {
//
//  private val LOGGER = LoggerFactory.getLogger(PaymentService.getClass)
//
//  implicit val timeout: Timeout = 5.seconds
//
//  implicit val system = ActorSystem(Behaviors.empty, "trakkie-payment-grpc")
//  implicit val executionContext = system.executionContext
//
//  override def queryPaymentResult(in: PaymentServiceRequest): Future[PaymentServiceResponse] = {
//    val paymentActor: ActorSystem[Command] = ActorSystem(PaymentActor.apply(in.paymentId), "paymentActor")
//    (paymentActor ? (replyTo => GetPaymentInfo(replyTo)))
//      .map(value => {
//        val paymentModel = value.getValue
//        if (paymentModel.orderId == null || paymentModel.orderId.equals("")) {
//          PaymentServiceResponse()
//        } else {
//          PaymentServiceResponse(success = true, paymentId = paymentModel.paymentId, orderId = paymentModel.orderId, paymentAmount = paymentModel.amount.toFloat, paymentUrl = paymentModel.paymentLink)
//        }
//      })
//  }
//
//  override def createPayment(in: PaymentServiceRequest): Future[PaymentServiceResponse] = {
//    val paymentId: String = UUID.randomUUID().toString
//    val paymentActor: ActorSystem[Command] = ActorSystem(PaymentActor.apply(paymentId), "paymentActor")
//
//    paymentActor ! StartPayment(in.orderId, in.paymentAmount)
//
//    Future.apply(PaymentServiceResponse(success = true, paymentId = paymentId))
//  }
//
//  override def finishPayment(in: PaymentServiceRequest): Future[PaymentServiceResponse] = {
//    (ActorSystem(PaymentActor.apply(in.paymentId), "paymentActor") ? (replyTo => UpdatePayment(2, replyTo)))
//      .map(value =>
//        if (value.isSuccess) {
//          PaymentServiceResponse(
//            paymentId = value.getValue.paymentId, success = value.isSuccess, orderId = value.getValue.orderId, paymentAmount = value.getValue.amount.toFloat, paymentUrl = value.getValue.paymentLink
//          )
//        }else {
//          PaymentServiceResponse(
//            success = value.isSuccess
//          )
//        }
//      )
//  }
//
//  override def cancelPayment(in: PaymentServiceRequest): Future[PaymentServiceResponse] = {
//    (ActorSystem(PaymentActor.apply(in.paymentId), "paymentActor") ? (replyTo => UpdatePayment(2, replyTo)))
//      .map(value =>
//        if (value.isSuccess) {
//          PaymentServiceResponse(
//            paymentId = value.getValue.paymentId, success = value.isSuccess, orderId = value.getValue.orderId, paymentAmount = value.getValue.amount.toFloat, paymentUrl = value.getValue.paymentLink
//          )
//        }else {
//          PaymentServiceResponse(
//            success = value.isSuccess
//          )
//        }
//      )
//  }
//
//  override def streamPayments(in: Source[PaymentServiceRequest, NotUsed]): Source[PaymentServiceResponse, NotUsed] = {
//    in.map(req => PaymentServiceResponse(success = true, paymentId = req.paymentId))
//  }
//
//}
