package dev.lucasgrey.flow.indexer.monitoring

import akka.actor.typed.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import kamon.metric._
import kamon.module
import kamon.module.{CombinedReporter, ModuleFactory}
import kamon.trace.Span

import scala.concurrent.ExecutionContext

class CassandraMonitor(
  implicit val executionContext: ExecutionContext,
  val materializer: Materializer,
  val actorSystem: ActorSystem[_]
) extends CombinedReporter with StrictLogging{

  val sessionSettings = CassandraSessionSettings()
  implicit val cassandraSession: CassandraSession =
    CassandraSessionRegistry.get(actorSystem).sessionFor(sessionSettings)

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
    val metrics = cassandraSession.underlying().map(_.getMetrics)
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


  //  def inflight(host: String): RangeSampler =
  //    Kamon.rangeSampler("cassandra.client-inflight").refine("target", host)
  //
  //  def inflightDriver(host: String): Histogram =
  //    Kamon.histogram("cassandra.client-inflight-driver").refine("target", host)
  //
  //  def queryDuration: Histogram =
  //    Kamon.histogram("cassandra.query-duration", MeasurementUnit.time.nanoseconds)
  //
  //  def queryCount: Counter =
  //    Kamon.counter("cassandra.query-count")
  //
  //  def connections(host: String): Histogram =
  //    Kamon.histogram("cassandra.connection-pool-size").refine("target", host)
  //
  //  def trashedConnections(host: String): Histogram =
  //    Kamon.histogram("cassandra.trashed-connections").refine("target", host)
  //
  //  def recordQueryDuration(start: Long, end: Long): Unit = {
  //    queryDuration.record(end - start)
  //    queryCount.increment(1)
  //    inflight("ALL").decrement()
  //  }
}

object CassandraMonitor {
  class Factory extends ModuleFactory {
    override def create(settings: ModuleFactory.Settings): module.Module = ???
  }
}