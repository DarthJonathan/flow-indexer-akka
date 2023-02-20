package dev.lucasgrey.flow.indexer.utils

import scala.language.implicitConversions

object HexConverter {

  implicit def convertToHex(data: Array[Byte]): String = {
    val sb = new StringBuilder
    for (b <- data) {
      sb.append(String.format("%02x", Byte.box(b)))
    }
    sb.toString
  }

  implicit def convertToByteArray(data: String): Array[Byte] = {
    data.sliding(2,2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

}
