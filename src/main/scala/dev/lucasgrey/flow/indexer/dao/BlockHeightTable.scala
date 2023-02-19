package dev.lucasgrey.flow.indexer.dao

import slick.jdbc.PostgresProfile.profile.api._
import slick.lifted.Tag

import java.time.Instant


class BlockHeightTable(tag: Tag) extends Table[BlockHeightData] (tag, Some("indexer"),"registered_heights") {
  def height = column[Long] ("height", O.PrimaryKey)

  def timestamp = column[Instant]("timestamp")

  override def * = (height, timestamp).shaped <> (
    {
      case (height, timestamp) => BlockHeightData(height, timestamp)
    },
    {
      data: BlockHeightData => Some(
        data.height, data.timestamp
      )
    }
  )
}