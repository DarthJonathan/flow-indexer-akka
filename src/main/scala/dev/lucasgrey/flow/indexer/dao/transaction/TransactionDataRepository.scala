package dev.lucasgrey.flow.indexer.dao.transaction

import akka.Done
import dev.lucasgrey.flow.indexer.utils.PostgresProfileExtended
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

class TransactionDataRepository(
  val dbConfig: DatabaseConfig[PostgresProfileExtended],
)(implicit val executionContext: ExecutionContext) {

  import dbConfig.profile.api._

  private val transactionTable = TableQuery[TransactionTable]

  def upsert(transactionData: TransactionData) = {
    transactionTable.insertOrUpdate(transactionData).map(_ => Done)
  }

  def findTransactionById(transactionId: String): Future[Option[TransactionData]] = {
    dbConfig.db.run(
      transactionTable.filter(_.transactionId === transactionId)
        .take(1)
        .result
        .headOption
    )
  }
  def findTransactionsByBlockHeight(height: Long): Future[Seq[TransactionData]] = {
    dbConfig.db.run(
      transactionTable
        .filter(_.blockHeight === height)
        .result
    )
  }

  def createTable(): Future[Unit] =
    dbConfig.db.run(transactionTable.schema.createIfNotExists)

}
