package dev.lucasgrey.flow.indexer.monitoring

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.stream.alpakka.cassandra.{CassandraMetricsRegistry, CassandraSessionSettings}
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import com.softwaremill.macwire.wire
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import dev.lucasgrey.flow.indexer.FlowIndexerApplication.system
import kamon.metric._
import kamon.{Kamon, module}
import kamon.module.{CombinedReporter, Module, ModuleFactory}
import kamon.trace.Span
import com.codahale.metrics.{Meter, MetricRegistry}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class CassandraMonitor(
  implicit val executionContext: ExecutionContext,
  val materializer: Materializer,
  val actorSystem: ActorSystem[_]
) extends Module with CombinedReporter with StrictLogging {

  val sessionSettings = CassandraSessionSettings()
  val sessionSettingsPersistance = CassandraSessionSettings(configPath = "akka.persistence.cassandra")
  val cassandraSession: CassandraSession =
    CassandraSessionRegistry.get(system).sessionFor(sessionSettingsPersistance)
  val cassandraSessionPersistance: CassandraSession =
    CassandraSessionRegistry.get(system).sessionFor(sessionSettingsPersistance)

  logger.info("Initialized cassandra monitor")

  val bytesIn = Kamon.counter("cassandra.bytes-received")
  val bytesOut = Kamon.counter("cassandra.bytes-sent")

//  bytesIn.withTag("session", "akka.persistance.cassandra").autoUpdate()

  actorSystem.scheduler.scheduleAtFixedRate(
    initialDelay = 1.seconds,
    interval = 1.seconds
  )(
    () => {
      cassandraSessionPersistance.underlying().map(s => {
        s.getMetrics.map(met => {
          met.getRegistry.getMetrics.forEach((key, value) => {
            value match {
              case x: Meter => {
                logger.info(s"Cassandra monitor tick session, ${key} -> ${x.getCount}")
                if (key.contains("bytes-received")) {
                  bytesIn.withTag("session", "akka.persistance.cassandra").increment()
                } else if (key.contains("bytes-sent")) {

                }
              }
              case _ => logger.info(s"Cassandra monitor tick session, ${key} -> ${value.toString}")
            }
          })
        })
      })
    }
  )

  override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit = {
    snapshot match {
      case PeriodSnapshot(from, to, counters, gauges, histograms, timers, rangeSamplers) =>
        logger.info(s"from -> $from")
        logger.info(s"to -> $to")
        logger.info(s"counters -> $counters")
        logger.info(s"gauges -> $gauges")
        logger.info(s"histos -> $histograms")
        logger.info(s"timers -> $timers")
        logger.info(s"range -> $rangeSamplers")
    }
  }


  override def reportSpans(spans: scala.Seq[Span.Finished]): Unit = {
//    val metrics = cassandraSession.underlying().map(_.getMetrics)
    spans.foreach {
      case Span.Finished(id, trace, parentId, operationName, hasError, wasDelayed, from, to, kind,
      position, tags, metricTags, marks, links) =>
        logger.info(s"$id id -> $id")
        logger.info(s"$id trace -> $trace")
        logger.info(s"$id parentId -> $parentId")
        logger.info(s"$id operationName -> $operationName")
        logger.info(s"$id hasError -> $hasError")
        logger.info(s"$id wasDelayed -> $wasDelayed")
        logger.info(s"$id from -> $from")
        logger.info(s"$id to -> $to")
        logger.info(s"$id kind -> $kind")
        logger.info(s"$id position -> $position")
        logger.info(s"$id tags -> $tags")
        logger.info(s"$id metricTags -> $metricTags")
        logger.info(s"$id marks -> $marks")
        logger.info(s"$id links -> $links")
    }
  }

  override def stop(): Unit =
    logger.warn("Nothing to do before stopping")

  override def reconfigure(newConfig: Config): Unit =
    logger.warn("No config")
}

object CassandraMonitor extends StrictLogging {
//  class Factory extends ModuleFactory {
//    override def create(settings: ModuleFactory.Settings): Module = {
//      logger.info("Cassandra monitor setup")
//      new CassandraMonitor()
//    }
//  }
}