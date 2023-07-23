package com.datastax.oss.driver.internal.core.metrics

import com.datastax.oss.driver.api.core.config.{DefaultDriverOption, DriverExecutionProfile}
import com.datastax.oss.driver.api.core.context.DriverContext
import com.datastax.oss.driver.api.core.metadata.Node
import com.datastax.oss.driver.api.core.metrics.Metrics
import com.datastax.oss.driver.internal.core.context.InternalDriverContext
import com.typesafe.scalalogging.StrictLogging

import java.util.Optional

class KamonMetricsFactory(ctx: DriverContext) extends MetricsFactory with StrictLogging {

  var context: InternalDriverContext = ctx.asInstanceOf[InternalDriverContext]
  var sessionUpdater: SessionMetricUpdater = null

  val logPrefix: String = context.getSessionName
  val config: DriverExecutionProfile = context.getConfig.getDefaultProfile
  val enabledSessionMetrics = MetricPaths
    .parseSessionMetricPaths(config.getStringList(DefaultDriverOption.METRICS_SESSION_ENABLED), logPrefix)

  if (enabledSessionMetrics.isEmpty) {
    logger.debug("[{}] All metrics are disabled, Session.getMetrics will be empty", logPrefix)
    sessionUpdater = NoopSessionMetricUpdater.INSTANCE
  } else {
    sessionUpdater = new KamonSessionMetricUpdater(this.context, enabledSessionMetrics)
  }

  override def getMetrics: Optional[Metrics] = {
    Optional.empty()
  }

  override def getSessionUpdater: SessionMetricUpdater = {
    sessionUpdater
  }

  override def newNodeUpdater(node: Node): NodeMetricUpdater = {
    logger.error(s"Node metrics are not supported yet!")
    NoopNodeMetricUpdater.INSTANCE
  }


}
