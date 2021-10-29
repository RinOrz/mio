@file:Suppress("NOTHING_TO_INLINE", "UsePropertyAccessSyntax")

package com.meowool.mio.internal

import com.meowool.mio.ChannelEmptyException
import com.meowool.mio.DataChannel
import com.meowool.mio.Endianness
import com.meowool.sweekt.throwIf
import java.io.EOFException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class DataChannelImpl(
  private val nioChannel: SeekableByteChannel,
) : DataChannel {
  override var size: Long
    get() = _size.ifPlaceholder { nioChannel.size().also { _size = it } }
    set(value) {
      _size = value
    }

  override var order: Endianness = Endianness.NativeEndian

  override val startCursor: DataChannel.Cursor
    get() = _startCursor ?: CursorImpl(0).also { _startCursor = it }

  override val endCursor: DataChannel.Cursor
    get() = _endCursor ?: CursorImpl(size).also { _endCursor = it }

  override fun peekAt(index: Long): Byte = ensureActive {
    nioChannel.position(index).read(buffer.limit(1))
    buffer.get()
  }

  override fun peek(): Byte = ensureActive { peek(1).get() }

  override fun peekLast(): Byte = ensureActive { peekLast(1).get() }

  override fun peekShort(): Short = ensureActive { peek(2).getShort() }

  override fun peekLastShort(): Short = ensureActive { peekLast(2).getShort() }

  override fun peekInt(): Int = ensureActive { peek(4).getInt() }

  override fun peekLastInt(): Int = ensureActive { peekLast(4).getInt() }

  override fun peekLong(): Long = ensureActive { peek(8).getLong() }

  override fun peekLastLong(): Long = ensureActive { peekLast(8).getLong() }

  override fun peekFloat(): Float = ensureActive { peek(4).getFloat() }

  override fun peekLastFloat(): Float = ensureActive { peekLast(4).getFloat() }

  override fun peekDouble(): Double = ensureActive { peek(8).getDouble() }

  override fun peekLastDouble(): Double = ensureActive { peekLast(8).getDouble() }

  override fun peekChar(): Char = ensureActive { peek(2).getChar() }

  override fun peekLastChar(): Char = ensureActive { peekLast(2).getChar() }

  override fun peekLineBytes(): ByteArray = peekLineBytesOrNull() ?: throw ChannelEmptyException()

  override fun peekLineBytesOrNull(): ByteArray? {
    while (true) {
      startCursor.index
    }
    "".indexOf()
  }

  override fun peekLastLineBytes(): ByteArray {
    TODO("Not yet implemented")
  }

  override fun peekLastLineBytesOrNull(): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun peekRangeBytes(startIndex: Long, endIndex: Long): ByteArray {
    TODO("Not yet implemented")
  }

  override fun peekAllBytes(): ByteArray {
    TODO("Not yet implemented")
  }

  override fun peekAllBytesOrNull(): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun popAt(index: Long): Byte {
    TODO("Not yet implemented")
  }

  override fun popShort(): Short {
    TODO("Not yet implemented")
  }

  override fun popLastShort(): Short {
    TODO("Not yet implemented")
  }

  override fun popInt(): Int {
    TODO("Not yet implemented")
  }

  override fun popLastInt(): Int {
    TODO("Not yet implemented")
  }

  override fun popLong(): Long {
    TODO("Not yet implemented")
  }

  override fun popLastLong(): Long {
    TODO("Not yet implemented")
  }

  override fun popFloat(): Float {
    TODO("Not yet implemented")
  }

  override fun popLastFloat(): Float {
    TODO("Not yet implemented")
  }

  override fun popDouble(): Double {
    TODO("Not yet implemented")
  }

  override fun popLastDouble(): Double {
    TODO("Not yet implemented")
  }

  override fun popChar(): Char {
    TODO("Not yet implemented")
  }

  override fun popLastChar(): Char {
    TODO("Not yet implemented")
  }

  override fun popLineBytes(): ByteArray {
    TODO("Not yet implemented")
  }

  override fun popLastLineBytes(): ByteArray {
    TODO("Not yet implemented")
  }

  override fun popLineBytesOrNull(): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun popLastLineBytesOrNull(): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun popAllBytes(): ByteArray {
    TODO("Not yet implemented")
  }

  override fun popAllBytesOrNull(): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun popRangeBytes(startIndex: Long, endIndex: Long): ByteArray {
    TODO("Not yet implemented")
  }

  override fun dropAt(index: Long) {
    TODO("Not yet implemented")
  }

  override fun dropShort() {
    TODO("Not yet implemented")
  }

  override fun dropLastShort() {
    TODO("Not yet implemented")
  }

  override fun dropInt() {
    TODO("Not yet implemented")
  }

  override fun dropLastInt() {
    TODO("Not yet implemented")
  }

  override fun dropLong() {
    TODO("Not yet implemented")
  }

  override fun dropLastLong() {
    TODO("Not yet implemented")
  }

  override fun dropFloat() {
    TODO("Not yet implemented")
  }

  override fun dropLastFloat() {
    TODO("Not yet implemented")
  }

  override fun dropDouble() {
    TODO("Not yet implemented")
  }

  override fun dropLastDouble() {
    TODO("Not yet implemented")
  }

  override fun dropChar() {
    TODO("Not yet implemented")
  }

  override fun dropLastChar() {
    TODO("Not yet implemented")
  }

  override fun dropLine() {
    TODO("Not yet implemented")
  }

  override fun dropLastLine() {
    TODO("Not yet implemented")
  }

  override fun dropRange(startIndex: Long, endIndex: Long) {
    TODO("Not yet implemented")
  }

  override fun clear() {
    TODO("Not yet implemented")
  }

  override fun push(byte: Byte) {
    TODO("Not yet implemented")
  }

  override fun push(short: Short) {
    TODO("Not yet implemented")
  }

  override fun push(int: Int) {
    TODO("Not yet implemented")
  }

  override fun push(long: Long) {
    TODO("Not yet implemented")
  }

  override fun push(float: Float) {
    TODO("Not yet implemented")
  }

  override fun push(double: Double) {
    TODO("Not yet implemented")
  }

  override fun push(char: Char) {
    TODO("Not yet implemented")
  }

  override fun pushLast(byte: Byte) {
    TODO("Not yet implemented")
  }

  override fun pushLast(short: Short) {
    TODO("Not yet implemented")
  }

  override fun pushLast(int: Int) {
    TODO("Not yet implemented")
  }

  override fun pushLast(long: Long) {
    TODO("Not yet implemented")
  }

  override fun pushLast(float: Float) {
    TODO("Not yet implemented")
  }

  override fun pushLast(double: Double) {
    TODO("Not yet implemented")
  }

  override fun pushLast(char: Char) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, byte: Byte) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, src: ByteArray, cutStartIndex: Int, cutEndIndex: Int) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, short: Short) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, int: Int) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, long: Long) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, float: Float) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, double: Double) {
    TODO("Not yet implemented")
  }

  override fun pushTo(index: Long, char: Char) {
    TODO("Not yet implemented")
  }

  override fun replace(index: Long, byte: Byte) {
    TODO("Not yet implemented")
  }

  override fun flush() {
    TODO("Not yet implemented")
  }

  override fun isOpen(): Boolean = _isOpen.ifBoolPlaceholder {
    nioChannel.isOpen.also { _isOpen = it.toInt() }
  }

  override fun close() {
    TODO("Not yet implemented")
  }


  ////////////////////////////////////////////////////////////////////
  ////                   Internal implementation                  ////
  ////////////////////////////////////////////////////////////////////

  @Volatile private var _buffer: ByteBuffer? = null
  @Volatile private var _startCursor: DataChannel.Cursor? = null
  @Volatile private var _endCursor: DataChannel.Cursor? = null
  @Volatile private var _isOpen: Int = IntPlaceholder
  @Volatile private var _size: Long = LongPlaceholder

  private val buffer: ByteBuffer
    get() = _buffer ?: ByteBuffer.allocate(8192).also { _buffer = it }

  private inline fun <reified R> ensureActive(block: () -> R): R {
    require(isOpen()) { "The channel has been closed" }
    throwIf(size < 0L) { ChannelEmptyException("channel size < 0: $size") }
    return try {
      val result = block()
      throwIf(result == -1) { ChannelEmptyException("the channel has reached end-of-stream") }
      result
    } catch (eof: EOFException) {
      throw ChannelEmptyException("EOF!")
    }
  }

  private fun peek(count: Int) = buffer.limit(count).also {
    nioChannel.position(startCursor.index).read(it)
    startCursor.moveRight(count)
  }

  private fun peekLast(count: Int): ByteBuffer = buffer.limit(count).also {
    nioChannel.position(endCursor.index).read(it)
    endCursor.moveLeft(count)
  }


  inner class CursorImpl(override var index: Long) : DataChannel.Cursor {
    override val isReachStart: Boolean
      get() = TODO("Not yet implemented")

    override val isReachEnd: Boolean
      get() = TODO("Not yet implemented")

    override fun moveUp(repeat: Int): DataChannel.Cursor = apply {
      repeat(repeat) { moveUp() }
    }

    override fun moveDown(repeat: Int): DataChannel.Cursor = apply {
      repeat(repeat) { moveDown() }
    }

    override fun remember(): DataChannel.Cursor {
      TODO("Not yet implemented")
    }

    override fun restore(): DataChannel.Cursor {
      TODO("Not yet implemented")
    }

    private fun moveUp() {
      while (true) {
        index -= 2
        val read = nioChannel.position(index).read(buffer.limit(2))
        if (read == -1) break
        when (buffer.get()) {
          CR -> try {
            // Combined with the next one is `\r\n`
            if (buffer.get() == LF) index++
            break
          } catch (e: BufferUnderflowException) {
            // Just `\r`
            break
          }

          // Just `\n`
          LF -> break
        }
      }
    }

    private fun moveDown() {
      while (true) {
        val read = nioChannel.position(index++).read(buffer.limit(2))
        if (read == -1) break
        when (buffer.get()) {
          CR -> try {
            // Combined with the next one is `\r\n`
            if (buffer.get() == LF) index++
            break
          } catch (e: BufferUnderflowException) {
            // Just `\r`
            break
          }

          // Just `\n`
          LF -> break
        }
      }
    }
  }

  companion object {
    const val LF = '\n'.code.toByte()
    const val CR = '\r'.code.toByte()
  }
}