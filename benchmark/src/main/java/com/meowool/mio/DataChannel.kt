package com.meowool.mio

import com.meowool.mio.channel.ByteOrder
import com.meowool.sweekt.ifTrue
import java.io.Closeable
import java.io.Flushable
import java.nio.charset.Charset

/**
 * Represents a bidirectional data channel with movable cursors ([startCursor], [endCursor]), and
 * provides support for random read-write access to data of channel.
 *
 * Note that this channel holds a buffer, and all write operations will be temporarily stored in
 * the buffer. Only after calling [flush] or [close] will all changes in the buffer be synchronized
 * to the final destination.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface DataChannel : Closeable, Flushable {

  /**
   * The size of this channel (in bytes).
   *
   * If changes the number of bytes in the channel to [size] and the new size is smaller. This will
   * remove bytes from the end. It will add empty bytes to the end if it is larger.
   *
   * Note that this value cannot be negative.
   */
  var size: Long

  /**
   * The first index of this channel.
   */
  val firstIndex: Long get() = 0

  /**
   * The last index of this channel.
   */
  val lastIndex: Long get() = size - 1

  /**
   * The byte order of this channel, the default order is [ByteOrder.NativeEndian].
   */
  var order: ByteOrder


  ////////////////////////////////////////////////////////////////////////////
  ////                            Data Cursors                            ////
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the cursor at the beginning of this channel.
   *
   * @see endCursor
   */
  val startCursor: Cursor

  /**
   * Returns the cursor at the ending of this channel.
   * Usually used for data access by the `**Last**` method.
   *
   * @see startCursor
   */
  val endCursor: Cursor


  /**
   * Reset the state of the two cursors.
   *
   * @see Cursor.reset
   */
  fun resetCursors() {
    startCursor.reset()
    endCursor.reset()
  }


  ////////////////////////////////////////////////////////////////////////////
  ////               Peek (only read without deleting) Apis               ////
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Returns a byte at the specified [index] from this channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the [size]
   * of this channel.
   *
   * @see popAt
   */
  @Throws(ChannelEmptyException::class, IndexOutOfBoundsException::class)
  fun peekAt(index: Long): Byte

  /**
   * Returns a byte object (boxing) at the specified [index] from this channel, or `null` if there
   * are no more bytes from this channel.
   *
   * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the [size]
   * of this channel.
   *
   * @see popOrNullAt
   */
  @Throws(IndexOutOfBoundsException::class)
  fun peekOrNullAt(index: Long): Byte?

  /** For more details: [peekAt] */
  operator fun get(index: Long): Byte = peekAt(index)

  /**
   * Returns a byte at the current index of the [startCursor] from this channel, and then moves the
   * cursor to after the byte being peeked: `startCursor.moveRight()`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see pop
   */
  @Throws(ChannelEmptyException::class)
  fun peek(): Byte

  /**
   * Returns a byte object (boxing) at the current index of the [startCursor] from this channel,
   * and then moves the cursor to after the byte being peeked: `startCursor.moveRight()`, or `null`
   * if there are no more bytes from this channel.
   *
   * @see popOrNull
   */
  fun peekOrNull(): Byte?

  /**
   * Returns a byte at the current index of the [endCursor] from this channel, and then moves the
   * cursor to before the byte being peeked: `endCursor.moveLeft()`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLast
   */
  @Throws(ChannelEmptyException::class)
  fun peekLast(): Byte

  /**
   * Returns a byte object (boxing) at the current index of the [endCursor] from this channel, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft()`, or `null` if
   * there are no more bytes from this channel.
   *
   * @see popLastOrNull
   */
  fun peekLastOrNull(): Byte?

  /**
   * Returns two bytes at the current index of the [startCursor] from this channel as a short, and
   * then moves the cursor to after the byte being peeked: `startCursor.moveRight(2)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popShort
   */
  @Throws(ChannelEmptyException::class)
  fun peekShort(): Short

  /**
   * Returns two bytes at the current index of the [startCursor] from this channel as a short
   * object (boxing), and then moves the cursor to after the byte being
   * peeked: `startCursor.moveRight(2)`, or `null` if there are no more bytes from this channel.
   *
   * @see popShortOrNull
   */
  fun peekShortOrNull(): Short?

  /**
   * Returns two bytes at the current index of the [endCursor] from this channel as a short, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft(2)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastShort
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastShort(): Short

  /**
   * Returns two bytes at the current index of the [endCursor] from this channel as a short
   * object (boxing), and then moves the cursor to before the byte being
   * peeked: `endCursor.moveLeft(2)`, or `null` if there are no more bytes from this channel.
   *
   * @see popLastShortOrNull
   */
  fun peekLastShortOrNull(): Short?

  /**
   * Returns four bytes at the current index of the [startCursor] from this channel as an int, and
   * then moves the cursor to after the byte being peeked: `startCursor.moveRight(4)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popInt
   */
  @Throws(ChannelEmptyException::class)
  fun peekInt(): Int

  /**
   * Returns four bytes at the current index of the [startCursor] from this channel as an int
   * object (boxing), and then moves the cursor to after the byte being
   * peeked: `startCursor.moveRight(4)`, or `null` if there are no more bytes from this channel.
   *
   * @see popIntOrNull
   */
  fun peekIntOrNull(): Int?

  /**
   * Returns four bytes at the current index of the [endCursor] from this channel as an int, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft(4)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastInt
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastInt(): Int

  /**
   * Returns four bytes at the current index of the [endCursor] from this channel as an int
   * object (boxing), and then moves the cursor to before the byte being
   * peeked: `endCursor.moveLeft(4)`, or `null` if there are no more bytes from this channel.
   *
   * @see popLastIntOrNull
   */
  fun peekLastIntOrNull(): Int?

  /**
   * Returns eight bytes at the current index of the [startCursor] from this channel as a long, and
   * then moves the cursor to after the byte being peeked: `startCursor.moveRight(8)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLong
   */
  @Throws(ChannelEmptyException::class)
  fun peekLong(): Long

  /**
   * Returns eight bytes at the current index of the [startCursor] from this channel as a long
   * object (boxing), and then moves the cursor to after the byte being
   * peeked: `startCursor.moveRight(8)`, or `null` if there are no more bytes from this channel.
   *
   * @see popLongOrNull
   */
  fun peekLongOrNull(): Long?

  /**
   * Returns eight bytes at the current index of the [endCursor] from this channel as a long, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft(8)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastLong
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastLong(): Long

  /**
   * Returns eight bytes at the current index of the [endCursor] from this channel as a long
   * object (boxing), and then moves the cursor to before the byte being
   * peeked: `endCursor.moveLeft(8)`, or `null` if there are no more bytes from this channel.
   *
   * @see popLastLongOrNull
   */
  fun peekLastLongOrNull(): Long?

  /**
   * Returns four bytes at the current index of the [startCursor] from this channel as a float, and
   * then moves the cursor to after the byte being peeked: `startCursor.moveRight(4)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popFloat
   */
  @Throws(ChannelEmptyException::class)
  fun peekFloat(): Float

  /**
   * Returns four bytes at the current index of the [startCursor] from this channel as a float
   * object (boxing), and then moves the cursor to after the byte being
   * peeked: `startCursor.moveRight(4)`, or `null` if there are no more bytes from this channel.
   *
   * @see popFloatOrNull
   */
  fun peekFloatOrNull(): Float?

  /**
   * Returns four bytes at the current index of the [endCursor] from this channel as a float, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft(4)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastFloat
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastFloat(): Float

  /**
   * Returns four bytes at the current index of the [endCursor] from this channel as a float
   * object (boxing), and then moves the cursor to before the byte being
   * peeked: `endCursor.moveLeft(4)`, or `null` if there are no more bytes from this channel.
   *
   * @see popLastFloatOrNull
   */
  fun peekLastFloatOrNull(): Float?

  /**
   * Returns eight bytes at the current index of the [startCursor] from this channel as a double,
   * and then moves the cursor to after the byte being peeked: `startCursor.moveRight(8)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popDouble
   */
  @Throws(ChannelEmptyException::class)
  fun peekDouble(): Double

  /**
   * Returns eight bytes at the current index of the [startCursor] from this channel as a double
   * object (boxing), and then moves the cursor to after the byte being
   * peeked: `startCursor.moveRight(8)`, or `null` if there are no more bytes from this channel.
   *
   * @see popDoubleOrNull
   */
  fun peekDoubleOrNull(): Double?

  /**
   * Returns eight bytes at the current index of the [endCursor] from this channel as a double, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft(8)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastDouble
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastDouble(): Double

  /**
   * Returns eight bytes at the current index of the [endCursor] from this channel as a double
   * object (boxing), and then moves the cursor to before the byte being
   * peeked: `endCursor.moveLeft(8)`, or `null` if there are no more bytes from this channel.
   *
   * @see popLastDoubleOrNull
   */
  fun peekLastDoubleOrNull(): Double?

  /**
   * Returns two bytes at the current index of the [startCursor] from this channel as a char,
   * and then moves the cursor to after the byte being peeked: `startCursor.moveRight(2)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popChar
   */
  @Throws(ChannelEmptyException::class)
  fun peekChar(): Char

  /**
   * Returns two bytes at the current index of the [startCursor] from this channel as a char
   * object (boxing), and then moves the cursor to after the byte being
   * peeked: `startCursor.moveRight(2)`, or `null` if there are no more bytes from this channel.
   *
   * @see popCharOrNull
   */
  fun peekCharOrNull(): Char?

  /**
   * Returns two bytes at the current index of the [endCursor] from this channel as a char, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft(2)`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastChar
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastChar(): Char

  /**
   * Returns two bytes at the current index of the [endCursor] from this channel as a char
   * object (boxing), and then moves the cursor to before the byte being
   * peeked: `endCursor.moveLeft(2)`, or `null` if there are no more bytes from this channel.
   *
   * @see popLastCharOrNull
   */
  fun peekLastCharOrNull(): Char?

  /**
   * Returns a byte at the current index of the [startCursor] from this channel as a boolean,
   * and then moves the cursor to after the byte being peeked: `startCursor.moveRight()`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popBoolean
   */
  @Throws(ChannelEmptyException::class)
  fun peekBoolean(): Boolean = peek() == 1.toByte()

  /**
   * Returns a byte at the current index of the [startCursor] from this channel as a boolean
   * object (boxing), and then moves the cursor to after the byte being
   * peeked: `startCursor.moveRight()`, or `null` if there are no more bytes from this channel.
   *
   * @see popBooleanOrNull
   */
  fun peekBooleanOrNull(): Boolean? = peekOrNull()?.let { it == 1.toByte() }

  /**
   * Returns a byte at the current index of the [endCursor] from this channel as a boolean, and
   * then moves the cursor to before the byte being peeked: `endCursor.moveLeft()`.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastBoolean
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastBoolean(): Boolean = peekLast() == 1.toByte()

  /**
   * Returns a byte at the current index of the [endCursor] from this channel as a boolean
   * object (boxing), and then moves the cursor to before the byte being
   * peeked: `endCursor.moveLeft()`, or `null` if there are no more bytes from this channel.
   *
   * @see popLastBooleanOrNull
   */
  fun peekLastBooleanOrNull(): Boolean? = peekLastOrNull()?.let { it == 1.toByte() }

  /**
   * Returns a string, starting from the current index of the [startCursor] from this channel, total
   * of [count] bytes, and then moves the cursor to after some bytes being
   * peeked: `startCursor.moveRight([count])`.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException the remaining bytes of this channel are less than the [count]
   *
   * @see popString
   */
  @Throws(ChannelEmptyException::class)
  fun peekString(count: Int, charset: Charset = Charsets.UTF_8): String =
    peekBytes(count).toString(charset)

  /**
   * Returns a string, starting from the current index of the [startCursor] from this channel, total
   * of [count] bytes, and then moves the cursor to after some bytes being
   * peeked: `startCursor.moveRight([count])`. Returns `null` if the remaining bytes of this channel
   * are less than the specified [count].
   *
   * @param charset the charset to use for decoding string
   *
   * @see popStringOrNull
   */
  fun peekStringOrNull(count: Int, charset: Charset = Charsets.UTF_8): String? =
    peekBytesOrNull(count)?.toString(charset)

  /**
   * Returns a string, starting from the current index of the [endCursor] from this channel, total
   * of [count] bytes, and then moves the cursor to before some bytes being
   * peeked: `endCursor.moveLeft([count])`.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException the remaining bytes in the front of this channel are less than
   * the [count]
   *
   * @see popLastString
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastString(count: Int, charset: Charset = Charsets.UTF_8): String =
    peekLastBytes(count).toString(charset)

  /**
   * Returns a string, starting from the current index of the [endCursor] from this channel, total
   * of [count] bytes, and then moves the cursor to before some bytes being
   * peeked: `endCursor.moveLeft([count])`. Returns `null` if the remaining bytes in the front of
   * this channel are less than the specified [count].
   *
   * @param charset the charset to use for decoding string
   *
   * @see popLastStringOrNull
   */
  fun peekLastStringOrNull(count: Int, charset: Charset = Charsets.UTF_8): String? =
    peekLastBytesOrNull(count)?.toString(charset)

  /**
   * Returns a byte array, starting from the current index of the [startCursor] from this channel,
   * total of [count] bytes, and then moves the cursor to after some bytes being
   * peeked: `startCursor.moveRight([count])`.
   *
   * @throws ChannelEmptyException the remaining bytes of this channel are less than the [count]
   *
   * @see popBytes
   */
  @Throws(ChannelEmptyException::class)
  fun peekBytes(count: Int): ByteArray

  /**
   * Returns a byte array, starting from the current index of the [startCursor] from this channel,
   * total of [count] bytes, and then moves the cursor to after some bytes being
   * peeked: `startCursor.moveRight([count])`. Returns `null` if the remaining bytes of this channel
   * are less than the specified [count].
   *
   * @see popBytesOrNull
   */
  fun peekBytesOrNull(count: Int): ByteArray?

  /**
   * Returns a byte array, starting from the current index of the [endCursor] from this channel,
   * total of [count] bytes, and then moves the cursor to before some bytes being
   * peeked: `endCursor.moveLeft([count])`.
   *
   * @throws ChannelEmptyException the remaining bytes in the front of this channel are less than
   * the [count]
   *
   * @see popLastBytes
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastBytes(count: Int): ByteArray

  /**
   * Returns a byte array, starting from the current index of the [endCursor] from this channel,
   * total of [count] bytes, and then moves the cursor to before some bytes being
   * peeked: `endCursor.moveLeft([count])`. Returns `null` if the remaining bytes in the front of
   * this channel are less than the specified [count].
   *
   * @see popLastBytesOrNull
   */
  fun peekLastBytesOrNull(count: Int): ByteArray?

  /**
   * Returns an utf-8 line of string at the current index of the [startCursor] from this channel,
   * and then moves the cursor to after the line being peeked: `startCursor.moveDown()`.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see Charsets.UTF_8
   * @see popLine
   */
  @Throws(ChannelEmptyException::class)
  fun peekLine(): String

  /**
   * Returns a line of string at the current index of the [startCursor] from this channel, and then
   * moves the cursor to after the line being peeked: `startCursor.moveDown()`.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLine
   */
  @Throws(ChannelEmptyException::class)
  fun peekLine(charset: Charset): String =
    peekLineBytes().toString(charset)

  /**
   * Returns an utf-8 line of string at the current index of the [startCursor] from this channel,
   * and then moves the cursor to after the line being peeked: `startCursor.moveDown()`, or `null`
   * if there are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see Charsets.UTF_8
   * @see popLineOrNull
   */
  fun peekLineOrNull(): String?

  /**
   * Returns a line of string at the current index of the [startCursor] from this channel, and then
   * moves the cursor to after the line being peeked: `startCursor.moveDown()`, or `null` if there are
   * no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @see popLineOrNull
   */
  fun peekLineOrNull(charset: Charset): String? =
    peekLineBytesOrNull()?.toString(charset)

  /**
   * Returns an utf-8 line of string at the current index of the [endCursor] from this channel, and
   * then moves the cursor to before the line being peeked: `endCursor.moveUp()`.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see Charsets.UTF_8
   * @see popLastLine
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastLine(): String

  /**
   * Returns a line of string at the current index of the [endCursor] from this channel, and then
   * moves the cursor to before the line being peeked: `endCursor.moveUp()`.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastLine
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastLine(charset: Charset): String =
    peekLastLineBytes().toString(charset)

  /**
   * Returns an utf-8 line of byte array at the current index of the [endCursor] from this channel,
   * and then moves the cursor to before the line being peeked: `endCursor.moveUp()`, or `null` if
   * there are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see Charsets.UTF_8
   * @see popLastLineOrNull
   */
  fun peekLastLineOrNull(): String?

  /**
   * Returns a line of byte array at the current index of the [endCursor] from this channel, and
   * then moves the cursor to before the line being peeked: `endCursor.moveUp()`, or `null` if there
   * are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @see popLastLineOrNull
   */
  fun peekLastLineOrNull(charset: Charset): String? =
    peekLastLineBytesOrNull()?.toString(charset)

  /**
   * Returns a line of byte array at the current index of the [startCursor] from this channel, and
   * then moves the cursor to after the line being peeked: `startCursor.moveDown()`.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLineBytes
   */
  @Throws(ChannelEmptyException::class)
  fun peekLineBytes(): ByteArray

  /**
   * Returns a line of byte array at the current index of the [startCursor] from this channel, and
   * then moves the cursor to after the line being peeked: `startCursor.moveDown()`, or `null` if there
   * are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see popLineBytesOrNull
   */
  fun peekLineBytesOrNull(): ByteArray?

  /**
   * Returns a line of byte array at the current index of the [endCursor] from this channel, and
   * then moves the cursor to before the line being peeked: `endCursor.moveUp()`.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popLastLineBytes
   */
  @Throws(ChannelEmptyException::class)
  fun peekLastLineBytes(): ByteArray

  /**
   * Returns a line of byte array at the current index of the [endCursor] from this channel, and
   * then moves the cursor to before the line being peeked: `endCursor.moveUp()`, or `null` if there
   * are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see popLastLineBytesOrNull
   */
  fun peekLastLineBytesOrNull(): ByteArray?

  /**
   * Returns a string from a range of this channel starting at the [startIndex] and ending right
   * before the [endIndex].
   *
   * By default, returns current index of the [startCursor] and all the remaining bytes afterwards.
   *
   * @param startIndex the start index (inclusive).
   * @param endIndex the end index (exclusive).
   * @param charset the charset to use for decoding string
   *
   * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
   *
   * @see popRange
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun peekRange(
    startIndex: Long = startCursor.index,
    endIndex: Long = endCursor.index + 1,
    charset: Charset = Charsets.UTF_8,
  ): String = peekRangeBytes(startIndex, endIndex).toString(charset)
  /**
   * Returns a byte array from a range of this channel starting at the [startIndex] and ending
   * right before the [endIndex].
   *
   * @param startIndex the start index (inclusive).
   * @param endIndex the end index (exclusive).
   *
   * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
   *
   * @see popRangeBytes
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun peekRangeBytes(
    startIndex: Long = startCursor.index,
    endIndex: Long = endCursor.index + 1,
  ): ByteArray

  /**
   * Returns all bytes from this channel as string.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popAll
   */
  @Throws(ChannelEmptyException::class)
  fun peekAll(charset: Charset = Charsets.UTF_8): String = peekAllBytes().toString(charset)

  /**
   * Returns all bytes from this channel as string, or `null` if there are no more bytes in
   * this channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @see popAllOrNull
   */
  fun peekAllOrNull(charset: Charset = Charsets.UTF_8): String? =
    peekAllBytesOrNull()?.toString(charset)

  /**
   * Returns all bytes from this channel as byte array.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @see popAllBytes
   */
  @Throws(ChannelEmptyException::class)
  fun peekAllBytes(): ByteArray

  /**
   * Returns all bytes from this channel as byte array, or `null` if there are no more bytes in
   * this channel.
   *
   * @see popAllBytesOrNull
   */
  fun peekAllBytesOrNull(): ByteArray?

  /**
   * Returns all utf-8 lines bytes from this channel as list of string, or empty if there are no
   * more bytes in this channel.
   *
   * @param reversed whether returns the reversed list of lines (from last line to first line).
   * @see popAllLines
   */
  fun peekAllLines(
    reversed: Boolean = false,
  ): List<String> = arrayListOf<String>().apply {
    forEachLine(reversed, action = ::add)
  }

  /**
   * Returns all lines bytes from this channel as list of string, or empty if there are no more
   * bytes in this channel.
   *
   * @param reversed whether returns the reversed list of lines (from last line to first line).
   * @param charset character set to use for reading line.
   * @see popAllLines
   */
  fun peekAllLines(
    charset: Charset,
    reversed: Boolean = false,
  ): List<String> = arrayListOf<String>().apply {
    forEachLine(charset, reversed, action = ::add)
  }

  /**
   * Returns all lines bytes from this channel as list of byte array, or empty if there are no
   * more bytes in this channel.
   *
   * @param reversed whether returns the reversed list of lines (from last line to first line).
   * @see popAllLinesBytes
   */
  fun peekAllLinesBytes(reversed: Boolean = false): List<ByteArray> =
    arrayListOf<ByteArray>().apply { forEachByteLine(reversed, action = ::add) }


  ////////////////////////////////////////////////////////////////////////////
  ////                     Pop (read and remove) Apis                     ////
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Pops (removes and returns) a byte at the specified [index] from this channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the [size]
   * of this channel.
   *
   * @see peekAt
   * @see dropAt
   */
  @Throws(ChannelEmptyException::class, IndexOutOfBoundsException::class)
  fun popAt(index: Long): Byte

  /**
   * Pops (removes and returns) a byte object (boxing)  at the specified [index] from this channel,
   * or `null` if there are no more bytes from this channel.
   *
   * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the [size]
   * of this channel.
   *
   * @see peekOrNullAt
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popOrNullAt(index: Long): Byte?

  /**
   * Pops (removes and returns) a byte at the current index of the [startCursor] from this channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peek
   * @see drop
   */
  @Throws(ChannelEmptyException::class)
  fun pop(): Byte

  /**
   * Pops (removes and returns) a byte object (boxing) at the current index of the [startCursor]
   * from this channel, or `null` if there are no more bytes from this channel.
   *
   * @see peekOrNull
   */
  fun popOrNull(): Byte?

  /**
   * Pops (removes and returns) a byte at the current index of the [endCursor] from
   * this channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLast
   * @see dropLast
   */
  @Throws(ChannelEmptyException::class)
  fun popLast(): Byte

  /**
   * Pops (removes and returns) a byte object (boxing) at the current index of the [endCursor] from
   * this channel, or `null` if there are no more bytes from this channel.
   *
   * @see peekLastOrNull
   */
  fun popLastOrNull(): Byte?

  /**
   * Pops (removes and returns) two bytes at the current index of the [startCursor] from this
   * channel as a short.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekShort
   * @see dropShort
   */
  @Throws(ChannelEmptyException::class)
  fun popShort(): Short

  /**
   * Pops (removes and returns) two bytes at the current index of the [startCursor] from this
   * channel as a short object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekShortOrNull
   */
  fun popShortOrNull(): Short?

  /**
   * Pops (removes and returns) two bytes at the current index of the [endCursor] from this
   * channel as a short.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastShort
   * @see dropLastShort
   */
  @Throws(ChannelEmptyException::class)
  fun popLastShort(): Short

  /**
   * Pops (removes and returns) two bytes at the current index of the [endCursor] from this channel
   * as a short object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekLastShortOrNull
   */
  fun popLastShortOrNull(): Short?

  /**
   * Pops (removes and returns) four bytes at the current index of the [startCursor] from this
   * channel as an int.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekInt
   * @see dropInt
   */
  @Throws(ChannelEmptyException::class)
  fun popInt(): Int

  /**
   * Pops (removes and returns) four bytes at the current index of the [startCursor] from this
   * channel as an int object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekIntOrNull
   */
  fun popIntOrNull(): Int?

  /**
   * Pops (removes and returns) four bytes at the current index of the [endCursor] from this channel
   * as an int.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastInt
   * @see dropLastInt
   */
  @Throws(ChannelEmptyException::class)
  fun popLastInt(): Int

  /**
   * Pops (removes and returns) four bytes at the current index of the [endCursor] from this
   * channel as an int object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekLastIntOrNull
   */
  fun popLastIntOrNull(): Int?

  /**
   * Pops (removes and returns) eight bytes at the current index of the [startCursor] from this
   * channel as a long.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLong
   * @see dropLong
   */
  @Throws(ChannelEmptyException::class)
  fun popLong(): Long

  /**
   * Pops (removes and returns) eight bytes at the current index of the [startCursor] from this
   * channel as a long object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekLongOrNull
   */
  fun popLongOrNull(): Long?

  /**
   * Pops (removes and returns) eight bytes at the current index of the [endCursor] from this
   * channel as a long.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastLong
   * @see dropLastLong
   */
  @Throws(ChannelEmptyException::class)
  fun popLastLong(): Long

  /**
   * Pops (removes and returns) eight bytes at the current index of the [endCursor] from this
   * channel as a long object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekLastLongOrNull
   */
  fun popLastLongOrNull(): Long?

  /**
   * Pops (removes and returns) four bytes at the current index of the [startCursor] from this
   * channel as a float.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekFloat
   * @see dropFloat
   */
  @Throws(ChannelEmptyException::class)
  fun popFloat(): Float

  /**
   * Pops (removes and returns) four bytes at the current index of the [startCursor] from this
   * channel as a float object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekFloatOrNull
   */
  fun popFloatOrNull(): Float?

  /**
   * Pops (removes and returns) four bytes at the current index of the [endCursor] from this
   * channel as a float.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastFloat
   * @see dropLastFloat
   */
  @Throws(ChannelEmptyException::class)
  fun popLastFloat(): Float

  /**
   * Pops (removes and returns) four bytes at the current index of the [endCursor] from this channel
   * as a float object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekLastFloatOrNull
   */
  fun popLastFloatOrNull(): Float?

  /**
   * Pops (removes and returns) eight bytes at the current index of the [startCursor] from this
   * channel as a double.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekDouble
   * @see dropDouble
   */
  @Throws(ChannelEmptyException::class)
  fun popDouble(): Double

  /**
   * Pops (removes and returns) eight bytes at the current index of the [startCursor] from this
   * channel as a double object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekDoubleOrNull
   */
  fun popDoubleOrNull(): Double?

  /**
   * Pops (removes and returns) eight bytes at the current index of the [endCursor] from this
   * channel as a double.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastDouble
   * @see dropLastDouble
   */
  @Throws(ChannelEmptyException::class)
  fun popLastDouble(): Double

  /**
   * Pops (removes and returns) eight bytes at the current index of the [endCursor] from this
   * channel as a double object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekLastDoubleOrNull
   */
  fun popLastDoubleOrNull(): Double?

  /**
   * Pops (removes and returns) two bytes at the current index of the [startCursor] from this
   * channel as a char.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekChar
   * @see dropChar
   */
  @Throws(ChannelEmptyException::class)
  fun popChar(): Char

  /**
   * Pops (removes and returns) two bytes at the current index of the [startCursor] from this
   * channel as a char object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekCharOrNull
   */
  fun popCharOrNull(): Char?

  /**
   * Pops (removes and returns) two bytes at the current index of the [endCursor] from this channel
   * as a char.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastChar
   * @see dropLastChar
   */
  @Throws(ChannelEmptyException::class)
  fun popLastChar(): Char

  /**
   * Pops (removes and returns) two bytes at the current index of the [endCursor] from this channel
   * as a char object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekLastCharOrNull
   */
  fun popLastCharOrNull(): Char?

  /**
   * Pops (removes and returns) a byte at the current index of the [startCursor] from this channel
   * as a boolean.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekBoolean
   * @see dropBoolean
   */
  @Throws(ChannelEmptyException::class)
  fun popBoolean(): Boolean = pop() == 1.toByte()

  /**
   * Pops (removes and returns) a byte at the current index of the [startCursor] from this channel
   * as a boolean object (boxing), or `null` if there are no more bytes from this channel.
   *
   * @see peekBooleanOrNull
   */
  fun popBooleanOrNull(): Boolean? = popOrNull()?.let { it == 1.toByte() }

  /**
   * Pops (removes and returns) a byte at the current index of the [endCursor] from this channel as
   * a boolean.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastBoolean
   * @see dropLastBoolean
   */
  @Throws(ChannelEmptyException::class)
  fun popLastBoolean(): Boolean = popLast() == 1.toByte()

  /**
   * Pops (removes and returns) a byte at the current index of the [endCursor] from this channel
   * as a boolean object (boxing), and then moves the cursor to before the byte being, or `null` if
   * there are no more bytes from this channel.
   *
   * @see peekLastBooleanOrNull
   */
  fun popLastBooleanOrNull(): Boolean? = popLastOrNull()?.let { it == 1.toByte() }

  /**
   * Pops (removes and returns) a string, starting from the current index of the [startCursor] from
   * this channel, total f [count] bytes.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException the remaining bytes of this channel are less than the [count]
   *
   * @see peekString
   * @see drop
   */
  @Throws(ChannelEmptyException::class)
  fun popString(count: Int, charset: Charset = Charsets.UTF_8): String =
    popBytes(count).toString(charset)

  /**
   * Pops (removes and returns) a string, starting from the current index of the [startCursor] from
   * this channel, total of [count] bytes. Returns `null` if the remaining bytes of this channel
   * are less than the specified [count].
   *
   * @param charset the charset to use for decoding string
   *
   * @see peekStringOrNull
   * @see drop
   */
  fun popStringOrNull(count: Int, charset: Charset = Charsets.UTF_8): String? =
    popBytesOrNull(count)?.toString(charset)

  /**
   * Pops (removes and returns) a string, starting from the current index of the [endCursor] from
   * this channel, total of [count] bytes.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException the remaining bytes in the front of this channel are less than
   * the [count]
   *
   * @see peekLastString
   * @see dropLast
   */
  @Throws(ChannelEmptyException::class)
  fun popLastString(count: Int, charset: Charset = Charsets.UTF_8): String =
    popLastBytes(count).toString(charset)

  /**
   * Pops (removes and returns) a string, starting from the current index of the [endCursor] from
   * this channel, total of [count] bytes. Returns `null` if the remaining bytes in the front of
   * this channel are less than the specified [count].
   *
   * @param charset the charset to use for decoding string
   *
   * @see peekLastStringOrNull
   * @see dropLast
   */
  fun popLastStringOrNull(count: Int, charset: Charset = Charsets.UTF_8): String? =
    popLastBytesOrNull(count)?.toString(charset)

  /**
   * Pops (removes and returns) a byte array, starting from the current index of the [startCursor]
   * from this channel, total of [count] bytes.
   *
   * @throws ChannelEmptyException the remaining bytes of this channel are less than the [count]
   *
   * @see peekBytes
   * @see drop
   */
  @Throws(ChannelEmptyException::class)
  fun popBytes(count: Int): ByteArray

  /**
   * Pops (removes and returns) a byte array, starting from the current index of the [startCursor]
   * from this channel, total of [count] bytes. Pops `null` if the remaining bytes of this channel
   * are less than the specified [count].
   *
   * @see peekBytesOrNull
   * @see drop
   */
  fun popBytesOrNull(count: Int): ByteArray?

  /**
   * Pops (removes and returns) a byte array, starting from the current index of the [endCursor]
   * from this channel, total of [count] bytes.
   *
   * @throws ChannelEmptyException the remaining bytes in the front of this channel are less than
   * the [count]
   *
   * @see peekLastBytes
   * @see dropLast
   */
  @Throws(ChannelEmptyException::class)
  fun popLastBytes(count: Int): ByteArray

  /**
   * Pops (removes and returns) a byte array, starting from the current index of the [endCursor]
   * from this channel, total of [count] bytes. Pops `null` if the remaining bytes in the front of
   * this channel are less than the specified [count].
   *
   * @see peekLastBytesOrNull
   * @see dropLast
   */
  fun popLastBytesOrNull(count: Int): ByteArray?

  /**
   * Pops (removes and returns) a line of string at the current index of the [startCursor] from this
   * channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLine
   * @see dropLine
   */
  @Throws(ChannelEmptyException::class)
  fun popLine(charset: Charset = Charsets.UTF_8): String = popLineBytes().toString(charset)

  /**
   * Pops (removes and returns) a line of string at the current index of the [startCursor] from this
   * channel, or `null` if there are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @see peekLineOrNull
   * @see dropLine
   */
  fun popLineOrNull(charset: Charset = Charsets.UTF_8): String? =
    popLineBytesOrNull()?.toString(charset)

  /**
   * Pops (removes and returns) a line of string at the current index of the [endCursor]
   * from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastLine
   * @see dropLastLine
   */
  @Throws(ChannelEmptyException::class)
  fun popLastLine(charset: Charset = Charsets.UTF_8): String = popLastLineBytes().toString(charset)

  /**
   * Pops (removes and returns) a line of string at the current index of the [endCursor]
   * from this channel, or `null` if there are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @see peekLastLineOrNull
   * @see dropLastLine
   */
  fun popLastLineOrNull(charset: Charset = Charsets.UTF_8): String? =
    popLastLineBytesOrNull()?.toString(charset)

  /**
   * Pops (removes and returns) a line of byte array at the current index of the [startCursor] from this
   * channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLineBytes
   * @see dropLine
   */
  @Throws(ChannelEmptyException::class)
  fun popLineBytes(): ByteArray

  /**
   * Pops (removes and returns) a line of byte array at the current index of the [endCursor]
   * from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekLastLineBytes
   * @see dropLastLine
   */
  @Throws(ChannelEmptyException::class)
  fun popLastLineBytes(): ByteArray

  /**
   * Pops (removes and returns) a line of byte array at the current index of the [startCursor] from
   * this channel, or `null` if there are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see peekLineBytesOrNull
   * @see dropLine
   */
  fun popLineBytesOrNull(): ByteArray?

  /**
   * Pops (removes and returns) a line of byte array at the current index of the [endCursor] from
   * this channel, or `null` if there are no more bytes from this channel.
   *
   * Note that the result returned not include the terminator.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see peekLastLineBytesOrNull
   * @see dropLastLine
   */
  fun popLastLineBytesOrNull(): ByteArray?

  /**
   * Pops (removes and returns) all bytes from this channel as string, and reset index of the
   * [startCursor] and the [endCursor] to initial.
   *
   * @param charset the charset to use for decoding string
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekAll
   * @see clear
   */
  @Throws(ChannelEmptyException::class)
  fun popAll(charset: Charset = Charsets.UTF_8): String = popAllBytes().toString(charset)

  /**
   * Pops (removes and returns) all bytes from this channel as string, or `null` if there are no
   * more bytes from this channel.
   *
   * @param charset the charset to use for decoding string
   *
   * @see peekAllOrNull
   * @see clear
   */
  fun popAllOrNull(charset: Charset = Charsets.UTF_8): String? =
    popAllBytesOrNull()?.toString(charset)

  /**
   * Pops (removes and returns) all bytes from this channel as byte array.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   *
   * @see peekAllBytes
   * @see clear
   */
  @Throws(ChannelEmptyException::class)
  fun popAllBytes(): ByteArray

  /**
   * Pops (removes and returns) all bytes from this channel as byte array, or `null` if there are
   * no more bytes from this channel.
   *
   * @see peekAllBytesOrNull
   * @see clear
   */
  fun popAllBytesOrNull(): ByteArray?

  /**
   * Pops (removes and returns) all utf-8 lines bytes from this channel as list of string, or empty
   * if there are no more bytes in this channel.
   *
   * @param reversed whether returns the reversed list of lines (from last line to first line).
   * @see peekAllLines
   */
  fun popAllLines(
    reversed: Boolean = false,
  ): List<String> = arrayListOf<String>().apply {
    forEachLine(reversed, drop = true, action = ::add)
  }

  /**
   * Pops (removes and returns) all lines bytes from this channel as list of string, or empty if
   * there are no more bytes in this channel.
   *
   * @param reversed whether returns the reversed list of lines (from last line to first line).
   * @param charset character set to use for reading line.
   * @see peekAllLines
   */
  fun popAllLines(
    charset: Charset,
    reversed: Boolean = false,
  ): List<String> = arrayListOf<String>().apply {
    forEachLine(charset, reversed, drop = true, action = ::add)
  }

  /**
   * Pops (removes and returns) all lines bytes from this channel as list of byte array, or empty
   * if there are no more bytes in this channel.
   *
   * @param reversed whether returns the reversed list of lines (from last line to first line).
   * @see peekAllLinesBytes
   */
  fun popAllLinesBytes(reversed: Boolean = false): List<ByteArray> =
    arrayListOf<ByteArray>().apply { forEachByteLine(reversed, drop = true, action = ::add) }

  /**
   * Pops (removes and returns) a string from a range of this channel starting at the [startIndex]
   * and ending right before the [endIndex].
   *
   * By default, pops current index of the [startCursor] and all the remaining bytes afterwards.
   *
   * @param startIndex the start index (inclusive).
   * @param endIndex the end index (exclusive).
   * @param charset the charset to use for decoding string
   *
   * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
   *
   * @see peekRange
   * @see dropRange
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun popRange(
    startIndex: Long = startCursor.index,
    endIndex: Long = endCursor.index + 1,
    charset: Charset = Charsets.UTF_8,
  ): String = popRangeBytes(startIndex, endIndex).toString(charset)

  /**
   * Pops (removes and returns) a byte array from a range of this channel starting at the
   * [startIndex] and ending right before the [endIndex].
   *
   * @param startIndex the start index (inclusive).
   * @param endIndex the end index (exclusive).
   *
   * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
   *
   * @see peekRangeBytes
   * @see dropRange
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun popRangeBytes(
    startIndex: Long = startCursor.index,
    endIndex: Long = endCursor.index + 1,
  ): ByteArray


  ////////////////////////////////////////////////////////////////////////////
  ////            Remove (only delete without returning) Apis             ////
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Drops a byte at the specified [index] from this channel.
   *
   * @throws ChannelEmptyException this channel has no more bytes
   * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the [size]
   * of this channel.
   *
   * @see popAt
   * @see popOrNullAt
   */
  @Throws(ChannelEmptyException::class, IndexOutOfBoundsException::class)
  fun dropAt(index: Long)

  /**
   * Drops a byte at the current index of the [startCursor] from this channel. If there are no bytes
   * from this channel, nothing happens.
   *
   * @see pop
   * @see popOrNull
   */
  fun drop() = dropAt(startCursor.index)

  /**
   * Drops [count] bytes at the current index of the [startCursor] from this channel. If there are
   * no bytes from this channel, nothing happens.
   *
   * @see popBytes
   * @see popBytesOrNull
   * @see popString
   * @see popStringOrNull
   */
  fun drop(count: Int) = dropRange(endIndex = startCursor.index + count)

  /**
   * Drops a byte at the current index of the [endCursor] from this channel. If there are no bytes
   * from this channel, nothing happens.
   *
   * @see popLast
   * @see popLastOrNull
   */
  fun dropLast() = dropAt(endCursor.index)

  /**
   * Drops [count] bytes at the current index of the [endCursor] from this channel. If there are no
   * bytes from this channel, nothing happens.
   *
   * @see popLastBytes
   * @see popLastBytesOrNull
   * @see popLastString
   * @see popLastStringOrNull
   */
  fun dropLast(count: Int) = dropRange(startIndex = endCursor.index - count)

  /**
   * Drops a short (two bytes) at the current index of the [startCursor] from this channel. If
   * there are no bytes from this channel, nothing happens.
   *
   * @see popShort
   * @see popShortOrNull
   */
  fun dropShort()

  /**
   * Drops a short (two bytes) at the current index of the [endCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popLastShort
   * @see popLastShortOrNull
   */
  fun dropLastShort()

  /**
   * Drops an int (four bytes) at the current index of the [startCursor] from this channel. If
   * there are no bytes from this channel, nothing happens.
   *
   * @see popInt
   * @see popIntOrNull
   */
  fun dropInt()

  /**
   * Drops an int (four bytes) at the current index of the [endCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popLastInt
   * @see popLastIntOrNull
   */
  fun dropLastInt()

  /**
   * Drops a long (eight bytes) at the current index of the [startCursor] from this channel. If
   * there are no bytes from this channel, nothing happens.
   *
   * @see popLong
   * @see popLongOrNull
   */
  fun dropLong()

  /**
   * Drops a long (eight bytes) at the current index of the [endCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popLastLong
   * @see popLastLongOrNull
   */
  fun dropLastLong()

  /**
   * Drops a float (four bytes) at the current index of the [startCursor] from this channel. If
   * there are no bytes from this channel, nothing happens.
   *
   * @see popFloat
   * @see popFloatOrNull
   */
  fun dropFloat()

  /**
   * Drops a float (four bytes) at the current index of the [endCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popLastFloat
   * @see popLastFloatOrNull
   */
  fun dropLastFloat()

  /**
   * Drops a double (eight bytes) at the current index of the [startCursor] from this channel. If
   * there are no bytes from this channel, nothing happens.
   *
   * @see popDouble
   * @see popDoubleOrNull
   */
  fun dropDouble()

  /**
   * Drops a double (eight bytes) at the current index of the [endCursor] from this channel. If
   * there are no bytes from this channel, nothing happens.
   *
   * @see popLastDouble
   * @see popLastDoubleOrNull
   */
  fun dropLastDouble()

  /**
   * Drops a char (two bytes) at the current index of the [startCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popChar
   * @see popCharOrNull
   */
  fun dropChar()

  /**
   * Drops a char (two bytes) at the current index of the [endCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popLastChar
   * @see popLastCharOrNull
   */
  fun dropLastChar()

  /**
   * Drops a boolean (one byte) at the current index of the [startCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popBoolean
   * @see popBooleanOrNull
   */
  fun dropBoolean()

  /**
   * Drops a boolean (one byte) at the current index of the [endCursor] from this channel. If there
   * are no bytes from this channel, nothing happens.
   *
   * @see popLastBoolean
   * @see popLastBooleanOrNull
   */
  fun dropLastBoolean()

  /**
   * Drops a line of string at the current index of the [startCursor] from this
   * channel (include the terminator). If there are no bytes from this channel, nothing happens.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see popLine
   * @see popLineOrNull
   */
  fun dropLine()

  /**
   * Drops a line of string at the current index of the [endCursor] from this
   * channel (include the terminator). If there are no bytes from this channel, nothing happens.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * @see popLastLine
   * @see popLastLineOrNull
   */
  fun dropLastLine()

  /**
   * Drops some bytes from a range of this channel starting at the [startIndex] and ending right
   * before the [endIndex].
   *
   * By default, drops current index of the [startCursor] and all the remaining bytes afterwards.
   *
   * @param startIndex the start index (inclusive).
   * @param endIndex the end index (exclusive).
   *
   * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
   *
   * @see popRange
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun dropRange(
    startIndex: Long = startCursor.index,
    endIndex: Long = endCursor.index + 1,
  )

  /**
   * Clear all bytes from this channel and reset index of the [startCursor] and the [endCursor]
   * to initial.
   *
   * @see popAll
   */
  fun clear()


  ////////////////////////////////////////////////////////////////////////////
  ////                    Push (append or insert) Apis                    ////
  ////////////////////////////////////////////////////////////////////////////

  //// Push byte (one byte)

  /**
   * Pushes a given [byte] to the beginning of this channel, and then moves the cursor to after
   * the byte being added: `startCursor.moveRight()`.
   */
  fun push(byte: Byte)

  /**
   * Pushes a given [byte] to the ending of this channel, and then moves the cursor to before the
   * byte being added: `endCursor.moveLeft()`.
   */
  fun pushLast(byte: Byte)

  /**
   * Pushes a given [byte] to the specified [index] of this channel.
   *
   * @see replace
   */
  fun pushTo(index: Long, byte: Byte)

  //// Push bytes

  /**
   * Pushes a given [src] to the beginning of this channel, and then moves the cursor to after the
   * byte array being added: `startCursor.moveRight(cutEndIndex - cutStartIndex)`.
   *
   * @param src the byte array to be pushed
   * @param cutStartIndex the start index of [src] to be pushed (inclusive).
   * @param cutEndIndex The end index of [src] to be pushed (exclusive).
   *
   * @throws IndexOutOfBoundsException if [cutStartIndex] is less than zero or [cutEndIndex] is
   *   greater than the size of the [src].
   * @throws IllegalArgumentException if [cutStartIndex] is greater than [cutEndIndex].
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun push(src: ByteArray, cutStartIndex: Int = 0, cutEndIndex: Int = src.size) {
    for (i in cutStartIndex until cutEndIndex) push(byte = src[i])
  }

  /**
   * Pushes a given [src] to the ending of this channel, and then moves the cursor to before the
   * byte array being added: `endCursor.moveLeft(cutEndIndex - cutStartIndex)`.
   *
   * @param src the byte array to be pushed
   * @param cutStartIndex the start index of [src] to be pushed (inclusive).
   * @param cutEndIndex The end index of [src] to be pushed (exclusive).
   *
   * @throws IndexOutOfBoundsException if [cutStartIndex] is less than zero or [cutEndIndex] is
   *   greater than the size of the [src].
   * @throws IllegalArgumentException if [cutStartIndex] is greater than [cutEndIndex].
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun pushLast(src: ByteArray, cutStartIndex: Int = 0, cutEndIndex: Int = src.size) {
    for (i in cutEndIndex - 1 downTo cutStartIndex) pushLast(byte = src[i])
  }

  /**
   * Pushes a given bytes to the specified [index] of this channel.
   *
   * In fact, the default implementation of this function is equivalent to the effect of the
   * following expression:
   * ```
   * startCursor.moveTemporarily {
   *   moveTo(index)
   *   push(src, cutStartIndex, cutEndIndex)
   * }
   * ```
   *
   * @param src the byte array to be pushed
   * @param cutStartIndex the start index of [src] to be pushed (inclusive).
   * @param cutEndIndex The end index of [src] to be pushed (exclusive).
   *
   * @throws IndexOutOfBoundsException if [cutStartIndex] is less than zero or [cutEndIndex] is
   *   greater than the size of the [src].
   * @throws IllegalArgumentException if [cutStartIndex] is greater than [cutEndIndex].
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun pushTo(index: Long, src: ByteArray, cutStartIndex: Int = 0, cutEndIndex: Int = src.size)

  //// Push string

  /**
   * Pushes a byte array that encodes the given [src] through the specified [charset] to the
   * beginning of this channel, and then moves the cursor to after the string being added:
   * `startCursor.moveRight(cutEndIndex - cutStartIndex)`.
   *
   * @param src the string to be pushed
   * @param cutStartIndex the start index of [src] to be pushed (inclusive).
   * @param cutEndIndex The end index of [src] to be pushed (exclusive).
   *
   * @throws IndexOutOfBoundsException if [cutStartIndex] is less than zero or [cutEndIndex] is
   *   greater than the size of the [src].
   * @throws IllegalArgumentException if [cutStartIndex] is greater than [cutEndIndex].
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun push(
    src: String,
    cutStartIndex: Int = 0,
    cutEndIndex: Int = src.length,
    charset: Charset = Charsets.UTF_8,
  ) = push(src.toByteArray(charset), cutStartIndex, cutEndIndex)

  /**
   * Pushes a byte array that encodes the given [src] through the specified [charset] to the ending
   * of this channel, and then moves the cursor to before the string being added:
   * `endCursor.moveLeft(cutEndIndex - cutStartIndex)`.
   *
   * @param src the string to be pushed
   * @param cutStartIndex the start index of [src] to be pushed (inclusive).
   * @param cutEndIndex The end index of [src] to be pushed (exclusive).
   *
   * @throws IndexOutOfBoundsException if [cutStartIndex] is less than zero or [cutEndIndex] is
   *   greater than the size of the [src].
   * @throws IllegalArgumentException if [cutStartIndex] is greater than [cutEndIndex].
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun pushLast(
    src: String,
    cutStartIndex: Int = 0,
    cutEndIndex: Int = src.length,
    charset: Charset = Charsets.UTF_8,
  ) = pushLast(src.toByteArray(charset), cutStartIndex, cutEndIndex)

  /**
   * Pushes a byte array that encodes the given [src] through the specified [charset] to the
   * specified [index] of this channel.
   *
   * @param cutStartIndex the start index of [src] to be pushed (inclusive).
   * @param cutEndIndex The end index of [src] to be pushed (exclusive).
   *
   * @throws IndexOutOfBoundsException if [cutStartIndex] is less than zero or [cutEndIndex] is
   *   greater than the size of the [src].
   * @throws IllegalArgumentException if [cutStartIndex] is greater than [cutEndIndex].
   */
  @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
  fun pushTo(
    index: Long,
    src: String,
    cutStartIndex: Int = 0,
    cutEndIndex: Int = src.length,
    charset: Charset = Charsets.UTF_8,
  ) = pushTo(index, src.toByteArray(charset), cutStartIndex, cutEndIndex)

  //// Push short (two bytes)

  /**
   * Pushes a given [short] to the beginning of this channel using two bytes, and then moves the
   * cursor to after the short being added: `startCursor.moveRight(repeat = 2)`.
   */
  fun push(short: Short)

  /**
   * Pushes a given [short] to the ending of this channel using two bytes, and then moves the
   * cursor to before the short being added: `endCursor.moveLeft(repeat = 2)`.
   */
  fun pushLast(short: Short)

  /**
   * Pushes a given [short] to the specified [index] of this channel using two bytes.
   */
  fun pushTo(index: Long, short: Short)

  //// Push int (four bytes)

  /**
   * Pushes a given [int] to the beginning of this channel using four bytes, and then moves the
   * cursor to after the int being added: `startCursor.moveRight(repeat = 4)`.
   */
  fun push(int: Int)

  /**
   * Pushes a given [int] to the ending of this channel using four bytes, and then moves the
   * cursor to before the int being added: `endCursor.moveLeft(repeat = 4)`.
   */
  fun pushLast(int: Int)

  /**
   * Pushes a given [int] to the specified [index] of this channel using four bytes.
   */
  fun pushTo(index: Long, int: Int)

  //// Push long (eight bytes)

  /**
   * Pushes a given [long] to the beginning of this channel using eight bytes, and then moves the
   * cursor to after the long being added: `startCursor.moveRight(repeat = 8)`.
   */
  fun push(long: Long)

  /**
   * Pushes a given [long] to the ending of this channel using eight bytes, and then moves the
   * cursor to before the long being added: `endCursor.moveLeft(repeat = 8)`.
   */
  fun pushLast(long: Long)

  /**
   * Pushes a given [long] to the specified [index] of this channel using eight bytes.
   */
  fun pushTo(index: Long, long: Long)

  //// Push float (four bytes)

  /**
   * Pushes a given [float] to the beginning of this channel using four bytes, and then moves the
   * cursor to after the float being added: `startCursor.moveRight(repeat = 4)`.
   */
  fun push(float: Float)

  /**
   * Pushes a given [float] to the ending of this channel using four bytes, and then moves the
   * cursor to before the float being added: `endCursor.moveLeft(repeat = 4)`.
   */
  fun pushLast(float: Float)

  /**
   * Pushes a given [float] to the specified [index] of this channel using four bytes.
   */
  fun pushTo(index: Long, float: Float)

  //// Push double (eight bytes)

  /**
   * Pushes a given [double] to the beginning of this channel using eight bytes, and then moves the
   * cursor to after the double being added: `startCursor.moveRight(repeat = 8)`.
   */
  fun push(double: Double)

  /**
   * Pushes a given [double] to the ending of this channel using eight bytes, and then moves the
   * cursor to before the double being added: `endCursor.moveLeft(repeat = 8)`.
   */
  fun pushLast(double: Double)

  /**
   * Pushes a given [double] to the specified [index] of this channel using eight bytes.
   */
  fun pushTo(index: Long, double: Double)

  //// Push char (two bytes)

  /**
   * Pushes a given [char] to the beginning of this channel using two bytes, and then moves the
   * cursor to after the char being added: `startCursor.moveRight(repeat = 2)`.
   */
  fun push(char: Char)

  /**
   * Pushes a given [char] to the ending of this channel using two bytes, and then moves the
   * cursor to before the char being added: `endCursor.moveLeft(repeat = 2)`.
   */
  fun pushLast(char: Char)

  /**
   * Pushes a given [char] to the specified [index] of this channel using two bytes.
   */
  fun pushTo(index: Long, char: Char)

  //// Push boolean (one byte)

  /**
   * Pushes a given [boolean] to the beginning of this channel using one byte, and then moves the
   * cursor to after the boolean being added: `startCursor.moveRight()`.
   */
  fun push(boolean: Boolean) = push(byte = if (boolean) 1 else 0)

  /**
   * Pushes a given [boolean] to the ending of this channel using one byte, and then moves the
   * cursor to before the boolean being added: `endCursor.moveLeft()`.
   */
  fun pushLast(boolean: Boolean) = pushLast(byte = if (boolean) 1 else 0)

  /**
   * Pushes a given [boolean] to the specified [index] of this channel using one byte.
   */
  fun pushTo(index: Long, boolean: Boolean) = pushTo(index, byte = if (boolean) 1 else 0)


  ////////////////////////////////////////////////////////////////////////////
  ////                         Replace (Set) Apis                         ////
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Replaces the byte at the specified [index] in this channel with the given new [byte].
   *
   * @see set
   */
  fun replace(index: Long, byte: Byte)

  /** For more details: [set] */
  operator fun set(index: Long, byte: Byte) = replace(index, byte)

  ////////////////////////////////////////////////////////////////////////////
  ////                             State Apis                             ////
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Returns `true` if this channel has no data.
   */
  fun isEmpty(): Boolean = size == 0L

  /**
   * Returns `true` if this channel has data.
   */
  fun isNotEmpty(): Boolean = size > 0L

  /**
   * Returns `true` if this channel is open.
   */
  fun isOpen(): Boolean


  /**
   * The cursor of this channel, which is the [index] of the data being accessed.
   *
   * This is best explained by analogy. Imagine you're in a terminal, the highlighted cursor is the
   * data position of the channel you are currently accessing.
   */
  interface Cursor {

    /**
     * The index of the cursor, which is the position in the accessed channel.
     *
     * Changing this value means moving the cursor to access data in the channel. Note that this
     * value cannot be a negative number.
     *
     * For example, the [index] is `2`:
     * ```
     * a  b  c  d  e  f  g
     *       ^
     * ```
     */
    var index: Long

    /**
     * Returns `true` if this cursor reaches the start of the data channel.
     */
    val isReachStart: Boolean

    /**
     * Returns `true` if this cursor reaches the end of the data channel.
     */
    val isReachEnd: Boolean

    /**
     * Moves the cursor to the specified absolute [index] in the data channel.
     *
     * @return this cursor
     */
    fun moveTo(index: Long): Cursor

    /**
     * Moves the cursor to the first index in the data channel.
     *
     * @return this cursor
     * @see DataChannel.firstIndex
     */
    fun moveToFirst(): Cursor

    /**
     * Moves the cursor to the last index in the data channel.
     *
     * @return this cursor
     * @see DataChannel.lastIndex
     */
    fun moveToLast(): Cursor

    /**
     * Move the cursor to the next byte in the data channel.
     *
     * For example, current [index] is `1`, the [repeat] is `2`:
     * ```
     * a  b  c  d  e  f  g
     *          ^
     * ```
     *
     * @param repeat the value of repeated moves can be used to control how many times to move right
     * @return this cursor
     */
    fun moveRight(repeat: Int = 1): Cursor

    /**
     * Move the cursor to the previous byte in the data channel.
     *
     * For example, current [index] is `4`, the [repeat] is `2`:
     * ```
     * a  b  c  d  e  f  g
     *       ^
     * ```
     *
     * @param repeat the value of repeated moves can be used to control how many times to move left
     * @return this cursor
     */
    fun moveLeft(repeat: Int = 1): Cursor

    /**
     * Moves the cursor to the beginning of the previous line in the data channel.
     *
     * For example:
     * ```
     * Initialize:
     * a  b  c  d  e  f  g
     * h  i  j  k  l  m  n
     *             ^
     * Result:
     * a  b  c  d  e  f  g
     * ^
     * h  i  j  k  l  m  n
     * ```
     *
     * @param repeat the value of repeated moves can be used to control how many times to move up
     *
     * @return this cursor
     *
     * @throws ChannelEmptyException this channel has no more bytes
     */
    @Throws(ChannelEmptyException::class)
    fun moveUp(repeat: Int = 1): Cursor

    /**
     * Moves the cursor to the beginning of the next line from the data channel.
     *
     * For example:
     * ```
     * Initialize:
     * a  b  c  d  e  f  g
     *       ^
     * h  i  j  k  l  m  n
     *
     * Result:
     * a  b  c  d  e  f  g
     * h  i  j  k  l  m  n
     * ^
     * ```
     *
     * @param repeat the value of repeated moves can be used to control how many times to move down
     *
     * @return this cursor
     *
     * @throws ChannelEmptyException this channel has no more bytes
     */
    @Throws(ChannelEmptyException::class)
    fun moveDown(repeat: Int = 1): Cursor

    /**
     * Remembers the [index] of this cursor.
     *
     * @return this cursor
     */
    fun remember(): Cursor

    /**
     * Remembers the [index] of this cursor.
     *
     * @return this cursor
     */
    fun restore(): Cursor

    /**
     * Moves the cursor back to the position specified at to initialize of the data channel.
     *
     * @return this cursor
     *
     * @see DataChannel.startCursor
     * @see DataChannel.endCursor
     */
    fun reset(): Cursor

    /**
     * Moves this cursor temporarily, any operations in the [block] will be forgotten after
     * execution (reset cursor).
     *
     * @return this cursor
     */
    fun moveTemporarily(block: Cursor.() -> Unit): Cursor = apply {
      remember()
      block()
      restore()
    }
  }
}

/**
 * Performs the given [action] on each line of this channel.
 *
 * @param reversed if the value is `true`, read reversed from the last line to the first line.
 * @param drop if the value is `true`, drop it after line is read.
 */
inline fun DataChannel.forEachByteLine(
  reversed: Boolean = false,
  drop: Boolean = false,
  action: (ByteArray) -> Unit,
) {
  while (true) action(
    when {
      reversed -> drop.ifTrue { popLastLineBytesOrNull() }
        ?: peekLastLineBytesOrNull()
      else -> drop.ifTrue { popLineBytesOrNull() }
        ?: peekLineBytesOrNull()
    } ?: break
  )
}

/**
 * Performs the given [action] on each line of this channel.
 *
 * @param reversed if the value is `true`, read reversed from the last line to the first line.
 * @param drop if the value is `true`, drop it after line is read.
 * @param charset character set to use for reading line of source.
 */
inline fun DataChannel.forEachLine(
  charset: Charset,
  reversed: Boolean = false,
  drop: Boolean = false,
  action: (String) -> Unit,
) {
  while (true) action(
    when {
      reversed -> drop.ifTrue { popLastLineOrNull(charset) }
        ?: peekLastLineOrNull(charset)
      else -> drop.ifTrue { popLineOrNull(charset) }
        ?: peekLineOrNull(charset)
    } ?: break
  )
}

/**
 * Performs the given [action] on each utf-8 line of this channel.
 *
 * @param reversed if the value is `true`, read reversed from the last line to the first line.
 * @param drop if the value is `true`, drop it after line is read.
 */
inline fun DataChannel.forEachLine(
  reversed: Boolean = false,
  drop: Boolean = false,
  action: (String) -> Unit,
) {
  while (true) action(
    when {
      reversed -> drop.ifTrue { popLastLineOrNull() }
        ?: peekLastLineOrNull()
      else -> drop.ifTrue { popLineOrNull() }
        ?: peekLineOrNull()
    } ?: break
  )
}
