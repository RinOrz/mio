package com.meowool.mio.internal

import com.meowool.mio.channel.ByteOrder
import com.meowool.sweekt.letOrNull
import com.meowool.sweekt.runOrNull

/**
 * @author 凛 (https://github.com/RinOrz)
 */
internal interface DataBuffer<S : DataBuffer<S>> {
  val size: Long
  var order: ByteOrder

  /** Gets data from this buffer. */
  fun getByte(index: Long): Byte
  fun getShort(index: Long): Short
  fun getChar(index: Long): Char
  fun getInt(index: Long): Int
  fun getFloat(index: Long): Float
  fun getLong(index: Long): Long
  fun getDouble(index: Long): Double
  fun getBytes(index: Long, count: Int): ByteArray

  fun getByteOrNull(index: Long): Byte? = letOrNull { getByte(index) }
  fun getShortOrNull(index: Long): Short? = letOrNull { getShort(index) }
  fun getCharOrNull(index: Long): Char? = letOrNull { getChar(index) }
  fun getIntOrNull(index: Long): Int? = letOrNull { getInt(index) }
  fun getFloatOrNull(index: Long): Float? = letOrNull { getFloat(index) }
  fun getLongOrNull(index: Long): Long? = letOrNull { getLong(index) }
  fun getDoubleOrNull(index: Long): Double? = letOrNull { getDouble(index) }
  fun getBytesOrNull(index: Long, count: Int): ByteArray? = letOrNull { getBytes(index, count) }

  fun getAllBytes(): ByteArray

  /** Puts data to this buffer. */
  fun put(index: Long, data: Byte): S
  fun put(index: Long, data: Short): S
  fun put(index: Long, data: Char): S
  fun put(index: Long, data: Int): S
  fun put(index: Long, data: Float): S
  fun put(index: Long, data: Long): S
  fun put(index: Long, data: Double): S

  /** Adds data to this buffer. */
  fun add(data: Byte): S = put(size, data)
  fun add(data: Short): S = put(size, data)
  fun add(data: Char): S = put(size, data)
  fun add(data: Int): S = put(size, data)
  fun add(data: Float): S = put(size, data)
  fun add(data: Long): S = put(size, data)
  fun add(data: Double): S = put(size, data)

  fun clear(): S
}


internal interface MutableDataBuffer<S : MutableDataBuffer<S>> : DataBuffer<S> {
  /** Inserts data to this buffer. */
  fun insert(index: Long, data: Byte): S
  fun insert(index: Long, data: Short): S
  fun insert(index: Long, data: Char): S
  fun insert(index: Long, data: Int): S
  fun insert(index: Long, data: Float): S
  fun insert(index: Long, data: Long): S
  fun insert(index: Long, data: Double): S

  /** Removes some data from this buffer. */
  fun remove(index: Long, count: Long = 1): S
}