package com.meowool.mio.internal

import com.meowool.mio.channel.ByteOrder
import java.nio.ByteOrder as NioByteOrder


internal fun NioByteOrder.toByteOrder() = when (this) {
  NioByteOrder.BIG_ENDIAN -> ByteOrder.BigEndian
  NioByteOrder.LITTLE_ENDIAN -> ByteOrder.LittleEndian
  else -> ByteOrder.NativeEndian
}

internal fun ByteOrder.toByteOrder() = when (this) {
  ByteOrder.BigEndian -> NioByteOrder.BIG_ENDIAN
  ByteOrder.LittleEndian -> NioByteOrder.LITTLE_ENDIAN
  ByteOrder.NativeEndian -> NioByteOrder.nativeOrder()
}

internal fun Short.reverseBytes(): Short {
  val i = toInt() and 0xffff
  val reversed = (i and 0xff00 ushr 8) or
    (i and 0x00ff shl 8)
  return reversed.toShort()
}

internal fun Int.reverseBytes(): Int =
  (this and -0x1000000 ushr 24) or
    (this and 0x00ff0000 ushr 8) or
    (this and 0x0000ff00 shl 8) or
    (this and 0x000000ff shl 24)

internal fun Long.reverseBytes(): Long =
  (this and -0x100000000000000L ushr 56) or
    (this and 0x00ff000000000000L ushr 40) or
    (this and 0x0000ff0000000000L ushr 24) or
    (this and 0x000000ff00000000L ushr 8) or
    (this and 0x00000000ff000000L shl 8) or
    (this and 0x0000000000ff0000L shl 24) or
    (this and 0x000000000000ff00L shl 40) or
    (this and 0x00000000000000ffL shl 56)
