package dev.lucasgrey.flow.indexer.utils

import scala.concurrent.{Future, Promise}
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture, MoreExecutors}

object FuturesConverter {
  implicit class RichListenableFuture[T](lf: ListenableFuture[T]) {
    def asScala: Future[T] = {
      val p = Promise[T]()
      Futures.addCallback(lf, new FutureCallback[T] {
        def onFailure(t: Throwable): Unit = p failure t
        def onSuccess(result: T): Unit    = p success result
      }, MoreExecutors.directExecutor())
      p.future
    }
  }


}
