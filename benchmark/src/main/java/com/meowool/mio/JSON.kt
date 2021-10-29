@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package com.meowool.mio

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class Type(private val name: String) {
  @Serializable object Foo : Type("object-Foo")
  @Serializable object Bar : Type("object-Bar")

  fun toPair() = name to this.javaClass.name
 }

@Serializable data class NetResult<T>(val data: T)
@Serializable data class MultiResult<K, V>(val data: Map<K, V>)

fun main() {
  Json.encodeToString(NetResult("test")).run(::println)
  println()
  Json.encodeToString(NetResult(50)).also(::println)
  println()
  Json.encodeToString(MultiResult(
    mapOf(Type.Foo.toPair(), Type.Bar.toPair())
  )).also(::println)
}