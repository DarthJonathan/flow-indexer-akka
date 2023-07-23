package com.datastax.oss.driver.internal.core.metrics

import com.datastax.oss.driver.api.core.metrics.SessionMetric
import com.datastax.oss.driver.internal.core.context.InternalDriverContext

import java.util

class KamonSessionMetricUpdater(context: InternalDriverContext, sessionMetrics: util.Set[SessionMetric])
  extends KamonMetricUpdater(context, sessionMetrics) with SessionMetricUpdater