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
import dev.lucasgrey.flow.indexer.actors.block.command.handlers.{ForceSyncBlockCmdHandler, RegisterBlockCmdHandler}
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.daemon.BlockMonitor
import dev.lucasgrey.flow.indexer.impl.{BlockController, InspectEntityController}
import dev.lucasgrey.flow.indexer.processor.{BlockEventProcessor, BlockEventPublisherProcessor, TransactionBlockEventProcessor}
import dev.lucasgrey.flow.indexer.processor.handler.{BlockEventPublisher, BlockEventReadSideHandler, TransactionEventReadSideHandler}
import dev.lucasgrey.flow.indexer.utils.{EntityRegistry, FlowClient, FlowHelper, PostgresProfileExtended}
import dev.lucasgrey.flow.indexer.utils.FlowClientCreator.buildAPIFutureStubs
import kamon.Kamon
import org.onflow.protobuf.access.AccessAPIGrpc
import slick.basic.DatabaseConfig
import akka.http.scaladsl.server.Directives._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.SendProducer
import dev.lucasgrey.flow.indexer.dao.height.BlockHeightRepository
import dev.lucasgrey.flow.indexer.dao.transaction.TransactionDataRepository
import org.apache.kafka.common.serialization.StringSerializer

import scala.io.StdIn

object FlowIndexerApplication extends App
  with ConfigHolder
  with StrictLogging {

  implicit val system = ActorSystem(Behaviors.empty, "flow-indexer")
  implicit val executionContext = system.executionContext

  Kamon.init()
  AkkaManagement(system).start()

  //Helpers
  lazy val flowHelper = wire[FlowHelper]

  //Repository
  lazy val blockHeightRepository = wire[BlockHeightRepository]
  lazy val transactionRepository = wire[TransactionDataRepository]

  //Migrations
  blockHeightRepository.createTable()
  transactionRepository.createTable()

  //Command Handler
  lazy val registerBlockCmdHandler = wire[RegisterBlockCmdHandler]
  lazy val forceSyncBlockCmdHandler = wire[ForceSyncBlockCmdHandler]

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
  lazy val dbConfig = DatabaseConfig.forConfig[PostgresProfileExtended] (
    "akka.projection.slick",
    system.settings.config
  )
  lazy val database = dbConfig.db

  //Kafka producer
  lazy val kafkaConfig = system.settings.config.getConfig("akka.kafka.producer")
  lazy val producerSettings =
    ProducerSettings(kafkaConfig, new StringSerializer, new StringSerializer)
      .withBootstrapServers(config.getString("akka.kafka.producer.bootstrap"))
  lazy val sendProducer = SendProducer(producerSettings)

  //Projections
  lazy val blockEventReadSideHandler = wire[BlockEventReadSideHandler]
  lazy val transactionReadSideHandler = wire[TransactionEventReadSideHandler]
  lazy val blockEventPublisher = wire[BlockEventPublisher]

  //Event Processor
  wire[BlockEventProcessor]
  wire[TransactionBlockEventProcessor]
  wire[BlockEventPublisherProcessor]

  //Registry
  lazy val sharding = ClusterSharding(system)

  sharding.init(Entity(EntityKey) { entityContext =>
    val i = math.abs(entityContext.entityId.hashCode % BlockActor.tags.size)
    val selectedTag = BlockActor.tags(i)
    BlockActor(entityContext.entityId, selectedTag)
  })

  lazy val entityRegistry = new EntityRegistry(sharding)

  //Controllers
  lazy val inspectEntityController: InspectEntityController = wire[InspectEntityController]
  lazy val blockController: BlockController = wire[BlockController]

  val routes = concat(
    (get & path("inspect" / Segment / Segment)) { case (entityName, entityId) => inspectEntityController.inspectEntity(entityName, entityId) },
    (get & path("transaction" / Segment)) { transactionId => blockController.getTransactionById(transactionId) }
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
