package dev.lucasgrey.flow.indexer.utils

import dev.lucasgrey.flow.indexer.model.{FlowSingleSignature, ProposalKey, TransactionResult}
import com.github.tminglei.slickpg._
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.{Json, JsonObject, jawn}
import io.circe.syntax._

trait PostgresProfileExtended extends ExPostgresProfile
  with PgArraySupport
  with PgCirceJsonSupport {
  override def pgjson = "jsonb"
  override protected def computeCapabilities: Set[slick.basic.Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  override val api: PostgresProfileExtendedAPI = new PostgresProfileExtendedAPI {}
  trait PostgresProfileExtendedAPI extends API
    with JsonImplicits
    with ArrayImplicits {

    implicit val flowSingleSignatureListMapper = MappedJdbcType.base[List[FlowSingleSignature], Json](
      s => s.asJson,
      s => s.as[List[FlowSingleSignature]].getOrElse(null)
    )

    implicit val proposalKeyMapper = MappedJdbcType.base[ProposalKey, Json](
      s => s.asJson,
      s => s.as[ProposalKey].getOrElse(null)
    )

    implicit val transactionResultMapper = MappedJdbcType.base[TransactionResult, Json](
      s => s.asJson,
      s => s.as[TransactionResult].getOrElse(null)
    )

  }
}

object PostgresProfileExtended extends PostgresProfileExtended