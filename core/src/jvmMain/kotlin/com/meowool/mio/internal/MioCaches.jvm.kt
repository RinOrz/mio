@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.github.benmanes.caffeine.cache.AsyncCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.meowool.sweekt.whenTrue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap


private class CaffeinePathCache<K, V>(
  private val cache: AsyncCache<K, V>,
  private val evictedFetcher: (K) -> V?
): MioCache<K, V> {
  override inline operator fun get(key: K): V? =
    cache.getIfPresent(key)?.get() ?: evictedFetcher(key)

  override inline operator fun set(key: K, value: V) {
    cache.put(key, CompletableFuture.completedFuture(value))
  }

  override inline fun getOrElse(key: K, defaultValue: () -> V): V =
    get(key) ?: defaultValue()

  override inline fun getOrPut(key: K, crossinline defaultValue: () -> V): V =
    cache.get(key) { _ -> defaultValue() }.get()

  override inline fun remove(key: K) = cache.synchronous().invalidate(key)

  override inline fun clear() = cache.synchronous().invalidateAll()
}

internal actual fun <K, V> createCache(
  initialCapacity: Int?,
  weigher: ((K, V) -> Int)?,
  evictionListener: (K, V) -> Unit,
  evictedFetcher: (K) -> V?,
): MioCache<K, V> = when (weigher) {
  null -> CHMCache(initialCapacity?.let(::ConcurrentHashMap) ?: ConcurrentHashMap())
  else -> CaffeinePathCache(
    Caffeine.newBuilder()
      .weigher(weigher)
      .maximumWeight(maxMemoryCache)
      .evictionListener<K, V> { key, value, _ -> evictionListener(key!!, value!!) }
      .whenTrue(initialCapacity != null) { it.initialCapacity(initialCapacity!!) }
      .buildAsync(),
    evictedFetcher
  )
}