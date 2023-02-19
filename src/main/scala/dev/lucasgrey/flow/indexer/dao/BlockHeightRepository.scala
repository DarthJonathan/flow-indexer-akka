package dev.lucasgrey.flow.indexer.dao

import akka.Done
import slick.basic.DatabaseConfig
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

class BlockHeightRepository (
  val dbConfig: DatabaseConfig[PostgresProfile]
)(implicit val executionContext: ExecutionContext) {

  import dbConfig.profile.api._

  private val blockHeightTable = TableQuery[BlockHeightTable]

  def upsert (blockHeightData: BlockHeightData) = {
    blockHeightTable.insertOrUpdate(blockHeightData).map(_ => Done)
  }

  def createTable(): Future[Unit] =
    dbConfig.db.run(blockHeightTable.schema.createIfNotExists)

}
