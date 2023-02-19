package dev.lucasgrey.flow.indexer.utils

import dev.lucasgrey.flow.indexer.model.{FlowBlock, FlowBlockHeader, FlowSeals}
import org.onflow.protobuf.access.{Access, AccessAPIGrpc}
import scala.jdk.CollectionConverters._

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class FlowClient (
  accessAPI: AccessAPIGrpc.AccessAPIFutureStub
)(implicit executionContext: ExecutionContext) {

  import FuturesConverter._

  def getLatestBlockHeader: Future[FlowBlockHeader] = {
    accessAPI.getLatestBlockHeader(
      Access.GetLatestBlockHeaderRequest
        .newBuilder()
        .build()
    ).asScala.map(s => {
      FlowBlockHeader(
        s.getBlock.getHeight,
        s.getBlock.getId.toByteArray,
        s.getBlock.getParentId.toByteArray
      )
    })
  }

  def getBlockHeaderByHeight(height: Long): Future[FlowBlockHeader] = {
    accessAPI.getBlockHeaderByHeight(
      Access.GetBlockHeaderByHeightRequest
        .newBuilder()
        .setHeight(height)
        .build()
    ).asScala.map(s => {
      FlowBlockHeader(
        s.getBlock.getHeight,
        s.getBlock.getId.toByteArray,
        s.getBlock.getParentId.toByteArray
      )
    })
  }

  def getBlockByHeight(height: Long): Future[FlowBlock] = {
    accessAPI.getBlockByHeight(
      Access.GetBlockByHeightRequest
        .newBuilder()
        .setHeight(height)
        .build()
    ).asScala.map(s => {
      FlowBlock(
        height = s.getBlock.getHeight,
        id = s.getBlock.getId.toByteArray,
        parentId = s.getBlock.getParentId.toByteArray,
        timestamp = Instant.ofEpochSecond(s.getBlock.getTimestamp.getSeconds, s.getBlock.getTimestamp.getNanos),
        seals = s.getBlock.getBlockSealsList.asScala.map(s => {
          FlowSeals(
            id = s.getBlockId.toByteArray,
            executionReceiptId = s.getExecutionReceiptId.toByteArray,
            executionReceiptSignatures = s.getExecutionReceiptSignaturesList.asScala.map(_.toByteArray).toList,
            resultApprovalSignatures = s.getResultApprovalSignaturesList.asScala.map(_.toByteArray).toList
          )
        }).toList,
        signatures = s.getBlock.getSignaturesList.asScala.map(_.toByteArray).toList,
        collectionGuarantee = s.getBlock.getCollectionGuaranteesList.asScala.map(s => {
          (s.getCollectionId.toByteArray, s.getSignaturesList.asScala.map(_.toByteArray).toList)
        }).toList
      )
    })
  }
}
