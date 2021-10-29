@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.ChannelEmptyException
import com.meowool.mio.DataChannel
import com.meowool.mio.ifNull
import com.meowool.sweekt.throwIf
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

/**
 * @author 凛 (https://github.com/RinOrz)
 */
internal class DataChannelImpl(private val channel: SeekableByteChannel) : CommonDataChannelImpl1() {

  override var size: Long
    get() = _size.ifPlaceholder {
      channel.size().also { _size = it }
    }
    set(value) {
      _size = value
    }

  private inline fun initOffset(index: Long) {
    if (this.virtualIndex != index) {
      this.virtualIndex = index
      // Find the real index based on the virtual index
      val realIndex = getRealIndex(index)
      if (realIndex >= 0) channel.position(realIndex)
    }
  }

  override fun seek(index: Long): CommonDataChannelImpl1 {
    TODO("Not yet implemented")
  }

  override fun load(cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    throwIf(virtualIndex < 0) { IndexOutOfBoundsException("Index: $virtualIndex < 0") }
    throwIf(readQuietly() == -1) {
      ChannelEmptyException("The channel has no more bytes.")
    }
    loadedSingleByte()
    when {
      consume -> forget(cursor.index)
      else -> cursor.index++
    }
    this
  }

  override fun load(count: Int, cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    throwIf(virtualIndex < 0) { IndexOutOfBoundsException("Index: $virtualIndex < 0") }
    readQuietly(count).let { readCount ->
      throwIf(readCount == -1) {
        ChannelEmptyException("The channel has no more bytes.")
      }
      throwIf(readCount < count) {
        ChannelEmptyException("The number of remaining bytes in this channel is less than the given count: $count.")
      }
    }
    loadedMultiByte()
    when {
      consume -> forget(cursor.index)
      else -> cursor.index++
    }
    this
  }

  override fun loadOrNull(cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    loadedSingleByte()
    when {
      consume -> forget(cursor.index)
      else -> cursor.index++
    }
    when {
      virtualIndex < 0 || readQuietly() == -1 -> null
      else -> this
    }
  }

  override fun loadOrNull(count: Int, cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    loadedMultiByte()
    when {
      consume -> repeat(count) { forget(cursor.index + it) }
      else -> cursor.index += count
    }
    when {
      virtualIndex < 0 || readQuietly(count) == -1 -> null
      else -> this
    }
  }

  override fun loadLast(cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    throwIf(virtualIndex < 0) { IndexOutOfBoundsException("Index: $virtualIndex < 0") }
    throwIf(readQuietly() == -1) {
      ChannelEmptyException("The channel has no more bytes.")
    }
    loadedSingleByte()
    when {
      consume -> forget(cursor.index)
      else -> cursor.index--
    }
    this
  }

  override fun loadLast(count: Int, cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    throwIf(virtualIndex < 0) { IndexOutOfBoundsException("Index: $virtualIndex < 0") }
    readQuietly(count).let { readCount ->
      throwIf(readCount == -1) {
        ChannelEmptyException("The channel has no more bytes.")
      }
      throwIf(readCount < count) {
        ChannelEmptyException("The number of remaining bytes in this channel is less than the given count: $count.")
      }
    }
    loadedMultiByte()
    when {
      consume -> repeat(count) { forget(cursor.index + it) }
      else -> cursor.index -= count
    }
    this
  }

  override fun loadLastOrNull(cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    loadedSingleByte()
    when {
      consume -> forget(cursor.index)
      else -> cursor.index++
    }
    when {
      virtualIndex < 0 || readQuietly() == -1 -> null
      else -> this
    }
  }

  override fun loadLastOrNull(count: Int, cursor: DataChannel.Cursor, consume: Boolean) = available {
    initOffset(cursor.index)
    loadedMultiByte()
    when {
      consume -> forget(cursor.index)
      else -> cursor.index++
    }
    when {
      virtualIndex < 0 || readQuietly(count) == -1 -> null
      else -> this
    }
  }

  override fun getByte(): Byte = available { loadedBuffer.get(0) }

  override fun getChar(): Char = available { loadedBuffer.getChar(0) }

  override fun getFloat(): Float = available { loadedBuffer.getFloat(0) }

  override fun getShort(): Short = available { loadedBuffer.getShort(0) }

  override fun getDouble(): Double = available { loadedBuffer.getDouble(0) }

  override fun getLong(): Long = available { loadedBuffer.getLong(0) }

  override fun getInt(): Int = available { loadedBuffer.getInt(0) }

  override fun getAll(): ByteArray = available {
    loadedBuffer.rewind().let { buffer ->
      when {
        buffer.hasArray() -> buffer.array().copyOf(buffer.limit())
        else -> ByteArray(buffer.limit()).apply(buffer::get)
      }
    }
  }

  override fun isOpen(): Boolean = _isOpen.ifBoolPlaceholder {
    channel.isOpen.also { _isOpen = it.toInt() }
  }


  ////////////////////////////////////////////////////////////////////////////
  ////                            Internal Apis                           ////
  ////////////////////////////////////////////////////////////////////////////

  private var _size: Long = LongPlaceholder
  private var _isOpen: Int = IntPlaceholder
  private var _loaded: Int = IntPlaceholder
  private var _singleByteBuffer: ByteBuffer? = null
  private var _bytesBuffer: ByteBuffer? = null

  private var virtualIndex: Long = 0

  private val loadedBuffer: ByteBuffer
    get() = when (_loaded) {
      1 -> _singleByteBuffer
      else -> _bytesBuffer
    }!!

  private val singleByteBuffer: ByteBuffer
    get() = _singleByteBuffer.ifNull {
      ByteBuffer.allocate(1).also { _singleByteBuffer = it }
    }.rewind()

  private fun bytesBuffer(count: Int): ByteBuffer = _bytesBuffer.ifNull {
    ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).also { _bytesBuffer = it }
  }.rewind().limit(count)

  private inline fun readQuietly() = channel.read(singleByteBuffer).apply {
    // Reset after read
    channel.position(virtualIndex)
  }

  private inline fun readQuietly(count: Int) = channel.read(bytesBuffer(count)).apply {
    // Reset after read
    channel.position(virtualIndex)
  }

  private fun loadedSingleByte() {
    _loaded = 1
  }

  private fun loadedMultiByte() {
    _loaded = 0
  }
}