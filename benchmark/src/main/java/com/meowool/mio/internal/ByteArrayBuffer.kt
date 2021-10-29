@file:Suppress("ReplaceGetOrSet", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.channel.ByteOrder
import com.meowool.sweekt.array.ByteArrayBuilder
import com.meowool.sweekt.throwIf
import java.nio.ByteOrder as NioByteOrder

/**
 * A big buffer similar to [ByteArrayBuilder].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class ByteArrayBuffer(private val capacity: Long = 1024) : DataBuffer<ByteArrayBuffer> {
  private var value: BigByteArray = BigByteArray(capacity)
  private var isBigEndian: Boolean = NioByteOrder.nativeOrder() == NioByteOrder.BIG_ENDIAN

  override var order: ByteOrder = ByteOrder.BigEndian
    set(value) {
      field = value
      isBigEndian = value.toByteOrder() == NioByteOrder.BIG_ENDIAN
    }

  var oldSize: Long = 0

  override var size: Long = 0
    private set

  /** [java.nio.HeapByteBuffer.get] */
  override fun getByte(index: Long): Byte = value.get(index)

  /** [jdk.internal.misc.Unsafe.makeShort] */
  override fun getShort(index: Long): Short {
    val b1 = getByte(index).toUInt() shl pickPos(8, 0)
    val b2 = getByte(index + 1).toUInt() shl pickPos(8, 8)
    return (b1 or b2).toShort().correctEndian()
  }

  /** [java.nio.HeapByteBuffer.getChar] */
  override fun getChar(index: Long): Char = getShort(index).toInt().toChar()

  /** [jdk.internal.misc.Unsafe.makeInt] */
  override fun getInt(index: Long): Int {
    val b1 = getByte(index).toUInt() shl pickPos(24, 0)
    val b2 = getByte(index + 1).toUInt() shl pickPos(24, 8)
    val b3 = getByte(index + 2).toUInt() shl pickPos(24, 16)
    val b4 = getByte(index + 3).toUInt() shl pickPos(24, 24)
    return (b1 or b2 or b3 or b4).toInt().correctEndian()
  }

  /** [java.nio.HeapByteBuffer.getFloat] */
  override fun getFloat(index: Long): Float = Float.fromBits(getInt(index))

  /** [jdk.internal.misc.Unsafe.makeLong] */
  override fun getLong(index: Long): Long {
    val b1 = getByte(index).toUInt() shl pickPos(56, 0)
    val b2 = getByte(index + 1).toUInt() shl pickPos(56, 8)
    val b3 = getByte(index + 2).toUInt() shl pickPos(56, 16)
    val b4 = getByte(index + 3).toUInt() shl pickPos(56, 24)
    val b5 = getByte(index + 4).toUInt() shl pickPos(56, 32)
    val b6 = getByte(index + 5).toUInt() shl pickPos(56, 40)
    val b7 = getByte(index + 6).toUInt() shl pickPos(56, 48)
    val b8 = getByte(index + 7).toUInt() shl pickPos(56, 56)
    return (b1 or b2 or b3 or b4 or b5 or b6 or b7 or b8).toLong().correctEndian()
  }

  /** [java.nio.HeapByteBuffer.getDouble] */
  override fun getDouble(index: Long): Double = Double.fromBits(getLong(index))

  override fun getBytes(index: Long, count: Int): ByteArray {
    val output = ByteArray(count)
    value.copyTo(output, srcPos = index, length = count)
    return output
  }

  override fun getAllBytes(): ByteArray {
    throwIf(size > ByteArrayBuilder.MaxArraySize) {
      OutOfMemoryError("Required array size too large")
    }
    val output = ByteArray(size.toInt())
    value.copyTo(output)
    return output
  }

  /** [java.nio.HeapByteBuffer.put] */
  override fun put(index: Long, data: Byte): ByteArrayBuffer = apply {
    this.value[index] = data
    val newSize = index + 1
    if (newSize > size) {
      oldSize = size
      size = newSize
    }
  }

  /** [jdk.internal.misc.Unsafe.putShortUnaligned] */
  override fun put(index: Long, data: Short): ByteArrayBuffer = apply {
    val short = data.reverseBytes().toInt()
    fun parts(i0: Byte, i1: Byte) {
      put(index, pick(i0, i1))
      put(index + 1, pick(i1, i0))
    }
    parts((short ushr 0).toByte(), (short ushr 8).toByte())
  }

  /** [java.nio.HeapByteBuffer.putChar] */
  override fun put(index: Long, data: Char): ByteArrayBuffer = apply {
    put(index, data.code.toShort())
  }

  /** [jdk.internal.misc.Unsafe.putIntUnaligned] */
  override fun put(index: Long, data: Int): ByteArrayBuffer = apply {
    val int = data.reverseBytes()
    fun parts(i0: Byte, i1: Byte, i2: Byte, i3: Byte) {
      put(index, pick(i0, i3))
      put(index + 1, pick(i1, i2))
      put(index + 2, pick(i2, i1))
      put(index + 3, pick(i3, i0))
    }
    parts(
      (int ushr 0).toByte(),
      (int ushr 8).toByte(),
      (int ushr 16).toByte(),
      (int ushr 24).toByte()
    )
  }

  /** [java.nio.HeapByteBuffer.putFloat] */
  override fun put(index: Long, data: Float): ByteArrayBuffer = apply {
    put(index, data.toRawBits())
  }

  /** [jdk.internal.misc.Unsafe.putLongUnaligned] */
  override fun put(index: Long, data: Long): ByteArrayBuffer = apply {
    val long = data.reverseBytes()
    fun parts(i0: Byte, i1: Byte, i2: Byte, i3: Byte, i4: Byte, i5: Byte, i6: Byte, i7: Byte) {
      put(index, pick(i0, i7))
      put(index + 1, pick(i1, i6))
      put(index + 2, pick(i2, i5))
      put(index + 3, pick(i3, i4))
      put(index + 4, pick(i4, i3))
      put(index + 5, pick(i5, i2))
      put(index + 6, pick(i6, i1))
      put(index + 7, pick(i7, i0))
    }
    parts(
      (long ushr 0).toByte(),
      (long ushr 8).toByte(),
      (long ushr 16).toByte(),
      (long ushr 24).toByte(),
      (long ushr 32).toByte(),
      (long ushr 40).toByte(),
      (long ushr 48).toByte(),
      (long ushr 56).toByte()
    )
  }

  override fun put(index: Long, data: Double): ByteArrayBuffer = apply {
    put(index, data.toRawBits())
  }

  override fun clear(): ByteArrayBuffer = apply {
    value = BigByteArray(capacity)
  }

  /** [jdk.internal.misc.Unsafe.pickPos] */
  private fun pickPos(top: Int, pos: Int): Int = if (isBigEndian) top - pos else pos

  /** [jdk.internal.misc.Unsafe.pick] */
  private fun pick(le: Byte, be: Byte): Byte = if (isBigEndian) be else le
  private fun pick(le: Short, be: Short): Short = if (isBigEndian) be else le
  private fun pick(le: Int, be: Int): Int = if (isBigEndian) be else le

  /** [jdk.internal.misc.Unsafe.convEndian] */
  private fun Short.correctEndian() = if (isBigEndian) this else this.reverseBytes()
  private fun Int.correctEndian() = if (isBigEndian) this else this.reverseBytes()
  private fun Long.correctEndian() = if (isBigEndian) this else this.reverseBytes()
}
