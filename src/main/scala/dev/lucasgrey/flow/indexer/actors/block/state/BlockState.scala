package dev.lucasgrey.flow.indexer.actors.block.state

import dev.lucasgrey.flow.indexer.serializable.JsonSerializable

case class BlockState(
  BlockHeight: Long
) extends JsonSerializable