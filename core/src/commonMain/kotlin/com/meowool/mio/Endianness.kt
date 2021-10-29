package com.meowool.mio

/**
 * Represents the definition of byte orders.
 *
 * [Reference](https://en.wikipedia.org/wiki/Endianness)
 *
 * @author 凛 (https://github.com/RinOrz)
 */
enum class Endianness {

  /**
   * The big-endian byte order. In this order, the bytes of a multibyte value are ordered from
   * most significant to least significant.
   */
  BigEndian,

  /**
   * The little-endian byte order. In this order, the bytes of a multibyte value are ordered from
   * least significant to most significant.
   */
  LittleEndian,

  /**
   * The native-endian byte order. Retrieves the native byte order of the underlying platform.
   */
  NativeEndian
}