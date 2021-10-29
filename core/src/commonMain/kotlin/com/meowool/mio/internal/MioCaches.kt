package com.meowool.mio.internal

internal interface MioCache<K, V> {
  operator fun set(key: K, value: V)
  operator fun get(key: K): V?
  fun getOrElse(key: K, defaultValue: () -> V): V
  fun getOrPut(key: K, defaultValue: () -> V): V
  fun remove(key: K)
  fun clear()
}

internal expect val maxMemoryCache: Long

internal expect fun <K, V> createCache(
  initialCapacity: Int? = null,
  weigher: ((K, V) -> Int)? = null,
  evictionListener: (K, V) -> Unit = { _, _ -> },
  evictedFetcher: (K) -> V? = { null },
): MioCache<K, V>