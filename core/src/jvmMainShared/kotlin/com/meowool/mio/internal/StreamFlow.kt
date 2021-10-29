@file:Suppress("NewApi")

package com.meowool.mio.internal

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import java.util.stream.Stream

/**
 * 在每次执行 [Flow.collect] 收集前通过给定的 [creator] 构造新的 [Stream].
 * NOTE: 因为 java8 的流是一次性的，所以不能让 kotlin 的冷流直接收集实例
 */
internal inline fun <T> streamFlow(
  crossinline creator: () -> Stream<T>
): Flow<T> = object : StreamFlow<T>() {
  override fun getStream(): Stream<T> = creator()
}

internal abstract class StreamFlow<T> : Flow<T> {
  @InternalCoroutinesApi
  @Suppress("ConvertTryFinallyToUseCall")
  override suspend fun collect(collector: FlowCollector<T>) {
    getStream().use {
      for (value in it.iterator()) {
        collector.emit(value)
      }
    }
  }

  abstract fun getStream(): Stream<T>
}

