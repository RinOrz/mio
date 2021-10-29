@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import java.util.concurrent.ConcurrentHashMap

internal class CHMCache<K, V>(private val cache: ConcurrentHashMap<K, V>) : MioCache<K, V> {
  override inline fun get(key: K): V? = cache[key]

  override inline fun set(key: K, value: V) {
    cache[key] = value
  }

  override inline fun getOrElse(key: K, defaultValue: () -> V): V =
    cache.getOrElse(key, defaultValue)

  override inline fun getOrPut(key: K, defaultValue: () -> V): V =
    cache.getOrPut(key, defaultValue)

  override inline fun remove(key: K) {
    cache.remove(key!!)
  }

  override fun clear() {
    cache.clear()
  }
}

internal actual val maxMemoryCache: Long = Runtime.getRuntime().maxMemory() / 8