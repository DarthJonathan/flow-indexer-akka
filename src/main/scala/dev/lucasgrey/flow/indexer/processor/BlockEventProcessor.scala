package dev.lucasgrey.flow.indexer.processor

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.projection.{ProjectionBehavior, ProjectionId}
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.slick.SlickProjection
import dev.lucasgrey.flow.indexer.actors.block.event.BlockEvents.BlockEvent
import dev.lucasgrey.flow.indexer.processor.handler.BlockEventReadSideHandler
import dev.lucasgrey.flow.indexer.actors.block.BlockActor
import dev.lucasgrey.flow.indexer.utils.PostgresProfileExtended
import slick.basic.DatabaseConfig

import scala.concurrent.ExecutionContext

class BlockEventProcessor (
  dbConfig: DatabaseConfig[PostgresProfileExtended],
  blockEventReadSideHandler: BlockEventReadSideHandler
) (implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) {

  val projectionId = "BlockHeight"
  val projectionKey = "blocks"
  val READ_SIDE_ID = "blocks-height"

  def sourcedProvider(tag: String) = {
    EventSourcedProvider
      .eventsByTag[BlockEvent](actorSystem, readJournalPluginId = CassandraReadJournal.Identifier, tag = tag)
  }

  def projection(tag: String) = {
    SlickProjection.atLeastOnce(
      projectionId = ProjectionId(projectionId, projectionKey),
      sourcedProvider(tag),
      dbConfig,
      handler = () => blockEventReadSideHandler)
  }

  ShardedDaemonProcess(actorSystem).init[ProjectionBehavior.Command] (
    name = READ_SIDE_ID,
    numberOfInstances = BlockActor.tags.size,
    behaviorFactory = (i: Int) => ProjectionBehavior(projection(BlockActor.tags(i))),
    stopMessage = ProjectionBehavior.Stop
  )
}
