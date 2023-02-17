package dev.lucasgrey.flow.indexer

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.actors.block.command.handlers.RegisterBlockCmdHandler
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.daemon.BlockMonitor
import dev.lucasgrey.flow.indexer.utils.FlowClientCreator.buildAPIFutureStubs
import kamon.Kamon
import org.onflow.protobuf.access.AccessAPIGrpc

import scala.io.StdIn

object FlowIndexerApplication extends App
  with ConfigHolder
  with StrictLogging {

  Kamon.init()

  implicit val system = ActorSystem(Behaviors.empty, "flow-indexer")
  implicit val executionContext = system.executionContext

  //Command Handler
  lazy val registerBlockCmdHandler: RegisterBlockCmdHandler = wire[RegisterBlockCmdHandler]

  //Flow Integration
  lazy val accessAPI: AccessAPIGrpc.AccessAPIFutureStub = buildAPIFutureStubs(
    config.getString("flow.access-node.host"),
    config.getInt("flow.access-node.port")
  )

  //Start Polling
  lazy val blockMonitor = wire[BlockMonitor]
  blockMonitor.StartPolling()

  val routes =
    path("test") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<H1>Hello Test</H1>"))
      }
    }

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
