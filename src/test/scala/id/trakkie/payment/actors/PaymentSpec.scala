package dev.lucasgrey.flow.indexer.actors

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import dev.lucasgrey.flow.indexer.actors.payment.PaymentActor
import dev.lucasgrey.flow.indexer.model.PaymentModel
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID

class PaymentSpec extends ScalaTestWithActorTestKit(s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """) with AnyWordSpecLike with Matchers{

  "The Payment Service" should {

    "start payment" in {
      val paymentActor = testKit.spawn(PaymentActor(UUID.randomUUID().toString))
      val probe = testKit.createTestProbe[StatusReply[PaymentModel]]
      paymentActor ! StartPayment(UUID.randomUUID().toString, 1)
      val res = probe.expectMessageType[StatusReply[PaymentModel]]

      res.isSuccess shouldBe true
      res.getValue.paymentStatus shouldBe 1
      res.getValue.amount shouldBe 0
      res.getValue.paymentId should !== (null)
    }

//    "update payment"

  }

}
