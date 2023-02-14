package dev.lucasgrey.flow.indexer

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import com.nftco.flow.sdk.{Flow, FlowAccessApi}
import com.softwaremill.macwire.wire
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.config.ConfigHolder
import dev.lucasgrey.flow.indexer.daemon.BlockMonitor
import kamon.Kamon

import scala.io.StdIn

object FlowIndexer extends App
  with ConfigHolder
  with StrictLogging {

  //Start Kamon
  Kamon.init()

  implicit val system = ActorSystem(Behaviors.empty, "flow-indexer")
  implicit val executionContext = system.executionContext

  lazy val accessAPI: FlowAccessApi = Flow.newAccessApi(
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

  val bindingFuture = Http().newServerAt("localhost", config.getString("http-port").toInt).bind(routes)

  logger.info(s"Serving HTTP in port : " + config.getString("http-port"))

  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
