package com.datastax.oss.driver.internal.core.metrics

import com.datastax.oss.driver.api.core.metrics.SessionMetric
import com.datastax.oss.driver.internal.core.context.InternalDriverContext
import com.typesafe.scalalogging.StrictLogging
import kamon.Kamon
import kamon.metric.{Counter, Histogram, Timer}

import java.util
import java.util.concurrent.TimeUnit

class KamonMetricUpdater(
  context: InternalDriverContext,
  sessionMetrics: util.Set[SessionMetric]
) extends AbstractMetricUpdater(context, sessionMetrics) with StrictLogging {

  import KamonMetricUpdater._

  override def clearMetrics(): Unit = {
    logger.info("clearing metrics not supported in kamon!")
  }

  override def incrementCounter(metric: SessionMetric, profileName: String, amount: Long): Unit =
    if (isEnabled(metric, profileName)) getOrCreateCounterFor(metric).increment(amount)

  override def updateHistogram(metric: SessionMetric, profileName: String, value: Long): Unit =
    if (isEnabled(metric, profileName)) getOrCreateDistributionSummaryFor(metric).record(value)

  override def markMeter(metric: SessionMetric, profileName: String, amount: Long): Unit =
    if (isEnabled(metric, profileName)) getOrCreateCounterFor(metric).increment(amount)

  override def updateTimer(metric: SessionMetric, profileName: String, duration: Long, unit: TimeUnit): Unit =
    if (isEnabled(metric, profileName)) getOrCreateTimerFor(metric).record(duration, unit)

  private def getOrCreateCounterFor(metric: SessionMetric): Counter = {
    Kamon.counter(metricName).withTag(tagName, metric.getPath)
  }

  private def getOrCreateTimerFor(metric: SessionMetric): Timer = {
    Kamon.timer(metricName).withTag(tagName, metric.getPath)
  }

  private def getOrCreateDistributionSummaryFor(metric: SessionMetric): Histogram = {
    Kamon.histogram(metricName).withTag(tagName, metric.getPath)
  }
}

object KamonMetricUpdater {
  private val metricName = "cassandra_metrics"
  private val tagName = "metric"
}
