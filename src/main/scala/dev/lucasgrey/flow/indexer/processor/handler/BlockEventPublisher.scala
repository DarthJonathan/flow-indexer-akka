package dev.lucasgrey.flow.indexer.processor.handler

import akka.Done
import akka.kafka.ProducerMessage
import akka.kafka.scaladsl.SendProducer
import akka.projection.eventsourced.EventEnvelope
import akka.projection.slick.SlickHandler
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, ForceSyncBlockEvt, NewBlockRegisteredEvt}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import slick.dbio.DBIO
import io.circe.syntax._
import org.apache.kafka.clients.producer.ProducerRecord

class BlockEventPublisher (
  sendProducer: SendProducer[String, String]
) extends SlickHandler[EventEnvelope[BlockEvent]]
    with StrictLogging {

  implicit val newBlockFormat: Encoder.AsObject[NewBlockRegisteredEvt] = deriveEncoder
  implicit val resyncBlockFormat: Encoder.AsObject[ForceSyncBlockEvt] = deriveEncoder

  val blockRegisteredEventTopic = "syncer-block-registered-v1"
  val blockResyncEventTopic = "syncer-block-resync-v1"

  override def process(envelopes: EventEnvelope[BlockEvent]): DBIO[Done] = {
    envelopes.event match {
      case evt: NewBlockRegisteredEvt =>
        sendProducer.send(
          new ProducerRecord(
            blockRegisteredEventTopic,
            evt.block.id,
            evt.asJson.noSpaces
          )
        )
        logger.info(s"Published registered block with height ${evt.block.height}")
        DBIO.successful(Done)

      case evt: ForceSyncBlockEvt =>
        sendProducer.send(
          new ProducerRecord(
            blockResyncEventTopic,
            evt.block.id,
            evt.asJson.noSpaces
          )
        )
        logger.info(s"Published resync block with height ${evt.block.height}")
        DBIO.successful(Done)

      case _ =>
        logger.debug("Unsupported event received by block publisher, skipping event!")
        DBIO.successful(Done)
    }
  }
}