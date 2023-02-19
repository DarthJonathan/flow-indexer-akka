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
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext

class BlockEventProcessor (
  dbConfig: DatabaseConfig[PostgresProfile],
  blockEventReadSideHandler: BlockEventReadSideHandler
) (implicit val actorSystem: ActorSystem[Nothing], val executionContext: ExecutionContext) {

  def sourcedProvider(tag: String) = {
    EventSourcedProvider
      .eventsByTag[BlockEvent](actorSystem, readJournalPluginId = CassandraReadJournal.Identifier, tag = tag)
  }

  def projection(tag: String) = {
    SlickProjection.atLeastOnce(
      projectionId = ProjectionId("BlockHeight", "blocks"),
      sourcedProvider(tag),
      dbConfig,
      handler = () => blockEventReadSideHandler)
  }

  ShardedDaemonProcess(actorSystem).init[ProjectionBehavior.Command] (
    name = "blocks-height",
    numberOfInstances = BlockActor.tags.size,
    behaviorFactory = (i: Int) => ProjectionBehavior(projection(BlockActor.tags(i))),
    stopMessage = ProjectionBehavior.Stop
  )
}
