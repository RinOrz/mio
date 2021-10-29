@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.sweekt

import java.io.Serializable

/**
 * Represents a generic pair of two long values.
 *
 * @see Pair
 * @author 凛 (https://github.com/RinOrz)
 */
data class LongPair(val first: Long, val second: Long) : Serializable

/**
 * Create a long pair.
 *
 * Example `10L to 20L`.
 */
inline infix fun Long.to(that: Long): LongPair = LongPair(this, that)

/**
 * Converts this int pair into a list.
 */
fun LongPair.toList(): List<Long> = listOf(first, second)
