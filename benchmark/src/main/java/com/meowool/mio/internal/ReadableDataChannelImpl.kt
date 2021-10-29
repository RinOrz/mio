@file:Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.channel.DataChannelCursor
import com.meowool.mio.channel.ByteOrder
import com.meowool.mio.channel.ReadableDataChannel
import com.meowool.sweekt.array.buildByteArray
import com.meowool.sweekt.throwIf
import java.nio.channels.ClosedChannelException

/**
 * @author 凛 (https://github.com/RinOrz)
 */
internal class ReadableDataChannelImpl(private var buffer: DataBuffer<*>) : ReadableDataChannel {

  private var _cursor: CursorImpl? = null

  /**
   * The buffer that can insert and delete middle bytes.
   */
  private val mutableBuffer: DataBuffer<*>
    get() = when (buffer) {
      is MutableDataBuffer<*> -> buffer
      else -> PieceBuffer(buffer).also { buffer = it }
    }

  override var size: Long
    get() = TODO("Not yet implemented")
    set(value) {}

  override var order: ByteOrder
    get() = buffer.order
    set(value) {
      buffer.order = value
    }

  override val cursor: DataChannelCursor
    get() = _cursor ?: CursorImpl(firstIndex).also { _cursor = it }

  override fun peek(index: Long): Byte =
    buffer.consume(index, Byte.SIZE_BYTES) { getByte(index) }

  override fun peekOrNull(index: Long): Byte? =
    buffer.consume(index, Byte.SIZE_BYTES) { getByteOrNull(index) }

  override fun peekShort(index: Long): Short =
    buffer.consume(index, Short.SIZE_BYTES) { getShort(index) }

  override fun peekShortOrNull(index: Long): Short? =
    buffer.consume(index, Short.SIZE_BYTES) { getShortOrNull(index) }

  override fun peekChar(index: Long): Char =
    buffer.consume(index, Char.SIZE_BYTES) { getChar(index) }

  override fun peekCharOrNull(index: Long): Char? =
    buffer.consume(index, Char.SIZE_BYTES) { getCharOrNull(index) }

  override fun peekInt(index: Long): Int =
    buffer.consume(index, Int.SIZE_BYTES) { getInt(index) }

  override fun peekIntOrNull(index: Long): Int? =
    buffer.consume(index, Int.SIZE_BYTES) { getIntOrNull(index) }

  override fun peekLong(index: Long): Long =
    buffer.consume(index, Long.SIZE_BYTES) { getLong(index) }

  override fun peekLongOrNull(index: Long): Long? =
    buffer.consume(index, Long.SIZE_BYTES) { getLongOrNull(index) }

  override fun peekFloat(index: Long): Float =
    buffer.consume(index, Float.SIZE_BYTES) { getFloat(index) }

  override fun peekFloatOrNull(index: Long): Float? =
    buffer.consume(index, Float.SIZE_BYTES) { getFloatOrNull(index) }

  override fun peekDouble(index: Long): Double =
    buffer.consume(index, Double.SIZE_BYTES) { getDouble(index) }

  override fun peekDoubleOrNull(index: Long): Double? =
    buffer.consume(index, Double.SIZE_BYTES) { getDoubleOrNull(index) }

  override fun peekBytes(index: Long, count: Int): ByteArray =
    buffer.consume(index, count) { getBytes(index, count) }

  override fun peekBytesOrNull(index: Long, count: Int): ByteArray? =
    buffer.consume(index, count) { getBytesOrNull(index, count) }

  override fun peekLineBytes(index: Long): ByteArray =
    buildByteArray { collectRightLine(::append) }

  override fun peekLineBytesOrNull(index: Long): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun peekLeftLineBytes(index: Long): ByteArray {
    TODO("Not yet implemented")
  }

  override fun peekLeftLineBytesOrNull(index: Long): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun peekAllBytes(): ByteArray {
    TODO("Not yet implemented")
  }

  override fun peekAllBytesOrNull(): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun isOpen(): Boolean {
    TODO("Not yet implemented")
  }

  private inline fun <B : DataBuffer<*>, R> B.consume(
    index: Long = cursor.index,
    offset: Int,
    block: B.(index: Long) -> R
  ): R {
    checkIndex(index)
    throwIf(isOpen().not()) { ClosedChannelException() }
    return block(index).also { cursor.moveTo(index + offset) }
  }

  /** Reads the bytes of the line of current [cursor]. */
  private inline fun collectRightLine(collector: (Byte) -> Unit = {}) {
    while (true) {
      when (val byte = peekOrNull() ?: break) {
        // In the case of `\r` or `\r\n`
        CarriageReturn -> {
          // Read next one byte (moved cursor)
          peekOrNull()?.let {
            // Move the cursor to skip the byte if combine with the next one is `\r\n`
            if (it == LineFeed) cursor.moveRight()
          }
          // Just `\r`, break the loop directly
          break
        }

        // Break the loop directly if the byte is `\n`
        LineFeed -> break

        // Collecting byte
        else -> collector(byte)
      }
    }
  }

  inner class CursorImpl(override var index: Long) : DataChannelCursor {
    private var remembered: Long = 0

    override val isReachStart: Boolean
      get() = index <= firstIndex

    override val isReachEnd: Boolean
      get() = index >= lastIndex

    inline fun moveRight() = apply { this.index++ }

    inline fun moveLeft() = apply { this.index-- }

    override inline fun moveTo(index: Long): DataChannelCursor = apply { this.index = index }

    override fun moveToFirst(): DataChannelCursor = apply { this.index = firstIndex }

    override fun moveToLast(): DataChannelCursor  = apply { this.index = lastIndex }

    override fun moveRight(repeat: Long): DataChannelCursor  = apply { this.index += repeat }

    override fun moveLeft(repeat: Long): DataChannelCursor = apply { this.index -= repeat }

    override fun moveToStartOfLine(): DataChannelCursor {
      TODO("Not yet implemented")
    }

    override fun moveToEndOfLine(): DataChannelCursor {
      TODO("Not yet implemented")
    }

    override fun moveToNextLine(repeat: Long): DataChannelCursor {
      TODO("Not yet implemented")
    }

    override fun moveToPreviousLine(repeat: Long): DataChannelCursor {
      TODO("Not yet implemented")
    }

    override fun remember(): DataChannelCursor = apply { remembered = index }

    override fun restore(): DataChannelCursor = apply { index = remembered }

    override fun first(): DataChannelCursor = apply { index = firstIndex }

    override fun last(): DataChannelCursor = apply { index = lastIndex }
  }
}