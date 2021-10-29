@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio

import kotlin.text.toByteArray as wrapByteArray

/**
 * A named mapping between sequences of sixteen-bit Unicode code units and sequences of bytes.
 */
actual typealias Charset = java.nio.charset.Charset

/**
 * Returns a named charset with the given charset [name].
 */
actual fun charset(name: String): Charset = Charset.forName(name)

/**
 * Encodes the contents of this string using the specified [charset] and returns the resulting
 * byte array.
 */
actual inline fun String.toByteArray(charset: Charset): ByteArray = wrapByteArray(charset)

/**
 * Converts the contents of this byte array to a string using the specified [charset].
 */
actual inline fun ByteArray.toString(charset: Charset): String = String(this, charset)