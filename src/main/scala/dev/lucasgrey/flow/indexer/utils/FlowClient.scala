package dev.lucasgrey.flow.indexer.utils

import com.google.protobuf.ByteString
import dev.lucasgrey.flow.indexer.model.{FlowBlock, FlowBlockHeader, FlowCollection, FlowEvents, FlowSeals, FlowSingleSignature, FlowTransaction, ProposalKey, TransactionResult}
import org.onflow.protobuf.access.{Access, AccessAPIGrpc}

import scala.jdk.CollectionConverters._
import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class FlowClient (
  accessAPI: AccessAPIGrpc.AccessAPIFutureStub
)(implicit executionContext: ExecutionContext) {

  import FuturesConverter._
  import HexConverter._

  def getLatestBlockHeader: Future[FlowBlockHeader] = {
    accessAPI.getLatestBlockHeader(
      Access.GetLatestBlockHeaderRequest
        .newBuilder()
        .build()
    ).asScala.map(s => {
      FlowBlockHeader(
        s.getBlock.getHeight,
        convertToHex(s.getBlock.getId.toByteArray),
        convertToHex(s.getBlock.getParentId.toByteArray)
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
        convertToHex(s.getBlock.getId.toByteArray),
        convertToHex(s.getBlock.getParentId.toByteArray)
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
            executionReceiptId = convertToHex(s.getExecutionReceiptId.toByteArray),
            executionReceiptSignatures = s.getExecutionReceiptSignaturesList.asScala.map(d => convertToHex(d.toByteArray)).toList,
            resultApprovalSignatures = s.getResultApprovalSignaturesList.asScala.map(d => convertToHex(d.toByteArray)).toList
          )
        }).toList,
        signatures = s.getBlock.getSignaturesList.asScala.map(d => convertToHex(d.toByteArray)).toList,
        collectionGuarantee = s.getBlock.getCollectionGuaranteesList.asScala
          .map(s => FlowCollection(
            collectionId = convertToHex(s.getCollectionId.toByteArray),
            transactionList = s.getSignaturesList.asScala.map(d => convertToHex(d.toByteArray)).toList
          ))
          .toList
      )
    })
  }

  def getTransactionResultById(transactionId: String): Future[TransactionResult] = {
    accessAPI.getTransactionResult(
      Access.GetTransactionRequest
        .newBuilder()
        .setId(ByteString.copyFrom(HexConverter.convertToByteArray(transactionId)))
        .build()
    ).asScala
      .map(trxRes => {
        TransactionResult(
          status = trxRes.getStatus.toString,
          block  = convertToHex(trxRes.getBlockId.toByteArray),
          events = trxRes.getEventsList.asScala.map(s => FlowEvents(
            eventType = s.getType,
            transactionId = convertToHex(s.getTransactionId.toByteArray),
            transactionIndex = s.getTransactionIndex,
            eventIndex = s.getEventIndex,
            payload = new String(s.getPayload.toByteArray)
          )).toList
        )
      })
  }


    def getTransactionById(transactionId: String): Future[FlowTransaction] = {
    accessAPI.getTransaction(
      Access.GetTransactionRequest
        .newBuilder()
        .setId(ByteString.copyFrom(HexConverter.convertToByteArray(transactionId)))
        .build()
    ).asScala
      .map(trxRes => {
        val trx = trxRes.getTransaction
        FlowTransaction(
          script = new String(trx.getScript.toByteArray),
          arguments = trx.getArgumentsList.asScala.map(s => new String(s.toByteArray)).toList,
          referenceBlockId = convertToHex(trx.getReferenceBlockId.toByteArray),
          payer = convertToHex(trx.getPayer.toByteArray),
          gasLimit = trx.getGasLimit,
          proposalKey = ProposalKey(
            address = convertToHex(trx.getProposalKey.getAddress.toByteArray), keyId = trx.getProposalKey.getKeyId, sequenceNo = trx.getProposalKey.getSequenceNumber
          ),
          authorizers = trx.getAuthorizersList.asScala.map(s => convertToHex(s.toByteArray)).toList,
          payloadSignatures = trx.getPayloadSignaturesList.asScala
            .map(s => FlowSingleSignature(
              signature = convertToHex(s.getSignature.toByteArray),
              keyId = s.getKeyId,
              address = convertToHex(s.getAddress.toByteArray)
            )).toList,
          envelopeSignatures = trx.getEnvelopeSignaturesList.asScala
            .map(s => FlowSingleSignature(
              signature = convertToHex(s.getSignature.toByteArray),
              keyId = s.getKeyId,
              address = convertToHex(s.getAddress.toByteArray)
            )).toList
        )
    })
  }

}
