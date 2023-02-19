package dev.lucasgrey.flow.indexer.dao.height

import java.time.Instant

case class BlockHeightData(
  height: Long,
  timestamp: Instant
)