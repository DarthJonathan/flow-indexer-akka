package dev.lucasgrey.flow.indexer.dao.block

case class BlockInfoData (
  height: Long,
  blockId: String,
  isSealed: Boolean,
  collections: List[String]
)
