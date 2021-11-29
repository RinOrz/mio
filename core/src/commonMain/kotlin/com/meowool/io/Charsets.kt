@file:Suppress("NO_ACTUAL_FOR_EXPECT", "NOTHING_TO_INLINE")

package com.meowool.io

import com.meowool.sweekt.LazyInit

/**
 * Constant definitions for the standard [Charset].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
object Charsets {

  /**
   * Eight-bit UCS Transformation Format.
   */
  @LazyInit val UTF_8: Charset = charset("UTF-8")

  /**
   * Sixteen-bit UCS Transformation Format, byte order identified by an
   * optional byte-order mark.
   */
  @LazyInit val UTF_16: Charset = charset("UTF-16")

  /**
   * Sixteen-bit UCS Transformation Format, big-endian byte order.
   */
  @LazyInit val UTF_16BE: Charset = charset("UTF-16BE")

  /**
   * Sixteen-bit UCS Transformation Format, little-endian byte order.
   */
  @LazyInit val UTF_16LE: Charset = charset("UTF-16LE")

  /**
   * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the
   * Unicode character set.
   */
  @LazyInit val US_ASCII: Charset = charset("US-ASCII")

  /**
   * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
   */
  @LazyInit val ISO_8859_1: Charset = charset("ISO-8859-1")

  /**
   * 32-bit Unicode (or UCS) Transformation Format, byte order identified by an optional byte-order mark
   */
  @LazyInit val UTF_32: Charset = charset("UTF-32")

  /**
   * 32-bit Unicode (or UCS) Transformation Format, little-endian byte order.
   */
  @LazyInit val UTF_32LE: Charset = charset("UTF-32LE")

  /**
   * 32-bit Unicode (or UCS) Transformation Format, big-endian byte order.
   */
  @LazyInit val UTF_32BE: Charset = charset("UTF-32BE")
}

/**
 * A named mapping between sequences of sixteen-bit Unicode code units and sequences of bytes.
 */
expect abstract class Charset

/**
 * Returns a named charset with the given charset [name].
 */
expect fun charset(name: String): Charset

/**
 * Encodes the contents of this string using the specified [charset] and returns the resulting
 * byte array.
 */
expect inline fun String.toByteArray(charset: Charset = Charsets.UTF_8): ByteArray

/**
 * Converts the contents of this byte array to a string using the specified [charset].
 */
expect inline fun ByteArray.toString(charset: Charset = Charsets.UTF_8): String

/**
 * Decodes the contents of this byte array to a string using the specified [charset].
 */
inline fun ByteArray.decodeToString(charset: Charset = Charsets.UTF_8): String = toString(charset)
