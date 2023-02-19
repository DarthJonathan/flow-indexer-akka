package dev.lucasgrey.flow.indexer

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.http.scaladsl.Http
import akka.management.scaladsl.AkkaManagement
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.BlockActor
import dev.lucasgrey.flow.indexer.actors.block.BlockActor.EntityKey
import dev.lucasgrey.flow.indexer.actors.block.command.handlers.RegisterBlockCmdHandler
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.daemon.BlockMonitor
import dev.lucasgrey.flow.indexer.impl.BlockController
import dev.lucasgrey.flow.indexer.processor.BlockEventProcessor
import dev.lucasgrey.flow.indexer.processor.handler.BlockEventReadSideHandler
import dev.lucasgrey.flow.indexer.utils.{EntityRegistry, FlowClient}
import dev.lucasgrey.flow.indexer.utils.FlowClientCreator.buildAPIFutureStubs
import kamon.Kamon
import org.onflow.protobuf.access.AccessAPIGrpc
import slick.basic.DatabaseConfig
import akka.http.scaladsl.server.Directives._
import dev.lucasgrey.flow.indexer.dao.height.BlockHeightRepository
import slick.jdbc.PostgresProfile

import scala.io.StdIn

object FlowIndexerApplication extends App
  with ConfigHolder
  with StrictLogging {

  implicit val system = ActorSystem(Behaviors.empty, "flow-indexer")
  implicit val executionContext = system.executionContext

  Kamon.init()
  AkkaManagement(system).start()

  //Repository
  lazy val blockHeightRepository = wire[BlockHeightRepository]

  //Migrations
  blockHeightRepository.createTable()

  //Command Handler
  lazy val registerBlockCmdHandler: RegisterBlockCmdHandler = wire[RegisterBlockCmdHandler]

  //Flow Integration
  lazy val accessAPI: AccessAPIGrpc.AccessAPIFutureStub = buildAPIFutureStubs(
    config.getString("flow.access-node.host"),
    config.getInt("flow.access-node.port")
  )
  lazy val flowClient: FlowClient = wire[FlowClient]

  //Start Polling
  lazy val blockMonitor = wire[BlockMonitor]
  blockMonitor.StartPolling()

  //Read side Connections
  lazy val dbConfig: DatabaseConfig[PostgresProfile] = DatabaseConfig.forConfig("akka.projection.slick", system.settings.config)
  lazy val database = dbConfig.db

  //Event Processor
  wire[BlockEventProcessor]

  //Projections
  lazy val blockEventReadSideHandler = wire[BlockEventReadSideHandler]

  //Registry
  lazy val sharding = ClusterSharding(system)

  sharding.init(Entity(EntityKey) { entityContext =>
    val i = math.abs(entityContext.entityId.hashCode % BlockActor.tags.size)
    val selectedTag = BlockActor.tags(i)
    BlockActor(entityContext.entityId, selectedTag)
  })

  lazy val entityRegistry = new EntityRegistry(sharding)

  //Controllers
  lazy val blockController: BlockController = wire[BlockController]

  val routes = concat(
    (get & path("inspect" / "block" / IntNumber)) (height => blockController.inspectBlockEntity(height))
  )

  val httpPort = config.getInt("http-port")
  val bindingFut = Http()
    .newServerAt("localhost", httpPort)
    .bind(routes)

  logger.info(s"Serving HTTP in port : $httpPort")

  StdIn.readLine()
  bindingFut
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
