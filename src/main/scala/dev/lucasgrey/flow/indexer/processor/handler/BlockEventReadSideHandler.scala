package dev.lucasgrey.flow.indexer.processor.handler

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.slick.SlickHandler
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.{BlockEvent, ForceSyncBlockEvt, NewBlockRegisteredEvt}
import dev.lucasgrey.flow.indexer.dao.height.{BlockHeightData, BlockHeightRepository}
import slick.dbio.DBIO

import java.time.Instant

class BlockEventReadSideHandler(
  blockHeightRepository: BlockHeightRepository
) extends SlickHandler[EventEnvelope[BlockEvent]] with StrictLogging {
  override def process(envelopes: EventEnvelope[BlockEvent]): DBIO[Done] = {
    envelopes.event match {
      case NewBlockRegisteredEvt(height, _, _) =>
        logger.info(s"Read side processor received height to be stored $height")
        blockHeightRepository.upsert(
          BlockHeightData(
            height = height,
            timestamp = Instant.now()
          )
        )

      case ForceSyncBlockEvt(height, _, _) =>
        logger.info(s"Read side processor received height to be stored $height - force sync block!")
        blockHeightRepository.upsert(
          BlockHeightData(
            height = height,
            timestamp = Instant.now()
          )
        )

      case _ =>
        logger.debug("Unsupported event received by block read side handler, skipping event!")
        DBIO.successful(Done)
    }
  }
}