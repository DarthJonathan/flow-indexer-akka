package dev.lucasgrey.flow.indexer.daemon

import com.nftco.flow.sdk.{FlowAccessApi,Flow}

object BlockMonitor {
  val accessAPI: FlowAccessApi = Flow.newAccessApi("", 0)

  def getLatestBlockId() = {
    this.accessAPI.getLatestBlockHeader().getId()
  }

}

