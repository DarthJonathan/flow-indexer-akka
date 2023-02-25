package dev.lucasgrey.flow.indexer.dao.height

import akka.Done
import dev.lucasgrey.flow.indexer.utils.PostgresProfileExtended
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

class BlockHeightRepository (
  val dbConfig: DatabaseConfig[PostgresProfileExtended],
)(implicit val executionContext: ExecutionContext) {

  import dbConfig.profile.api._

  private val blockHeightTable = TableQuery[BlockHeightTable]

  def upsert(blockHeightData: BlockHeightData) = {
    blockHeightTable.insertOrUpdate(blockHeightData).map(_ => Done)
  }

  def findHeightExists(blockHeight: Long): Future[Option[BlockHeightData]] = {
    dbConfig.db.run(
      blockHeightTable.filter(_.height === blockHeight)
        .take(1)
        .result
        .headOption
    )
  }

  def createTable(): Future[Unit] =
    dbConfig.db.run(blockHeightTable.schema.createIfNotExists)

}
