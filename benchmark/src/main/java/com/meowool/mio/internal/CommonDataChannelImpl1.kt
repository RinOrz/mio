@file:Suppress("NOTHING_TO_INLINE", "UsePropertyAccessSyntax", "OVERRIDE_BY_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.CarriageReturn
import com.meowool.mio.ChannelEmptyException
import com.meowool.mio.DataChannel
import com.meowool.mio.channel.ByteOrder
import com.meowool.mio.LineFeed
import com.meowool.sweekt.array.ByteArrayBuilder
import com.meowool.sweekt.array.buildByteArray
import com.meowool.sweekt.throwIf
import java.io.EOFException
import java.nio.channels.ClosedChannelException

/**
 * @author 凛 (https://github.com/RinOrz)
 */
internal abstract class CommonDataChannelImpl1 : DataChannel {
  final override inline val firstIndex: Long inline get() = 0

  final override inline val lastIndex: Long inline get() = size - 1

  override var order: ByteOrder = ByteOrder.NativeEndian

  override val startCursor: CursorImpl
    get() = _startCursor ?: CursorImpl(firstIndex).also { _startCursor = it }

  override val endCursor: CursorImpl
    get() = _endCursor ?: CursorImpl(lastIndex).also { _endCursor = it }

  override fun peekAt(index: Long): Byte = seek(index).load().getByte()

  override fun peekOrNullAt(index: Long): Byte? = seek(index).loadOrNull()?.getByte()

  override fun peek(): Byte = consume().getByte()

  override fun peekOrNull(): Byte? = consumeOrNull()?.getByte()

  override fun peekLast(): Byte = consumeLast().getByte()

  override fun peekLastOrNull(): Byte? = consumeLastOrNull()?.getByte()

  override fun peekShort(): Short = consume(ShortSize).getShort()

  override fun peekShortOrNull(): Short? = consumeOrNull(ShortSize)?.getShort()

  override fun peekLastShort(): Short = consumeLast(ShortSize).getShort()

  override fun peekLastShortOrNull(): Short? = consumeLastOrNull(ShortSize)?.getShort()

  override fun peekInt(): Int = consume(IntSize).getInt()

  override fun peekIntOrNull(): Int? = consumeOrNull(IntSize)?.getInt()

  override fun peekLastInt(): Int = consumeLast(IntSize).getInt()

  override fun peekLastIntOrNull(): Int? = consumeLastOrNull(IntSize)?.getInt()

  override fun peekLong(): Long = consume(LongSize).getLong()

  override fun peekLongOrNull(): Long? = consumeOrNull(LongSize)?.getLong()

  override fun peekLastLong(): Long = consumeLast(LongSize).getLong()

  override fun peekLastLongOrNull(): Long? = consumeLastOrNull(LongSize)?.getLong()

  override fun peekFloat(): Float = consume(FloatSize).getFloat()

  override fun peekFloatOrNull(): Float? = consumeOrNull(FloatSize)?.getFloat()

  override fun peekLastFloat(): Float = consumeLast(FloatSize).getFloat()

  override fun peekLastFloatOrNull(): Float? = consumeLastOrNull(FloatSize)?.getFloat()

  override fun peekDouble(): Double = consume(DoubleSize).getDouble()

  override fun peekDoubleOrNull(): Double? = consumeOrNull(DoubleSize)?.getDouble()

  override fun peekLastDouble(): Double = consumeLast(DoubleSize).getDouble()

  override fun peekLastDoubleOrNull(): Double? = consumeLastOrNull(DoubleSize)?.getDouble()

  override fun peekChar(): Char = consume(CharSize).getChar()

  override fun peekCharOrNull(): Char? = consumeOrNull(CharSize)?.getChar()

  override fun peekLastChar(): Char = consumeLast(CharSize).getChar()

  override fun peekLastCharOrNull(): Char? = consumeLastOrNull(CharSize)?.getChar()

  override fun peekBytes(count: Int): ByteArray = consume(count).getAll()

  override fun peekBytesOrNull(count: Int): ByteArray? = consumeOrNull(count)?.getAll()

  override fun peekLastBytes(count: Int): ByteArray = consumeLast(count).getAll()

  override fun peekLastBytesOrNull(count: Int): ByteArray? = consumeLastOrNull(count)?.getAll()

  override fun peekLine(): String =
    peekLineOrNull() ?: throw ChannelEmptyException()

  override fun peekLineOrNull(): String? = when {
    startCursor.isReachEnd -> null
    else -> buildString(8) {
      visitByteOfLine { append(it.toInt().toChar()) }
    }
  }

  override fun peekLastLine(): String =
    peekLastLineOrNull() ?: throw ChannelEmptyException()

  override fun peekLastLineOrNull(): String? = when {
    endCursor.isReachStart -> null
    else -> buildString(8) {
      visitByteOfLastLine { append(it.toInt().toChar()) }
      reverse()
    }
  }

  override fun peekLineBytes(): ByteArray =
    peekLineBytesOrNull() ?: throw ChannelEmptyException()

  override fun peekLineBytesOrNull(): ByteArray? = when {
    startCursor.isReachEnd -> null
    else -> buildByteArray(8) { visitByteOfLine(collect = ::append) }
  }

  override fun peekLastLineBytes(): ByteArray =
    peekLastLineBytesOrNull() ?: throw ChannelEmptyException()

  override fun peekLastLineBytesOrNull(): ByteArray? = when {
    endCursor.isReachStart -> null
    else -> buildByteArray(8) {
      visitByteOfLastLine(collect = ::append)
      reverse()
    }
  }

  override fun peekRangeBytes(startIndex: Long, endIndex: Long): ByteArray =
    seek(startIndex).load((endIndex - startIndex).toInt()).getAll()

  override fun peekAllBytes(): ByteArray =
    seek(firstIndex).load(size.toInt()).getAll()

  override fun peekAllBytesOrNull(): ByteArray? =
    seek(firstIndex).loadOrNull(size.toInt())?.getAll()

  override fun popAt(index: Long): Byte = seek(index).load().getByte()

  override fun popOrNullAt(index: Long): Byte? = seek(index).loadOrNull()?.getByte()

  override fun pop(): Byte {
    TODO("Not yet implemented")
  }

  override fun popOrNull(): Byte? {
    123 shl
    TODO("Not yet implemented")
  }

  override fun popLast(): Byte {
    TODO("Not yet implemented")
  }

  override fun popLastOrNull(): Byte? {
    TODO("Not yet implemented")
  }

  override fun popShort(): Short {
    TODO("Not yet implemented")
  }

  override fun popShortOrNull(): Short? {
    TODO("Not yet implemented")
  }

  override fun popLastShort(): Short {
    TODO("Not yet implemented")
  }

  override fun popLastShortOrNull(): Short? {
    TODO("Not yet implemented")
  }

  override fun popInt(): Int {
    TODO("Not yet implemented")
  }

  override fun popIntOrNull(): Int? {
    TODO("Not yet implemented")
  }

  override fun popLastInt(): Int {
    TODO("Not yet implemented")
  }

  override fun popLastIntOrNull(): Int? {
    TODO("Not yet implemented")
  }

  override fun popLong(): Long {
    TODO("Not yet implemented")
  }

  override fun popLongOrNull(): Long? {
    TODO("Not yet implemented")
  }

  override fun popLastLong(): Long {
    TODO("Not yet implemented")
  }

  override fun popLastLongOrNull(): Long? {
    TODO("Not yet implemented")
  }

  override fun popFloat(): Float {
    TODO("Not yet implemented")
  }

  override fun popFloatOrNull(): Float? {
    TODO("Not yet implemented")
  }

  override fun popLastFloat(): Float {
    TODO("Not yet implemented")
  }

  override fun popLastFloatOrNull(): Float? {
    TODO("Not yet implemented")
  }

  override fun popDouble(): Double {
    TODO("Not yet implemented")
  }

  override fun popDoubleOrNull(): Double? {
    TODO("Not yet implemented")
  }

  override fun popLastDouble(): Double {
    TODO("Not yet implemented")
  }

  override fun popLastDoubleOrNull(): Double? {
    TODO("Not yet implemented")
  }

  override fun popChar(): Char {
    TODO("Not yet implemented")
  }

  override fun popCharOrNull(): Char? {
    TODO("Not yet implemented")
  }

  override fun popLastChar(): Char {
    TODO("Not yet implemented")
  }

  override fun popLastCharOrNull(): Char? {
    TODO("Not yet implemented")
  }

  override fun popBytes(count: Int): ByteArray {
    TODO("Not yet implemented")
  }

  override fun popBytesOrNull(count: Int): ByteArray? {
    TODO("Not yet implemented")
  }

  override fun popLastBytes(count: Int): ByteArray {
    TODO("Not yet implemented")
  }

  override fun popLastBytesOrNull(count: Int): ByteArray? {
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

  override fun dropBoolean() {
    TODO("Not yet implemented")
  }

  override fun dropLastBoolean() {
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

  override fun close() {
    // TODO("Not yet implemented")
  }


  ////////////////////////////////////////////////////////////////////////////
  ////                            Internal Apis                           ////
  ////////////////////////////////////////////////////////////////////////////

  private var _startCursor: CursorImpl? = null
  private var _endCursor: CursorImpl? = null

  /**
   * A list of real indices, each index of the list (virtual cursor) corresponds to the real
   * index value of the underlying data resource.
   */
  private var _realIndices: LongArrayList? = null

  private val realIndices: LongArrayList
    get() = _realIndices ?: LongArrayList(size shl 1).also {
      // Initialize virtual and real peer indices
      for (index in 0 until size) it[index] = index
      _realIndices = it
    }

  /** Seeks to the data of specified index in the platform buffer. */
  abstract fun seek(index: Long): CommonDataChannelImpl1

  /** Loads data to platform buffer. */
  abstract fun load(
    cursor: DataChannel.Cursor = startCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1
  abstract fun load(
    count: Int,
    cursor: DataChannel.Cursor = startCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1
  abstract fun loadOrNull(
    cursor: DataChannel.Cursor = startCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1?
  abstract fun loadOrNull(
    count: Int,
    cursor: DataChannel.Cursor = startCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1?
  abstract fun loadLast(
    cursor: DataChannel.Cursor = endCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1
  abstract fun loadLast(
    count: Int,
    cursor: DataChannel.Cursor = endCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1
  abstract fun loadLastOrNull(
    cursor: DataChannel.Cursor = endCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1?
  abstract fun loadLastOrNull(
    count: Int,
    cursor: DataChannel.Cursor = endCursor,
    consume: Boolean = false
  ): CommonDataChannelImpl1?

  /** Gets data from platform buffer. */
  abstract fun getByte(): Byte
  abstract fun getChar(): Char
  abstract fun getFloat(): Float
  abstract fun getShort(): Short
  abstract fun getDouble(): Double
  abstract fun getLong(): Long
  abstract fun getInt(): Int
  abstract fun getAll(): ByteArray

  /** Forgets the real data of specified [index] of channel. */
  protected inline fun forget(index: Long): CommonDataChannelImpl1 =
    apply { realIndices.removeAt(index) }

  /**
   * Returns the real index corresponding to the virtual (cursor) index.
   * Because some virtual indices may be dropped.
   */
  protected fun getRealIndex(virtualIndex: Long) =
    _realIndices?.get(virtualIndex) ?: virtualIndex

  /** Runs [action] ensure available. */
  protected inline fun <R> available(action: () -> R): R {
    throwIf(isOpen().not()) { ClosedChannelException() }
    throwIf(size < 0L) { ChannelEmptyException("Channel size < 0: $size") }
    return try {
      action().also { result ->
        throwIf(result == -1) { ChannelEmptyException("The channel has reached end-of-stream.") }
      }
    } catch (_: EOFException) {
      throw ChannelEmptyException("EOF!")
    }
  }

  private inline fun consume(cursor: DataChannel.Cursor = startCursor, ) =
    seek(cursor.index).load().apply { cursor.index++ }

  private inline fun consumeLast(cursor: DataChannel.Cursor = endCursor) =
    seek(cursor.index).load().apply { cursor.index-- }

  private inline fun consumeOrNull(cursor: DataChannel.Cursor = startCursor) =
    seek(cursor.index).loadOrNull().apply { cursor.index++ }

  private inline fun consumeLastOrNull(cursor: DataChannel.Cursor = endCursor) =
    seek(cursor.index).loadOrNull().apply { cursor.index-- }

  private inline fun consume(count: Int) =
    seek(startCursor.index).load(count).apply { startCursor.index += count }

  private inline fun consumeLast(count: Int) =
    seek(endCursor.index).load(count).apply { endCursor.index -= count }

  private inline fun consumeOrNull(count: Int) =
    seek(startCursor.index).loadOrNull(count).apply { startCursor.index += count }

  private inline fun consumeLastOrNull(count: Int) =
    seek(endCursor.index).loadOrNull(count).apply { endCursor.index -= count }

  /** Reads the bytes of the current [startCursor] line. */
  private inline fun visitByteOfLine(
    cursor: DataChannel.Cursor = startCursor,
    collect: (Byte) -> Unit = {},
  ) {
    while (true) {
      when (val byte = consumeOrNull(cursor)?.getByte() ?: break) {
        // In the case of `\r` or `\r\n`
        CarriageReturn -> {
          // Read next one byte
          consumeOrNull(cursor)?.let {
            // Reset mark if combine with the next one is not `\r\n`
            if (it.getByte() != LineFeed) seek(startCursor.index--)
          }
          // Just `\r`, break the loop directly
          break
        }

        // Break the loop directly if the byte is `\n`
        LineFeed -> break

        // Collecting byte
        else -> collect(byte)
      }
    }
  }

  /**
   * Reads the bytes of the current [endCursor] line.
   *
   * Note that you need to manually reverse the [collect] byte.
   *
   * The result is `^..^`:
   * ```
   * \nTWO_LINE
   *   ^^^^^^^^
   * ```
   */
  private inline fun visitByteOfLastLine(
    cursor: DataChannel.Cursor = endCursor,
    onComplete: (symbolLength: Int) -> Unit = {},
    collect: (Byte) -> Unit = {},
  ) {
    var symbolLength = 0
    while (true) {
      val byte = consumeLastOrNull(cursor)?.getByte() ?: break
      when (byte) {
        // In the case of `\n` or `\n\r` (reverse of `\r\n`)
        LineFeed -> {
          symbolLength = 1
          // Read previous one byte
          consumeLastOrNull(cursor)?.let {
            when (it.getByte()) {
              CarriageReturn -> symbolLength = 2
              // Reset mark if combine with the previous one is not `\n\r` (reverse of `\r\n`)
              else -> seek(cursor.index++)
            }
          }
          // Just `\r`, break the loop directly
          break
        }

        // Break the loop directly if the byte is `\r`
        CarriageReturn -> {
          symbolLength = 1
          break
        }

        // Collecting byte
        else -> collect(byte)
      }
    }
    onComplete(symbolLength)
  }

  inner class CursorImpl(override var index: Long) : DataChannel.Cursor {
    private val _bak = index
    private var _prevLine: ByteArrayBuilder? = null
    private var _nextLine: ByteArrayBuilder? = null
    private var _remember: Long = firstIndex

    val prevLine: ByteArrayBuilder
      get() = _prevLine ?: ByteArrayBuilder(8).also { _prevLine = it }

    val nextLine: ByteArrayBuilder
      get() = _nextLine ?: ByteArrayBuilder(8).also { _nextLine = it }

    override val isReachStart: Boolean
      get() = index <= firstIndex

    override val isReachEnd: Boolean
      get() = index >= lastIndex

    inline fun moveRight() = apply { this.index++ }

    inline fun moveLeft() = apply { this.index-- }

    override inline fun moveTo(index: Long) = apply { this.index = index }

    override fun moveToFirst() = apply { this.index = 0 }

    override fun moveToLast() = apply { this.index = lastIndex }

    override fun moveRight(repeat: Int) = apply { this.index += repeat }

    override fun moveLeft(repeat: Int) = apply { this.index -= repeat }

    override fun moveUp(repeat: Int) = apply {
      repeat(repeat) {
        // Read the end of the previous line
        visitByteOfLastLine(this)
        // Read the starting of the previous line (without previous two line end symbol)
        visitByteOfLastLine(this, onComplete = { symbolLength ->
          index = index.coerceAtLeast(firstIndex) + symbolLength
        })
      }
    }

    override fun moveDown(repeat: Int) = apply {
      repeat(repeat) {
        visitByteOfLine(this)
        index = index.coerceAtMost(lastIndex)
      }
    }

    override fun remember() = apply {
      _remember = index
    }

    override fun restore() = apply {
      index = _remember
    }

    override fun reset() = apply {
      index = _bak
    }

    override fun toString(): String = index.toString()
  }


  private companion object {
    const val LF = '\n'.code.toByte()
    const val CR = '\r'.code.toByte()

    /**
     * Count of byte occupied by the type.
     */
    const val ShortSize = 2
    const val IntSize = 4
    const val LongSize = 8
    const val FloatSize = 4
    const val DoubleSize = 8
    const val CharSize = 2
  }
}