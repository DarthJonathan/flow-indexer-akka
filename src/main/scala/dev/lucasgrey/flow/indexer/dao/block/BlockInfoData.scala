package dev.lucasgrey.flow.indexer.dao.block

case class BlockInfoData (
  height: Long,
  blockId: String,
  parentBlockId: String,
  isSealed: Boolean,
  collectionsIds: List[String],
  transactionsIds: List[String]
)
