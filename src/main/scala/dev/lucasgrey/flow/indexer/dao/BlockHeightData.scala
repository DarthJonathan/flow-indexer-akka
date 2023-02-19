package dev.lucasgrey.flow.indexer.dao


import java.time.Instant

case class BlockHeightData(
  height: Long,
  timestamp: Instant
)