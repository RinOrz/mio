package com.meowool.mio.channel

import com.meowool.mio.ChannelEmptyException
import com.meowool.mio.ChannelUnderflowException
import com.meowool.mio.internal.checkIndices
import com.meowool.mio.internal.toLegalInt
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.charset.Charset

/**
 * Abstract the interfaces for reading data in the data channel.
 *
 * This interface provides the ability to access data randomly, through the [cursor] can directly
 * access the data at the beginning, at the middle, and even at the end of the channel.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface ReadableDataChannel : DataChannelInfo {

  /**
   * Gets a byte at the specified [index] of this channel, and then moves the cursor to the right of
   * the byte being returned, that is: `cursor.moveRight()`.
   *
   * Note that the behavior of this function is almost the same as [peek], the only difference is
   * that this function overloaded Kotlin's operator, for example:
   * ```
   * A B C D E
   * -----------------------
   * channel.get(2)  ->  C
   * channel[2]      ->  C
   * ```
   *
   * @param index The index of the data to be returned.
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @see Byte.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  operator fun get(index: Long): Byte = peek(index)

  /**
   * Peeks a byte at the specified [index] of this channel, and then moves the cursor to the right
   * of the byte being peeked, that is: `cursor.moveTo(index + 1)`.
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Byte.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peek(index: Long = cursor.index): Byte

  /**
   * Peeks a byte at the specified [index] of this channel, and then moves the cursor to the right
   * of the byte being peeked, that is: `cursor.moveTo(index + 1)`.
   *
   * Note that it returns a boxed byte object, if there is no data at the specified [index] of this
   * channel, it returns `null`.
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Byte.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekOrNull(index: Long = cursor.index): Byte?

  /**
   * Peeks a byte from the left side of the specified [index] of this channel. In other words, first
   * moves the cursor to the left by one byte, that is: `cursor.moveTo(index - 1)`, and then peek
   * the byte on the right (not include the byte where the current cursor).
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Byte.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeft(index: Long = cursor.index): Byte =
    cursor.moveTo(index - 1).runTemporarily { peek(index) }

  /**
   * Peeks a byte from the left side of the specified [index] of this channel. In other words, first
   * moves the cursor to the left by one byte, that is: `cursor.moveTo(index - 1)`, and then peek
   * the byte on the right (not include the byte where the current cursor).
   *
   * Note that it returns a boxed byte object, if there is no data at the specified [index] of this
   * channel, it returns `null`.
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Byte.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftOrNull(index: Long = cursor.index): Byte? =
    cursor.moveTo(index - 1).runTemporarily { peekOrNull(index) }

  /**
   * Peeks a boolean at the specified [index] of this channel, and then moves the cursor to the
   * right of the boolean being peeked, that is: `cursor.moveTo(index + 1)`.
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekBoolean(index: Long = cursor.index): Boolean = peek() == 1.toByte()

  /**
   * Peeks a boolean at the specified [index] of this channel, and then moves the cursor to the
   * right of the boolean being peeked, that is: `cursor.moveTo(index + 1)`.
   *
   * Note that if there is no data at the specified [index] of this channel, it returns `null`.
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekBooleanOrNull(index: Long = cursor.index): Boolean? = peekOrNull()?.equals(1.toByte())

  /**
   * Peeks a boolean from the left side of the specified [index] of this channel. In other words,
   * first moves the cursor to the left by one byte, that is: `cursor.moveTo(index - 1)`, and then
   * peek the boolean on the right (not include the byte where the current cursor).
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftBoolean(index: Long = cursor.index): Boolean = peekLeft() == 1.toByte()

  /**
   * Peeks a boolean from the left side of the specified [index] of this channel. In other words,
   * first moves the cursor to the left by one byte, that is: `cursor.moveTo(index - 1)`, and then
   * peek the boolean on the right (not include the byte where the current cursor).
   *
   * Note that if there is no data at the specified [index] of this channel, it returns `null`.
   *
   * @param index The index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftBooleanOrNull(index: Long = cursor.index): Boolean? =
    peekLeftOrNull()?.equals(1.toByte())

  /**
   * Peeks a short value (two bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the short value (two bytes) being peeked, that is:
   * `cursor.moveTo(index + 2)`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than two bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Short.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekShort(index: Long = cursor.index): Short

  /**
   * Peeks a short value (two bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the short value (two bytes) being peeked, that is:
   * `cursor.moveTo(index + 2)`.
   *
   * Note that it returns a boxed short value (two bytes) object, if the right side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Short.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekShortOrNull(index: Long = cursor.index): Short?

  /**
   * Peeks a short value (two bytes) from the left side of the specified [index] of this channel. In
   * other words, first moves the cursor to the left by two bytes, that is:
   * `cursor.moveTo(index - 2)`, and then peek the short value (two bytes) on the right (not include
   * the byte where the current cursor).
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than two bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Short.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftShort(index: Long = cursor.index): Short =
    cursor.moveTo(index - 2).runTemporarily { peekShort(index - 1) }

  /**
   * Peeks a short value (two bytes) from the left side of the specified [index] of this channel. In
   * other words, first moves the cursor to the left by two bytes, that is:
   * `cursor.moveTo(index - 2)`, and then peek the short value (two bytes) on the right (not include
   * the byte where the current cursor).
   *
   * Note that it returns a boxed short value (two bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Short.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftShortOrNull(index: Long = cursor.index): Short? =
    cursor.moveTo(index - 2).runTemporarily { peekShortOrNull(index - 1) }

  /**
   * Peeks a char value (two bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the char value (two bytes) being peeked, that is:
   * `cursor.moveTo(index + 2)`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than two bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Char.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekChar(index: Long = cursor.index): Char

  /**
   * Peeks a char value (two bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the char value (two bytes) being peeked, that is:
   * `cursor.moveTo(index + 2)`.
   *
   * Note that it returns a boxed char value (two bytes) object, if the right side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Char.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekCharOrNull(index: Long = cursor.index): Char?

  /**
   * Peeks a char value (two bytes) from the left side of the specified [index] of this channel. In
   * other words, first moves the cursor to the left by two bytes, that is:
   * `cursor.moveTo(index - 2)`, and then peek the char value (two bytes) on the right (not include
   * the byte where the current cursor).
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than two bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Char.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftChar(index: Long = cursor.index): Char =
    cursor.moveTo(index - 2).runTemporarily { peekChar(index - 1) }

  /**
   * Peeks a char value (two bytes) from the left side of the specified [index] of this channel. In
   * other words, first moves the cursor to the left by two bytes, that is:
   * `cursor.moveTo(index - 2)`, and then peek the char value (two bytes) on the right (not include
   * the byte where the current cursor).
   *
   * Note that it returns a boxed char value (two bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Char.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftCharOrNull(index: Long = cursor.index): Char? =
    cursor.moveTo(index - 2).runTemporarily { peekCharOrNull(index - 1) }

  /**
   * Peeks an int value (four bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the int value (four bytes) being peeked, that is:
   * `cursor.moveTo(index + 4)`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than four bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Int.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekInt(index: Long = cursor.index): Int

  /**
   * Peeks an int value (four bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the int value (four bytes) being peeked, that is:
   * `cursor.moveTo(index + 4)`.
   *
   * Note that it returns a boxed int value (four bytes) object, if the right side of the specified
   * [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Int.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekIntOrNull(index: Long = cursor.index): Int?

  /**
   * Peeks an int value (four bytes) from the left side of the specified [index] of this channel. In
   * other words, first moves the cursor to the left by four bytes, that is:
   * `cursor.moveTo(index - 4)`, and then peek the int value (four bytes) on the right (not include
   * the byte where the current cursor).
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than four bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Int.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftInt(index: Long = cursor.index): Int =
    cursor.moveTo(index - 4).runTemporarily { peekInt(index - 3) }

  /**
   * Peeks an int value (four bytes) from the left side of the specified [index] of this channel. In
   * other words, first moves the cursor to the left by four bytes, that is:
   * `cursor.moveTo(index - 4)`, and then peek the int value (four bytes) on the right (not include
   * the byte where the current cursor).
   *
   * Note that it returns a boxed int value (four bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Int.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftIntOrNull(index: Long = cursor.index): Int? =
    cursor.moveTo(index - 4).runTemporarily { peekIntOrNull(index - 3) }

  /**
   * Peeks a long value (eight bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the long value (eight bytes) being peeked, that is:
   * `cursor.moveTo(index + 8)`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than eight bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Long.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLong(index: Long = cursor.index): Long

  /**
   * Peeks a long value (eight bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the long value (eight bytes) being peeked, that is:
   * `cursor.moveTo(index + 8)`.
   *
   * Note that it returns a boxed long value (eight bytes) object, if the right side of the
   * specified [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Long.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLongOrNull(index: Long = cursor.index): Long?

  /**
   * Peeks a long value (eight bytes) from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by eight bytes, that is:
   * `cursor.moveTo(index - 8)`, and then peek the long value (eight bytes) on the right (not
   * include the byte where the current cursor).
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than eight bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Long.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftLong(index: Long = cursor.index): Long =
    cursor.moveTo(index - 8).runTemporarily { peekLong(index - 7) }

  /**
   * Peeks a long value (eight bytes) from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by eight bytes, that is:
   * `cursor.moveTo(index - 8)`, and then peek the long value (eight bytes) on the right (not
   * include the byte where the current cursor).
   *
   * Note that it returns a boxed long value (eight bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Long.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftLongOrNull(index: Long = cursor.index): Long? =
    cursor.moveTo(index - 8).runTemporarily { peekLongOrNull(index - 7) }

  /**
   * Peeks a float value (four bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the float value (four bytes) being peeked, that is:
   * `cursor.moveTo(index + 4)`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than four bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Float.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekFloat(index: Long = cursor.index): Float

  /**
   * Peeks a float value (four bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the float value (four bytes) being peeked, that is:
   * `cursor.moveTo(index + 4)`.
   *
   * Note that it returns a boxed float value (four bytes) object, if the right side of the
   * specified [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Float.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekFloatOrNull(index: Long = cursor.index): Float?

  /**
   * Peeks a float value (four bytes) from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by four bytes, that is:
   * `cursor.moveTo(index - 4)`, and then peek the float value (four bytes) on the right (not
   * include the byte where the current cursor).
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than four bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Float.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftFloat(index: Long = cursor.index): Float =
    cursor.moveTo(index - 4).runTemporarily { peekFloat(index - 3) }

  /**
   * Peeks a float value (four bytes) from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by four bytes, that is:
   * `cursor.moveTo(index - 4)`, and then peek the float value (four bytes) on the right (not
   * include the byte where the current cursor).
   *
   * Note that it returns a boxed float value (four bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Float.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftFloatOrNull(index: Long = cursor.index): Float? =
    cursor.moveTo(index - 4).runTemporarily { peekFloatOrNull(index - 3) }

  /**
   * Peeks a double value (eight bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the double value (eight bytes) being peeked, that is:
   * `cursor.moveTo(index + 8)`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than eight bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Double.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekDouble(index: Long = cursor.index): Double

  /**
   * Peeks a double value (eight bytes) at the specified [index] of this channel, and then moves the
   * cursor to the right of the double value (eight bytes) being peeked, that is:
   * `cursor.moveTo(index + 8)`.
   *
   * Note that it returns a boxed double value (eight bytes) object, if the right side of the
   * specified [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Double.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekDoubleOrNull(index: Long = cursor.index): Double?

  /**
   * Peeks a double value (eight bytes) from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by eight bytes, that is:
   * `cursor.moveTo(index - 8)`, and then peek the double value (eight bytes) on the right (not
   * include the byte where the current cursor).
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than eight bytes.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Double.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftDouble(index: Long = cursor.index): Double =
    cursor.moveTo(index - 8).runTemporarily { peekDouble(index - 7) }

  /**
   * Peeks a double value (eight bytes) from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by eight bytes, that is:
   * `cursor.moveTo(index - 8)`, and then peek the double value (eight bytes) on the right (not
   * include the byte where the current cursor).
   *
   * Note that it returns a boxed double value (eight bytes) object, if the left side of the
   * specified [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   *
   * @see Double.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftDoubleOrNull(index: Long = cursor.index): Double? =
    cursor.moveTo(index - 8).runTemporarily { peekDoubleOrNull(index - 7) }

  /**
   * Peeks a byte array starting at the specified [index] (inclusive) and consists of [count] bytes
   * to the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the byteArray.
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekBytes(index: Long, count: Int): ByteArray

  /**
   * Peeks a byte array starting at the specified [index] (inclusive) and consists of [count] bytes
   * to the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the byteArray.
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekBytesOrNull(index: Long, count: Int): ByteArray?

  /**
   * Peeks a byte array starting at the specified [index] (inclusive) and consists of [count] bytes
   * to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the byte array on the right (not include the byte
   * where the current cursor).
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the byteArray.
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftBytes(index: Long, count: Int): ByteArray =
    cursor.moveTo(index - count).runTemporarily { peekBytes(index - count + 1, count) }

  /**
   * Peeks a byte array starting at the specified [index] (inclusive) and consists of [count] bytes
   * to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the byte array on the right (not include the byte
   * where the current cursor).
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the byteArray.
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftBytesOrNull(index: Long, count: Int): ByteArray? =
    cursor.moveTo(index - count).runTemporarily { peekBytesOrNull(index - count + 1, count) }

  /**
   * Peeks a string starting at the specified [index] (inclusive) and consists of [count] bytes to
   * the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekString(
    index: Long,
    count: Int,
    charset: Charset = Charsets.UTF_8
  ): String = peekBytes(index, count).toString(charset)

  /**
   * Peeks a string starting at the specified [index] (inclusive) and consists of [count] bytes to
   * the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekStringOrNull(
    index: Long,
    count: Int,
    charset: Charset = Charsets.UTF_8
  ): String? = peekBytesOrNull(index, count)?.toString(charset)

  /**
   * Peeks a string starting at the specified [index] (inclusive) and consists of [count] bytes to
   * the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the string on the right (not include the byte
   * where the current cursor).
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftString(
    index: Long,
    count: Int,
    charset: Charset = Charsets.UTF_8
  ): String = peekLeftBytes(index, count).toString(charset)

  /**
   * Peeks a string starting at the specified [index] (inclusive) and consists of [count] bytes to
   * the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the string on the right (not include the byte
   * where the current cursor).
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * @param index The starting index of the data to be peeked.
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftStringOrNull(
    index: Long,
    count: Int,
    charset: Charset = Charsets.UTF_8
  ): String? = peekLeftBytesOrNull(index, count)?.toString(charset)

  /**
   * Peeks a byte array starting at the current [cursor] index (inclusive) and consists of [count]
   * bytes to the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * @param count The total byte count of the byteArray.
   *
   * @throws ChannelUnderflowException If the right side of the current [cursor] index (inclusive)
   *   of this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekBytes(count: Int): ByteArray = peekBytes(cursor.index, count)

  /**
   * Peeks a byte array starting at the current [cursor] index (inclusive) and consists of [count]
   * bytes to the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * Note that if the right side of the current [cursor] index (inclusive) of this channel is less
   * than [count], it returns `null`.
   *
   * @param count The total byte count of the byteArray.
   *
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ClosedChannelException::class,
    IOException::class
  )
  fun peekBytesOrNull(count: Int): ByteArray? = peekBytesOrNull(cursor.index, count)

  /**
   * Peeks a byte array starting at the current [cursor] index (inclusive) and consists of [count]
   * bytes to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the byte array on the right (not include the byte
   * where the current cursor).
   *
   * @param count The total byte count of the byteArray.
   *
   * @throws ChannelUnderflowException If the left side of the current [cursor] index (inclusive) of
   *   this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftBytes(count: Int): ByteArray = peekLeftBytes(cursor.index, count)

  /**
   * Peeks a byte array starting at the current [cursor] index (inclusive) and consists of [count]
   * bytes to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the byte array on the right (not include the byte
   * where the current cursor).
   *
   * Note that if the left side of the current [cursor] index (inclusive) of this channel is less
   * than [count], it returns `null`.
   *
   * @param count The total byte count of the byteArray.
   *
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftBytesOrNull(count: Int): ByteArray? = peekLeftBytesOrNull(cursor.index, count)

  /**
   * Peeks a string starting at the current [cursor] index (inclusive) and consists of [count] bytes
   * to the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelUnderflowException If the right side of the current [cursor] index (inclusive)
   *   of this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekString(count: Int, charset: Charset = Charsets.UTF_8): String =
    peekBytes(count).toString(charset)

  /**
   * Peeks a string starting at the current [cursor] index (inclusive) and consists of [count] bytes
   * to the right, and then moves the cursor to the right of bytes being peeked, that is:
   * `cursor.moveTo(index + count)`.
   *
   * Note that if the right side of the current [cursor] index (inclusive) of this channel is less
   * than [count], it returns `null`.
   *
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ClosedChannelException::class,
    IOException::class
  )
  fun peekStringOrNull(count: Int, charset: Charset = Charsets.UTF_8): String? =
    peekBytesOrNull(count)?.toString(charset)

  /**
   * Peeks a string starting at the current [cursor] index (inclusive) and consists of [count] bytes
   * to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the string on the right (not include the byte
   * where the current cursor).
   *
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelUnderflowException If the left side of the current [cursor] index (inclusive) of
   *   this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftString(count: Int, charset: Charset = Charsets.UTF_8): String =
    peekLeftBytes(count).toString(charset)

  /**
   * Peeks a string starting at the current [cursor] index (inclusive) and consists of [count] bytes
   * to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then peek the string on the right (not include the byte
   * where the current cursor).
   *
   * Note that if the left side of the current [cursor] index (inclusive) of this channel is less
   * than [count], it returns `null`.
   *
   * @param count The total byte count of the string.
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftStringOrNull(count: Int, charset: Charset = Charsets.UTF_8): String? =
    peekLeftBytesOrNull(count)?.toString(charset)

  /**
   * Peeks a line of byte array on the right at the specified [index] of this channel, and then
   * moves the cursor to the start of the next line, that is: `moveToNextLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the start of the next line terminator (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peek()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the start of the next line
   *       cursor.moveRight()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.toByteArray()
   * ```
   *
   * For example, the cursor is on the `F`:
   * ```
   * A B C D E F G
   *           ^
   * H I J K L M N
   * ------------------
   * Output: F G
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLineBytes(index: Long = cursor.index): ByteArray

  /**
   * Peeks a line of byte array on the right at the specified [index] of this channel, and then
   * moves the cursor to the start of the next line, that is: `moveToNextLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the start of the next line terminator (exclusive), returns
   * `null` if this channel has no more bytes
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peek()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the start of the next line
   *       cursor.moveRight()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.toByteArray()
   * ```
   *
   * For example, the cursor is on the `F`:
   * ```
   * A B C D E F G
   *           ^
   * H I J K L M N
   * ------------------
   * Output: F G
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLineBytesOrNull(index: Long = cursor.index): ByteArray?

  /**
   * Peeks a line of byte array on the left at the specified [index] of this channel, and then moves
   * the cursor to the end of the previous line, that is: `moveToPreviousLine().moveToEndOfLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the end of the previous line terminator (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peekLeft()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the end of the previous line
   *       cursor.moveLeft()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.reverse().toByteArray()
   * ```
   *
   * For example, the cursor is on the `L`:
   * ```
   * A B C D E F G
   * H I J K L M N
   *         ^
   * ------------------
   * Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftLineBytes(index: Long = cursor.index): ByteArray

  /**
   * Peeks a line of byte array on the left at the specified [index] of this channel, and then moves
   * the cursor to the end of the previous line, that is: `moveToPreviousLine().moveToEndOfLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the end of the previous line terminator (exclusive),
   * returns `null` if this channel has no more bytes
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peekLeft()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the end of the previous line
   *       cursor.moveLeft()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.reverse().toByteArray()
   * ```
   *
   * For example, the cursor is on the `L`:
   * ```
   * A B C D E F G
   * H I J K L M N
   *         ^
   * ------------------
   * Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftLineBytesOrNull(index: Long = cursor.index): ByteArray?

  /**
   * Peeks a line of string on the right at the specified [index] of this channel, and then moves
   * the cursor to the start of the next line, that is: `moveToNextLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the start of the next line terminator (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peek()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the start of the next line
   *       cursor.moveRight()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.toByteArray().toString(charset)
   * ```
   *
   * For example, the cursor is on the `F`:
   * ```
   * A B C D E F G
   *           ^
   * H I J K L M N
   * ------------------
   * Output: F G
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLine(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String =
    peekLineBytes().toString(charset)

  /**
   * Peeks a line of string on the right at the specified [index] of this channel, and then moves
   * the cursor to the start of the next line, that is: `moveToNextLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the start of the next line terminator (exclusive), returns
   * `null` if this channel has no more bytes
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peek()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the start of the next line
   *       cursor.moveRight()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.toByteArray().toString(charset)
   * ```
   *
   * For example, the cursor is on the `F`:
   * ```
   * A B C D E F G
   *           ^
   * H I J K L M N
   * ------------------
   * Output: F G
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLineOrNull(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String? =
    peekLineBytesOrNull()?.toString(charset)

  /**
   * Peeks a line of string on the left at the specified [index] of this channel, and then moves the
   * cursor to the end of the previous line, that is: `moveToPreviousLine().moveToEndOfLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the end of the previous line terminator (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peekLeft()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the end of the previous line
   *       cursor.moveLeft()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.reverse().toByteArray().toString(charset)
   * ```
   *
   * For example, the cursor is on the `L`:
   * ```
   * A B C D E F G
   * H I J K L M N
   *         ^
   * ------------------
   * Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftLine(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String =
    peekLeftLineBytes().toString(charset)

  /**
   * Peeks a line of string on the left at the specified [index] of this channel, and then moves the
   * cursor to the end of the previous line, that is: `moveToPreviousLine().moveToEndOfLine()`.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, peek all bytes from
   * the specified [index] (inclusive) to the end of the previous line terminator (exclusive),
   * returns `null` if this channel has no more bytes
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = peekLeft()) {
   *     // A terminator was encountered, the end of a line
   *     "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
   *       // Move cursor to the end of the previous line
   *       cursor.moveLeft()
   *       break
   *     }
   *     else -> bos.append(byte)
   *   }
   * }
   * bos.reverse().toByteArray().toString(charset)
   * ```
   *
   * For example, the cursor is on the `L`:
   * ```
   * A B C D E F G
   * H I J K L M N
   *         ^
   * ------------------
   * Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be peeked (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekLeftLineOrNull(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String? =
    peekLeftLineBytesOrNull()?.toString(charset)

  /**
   * Peeks a remaining byte array to the right of the current cursor index in the channel and then
   * moves the cursor to the right of bytes being peeked, that is: `cursor.moveTo(size)`.
   *
   * @throws OutOfMemoryError If the required size by the remaining bytes cannot be allocated, for
   *   example the remaining bytes is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekRemainingBytes(): ByteArray =
    peekBytes(remainingSize.toLegalInt("Remaining bytes size too large"))

  /**
   * Peeks a remaining string to the right of the current cursor index in the channel and then moves
   * the cursor to the right of bytes being peeked, that is: `cursor.moveTo(size)`.
   *
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws OutOfMemoryError If the required size by the remaining bytes cannot be allocated, for
   *   example the remaining bytes is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekRemaining(charset: Charset = Charsets.UTF_8): String =
    peekBytes(remainingSize.toLegalInt("Remaining bytes size too large")).toString(charset)

  /**
   * Peeks a byte array from a range of this channel starting at the [startIndex] and ending right
   * before the [endIndex] and then moves the cursor to the [endIndex], that is:
   * `cursor.moveTo(endIndex)`.
   *
   * @param startIndex The start index of range. (inclusive)
   * @param endIndex The end index of range. (exclusive)
   *
   * @throws IndexOutOfBoundsException If [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException If [startIndex] is greater than [endIndex].
   * @throws OutOfMemoryError If the required size by the bytes of the specified range cannot be
   *   allocated, for example the bytes of the specified range is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    IllegalArgumentException::class,
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekRangeBytes(startIndex: Long, endIndex: Long): ByteArray =
    cursor.runTemporarily {
      checkIndices(startIndex, endIndex)
      peekBytes(index = startIndex, count = (endIndex - startIndex).toLegalInt())
    }

  /**
   * Peeks a string from a range of this channel starting at the [startIndex] and ending right
   * before the [endIndex] and then moves the cursor to the [endIndex], that is:
   * `cursor.moveTo(endIndex)`.
   *
   * @param startIndex The start index of range. (inclusive)
   * @param endIndex The end index of range. (exclusive)
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException If [startIndex] is greater than [endIndex].
   * @throws OutOfMemoryError If the required size by the bytes of the specified range cannot be
   *   allocated, for example the bytes of the specified range is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    IllegalArgumentException::class,
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekRange(
    startIndex: Long,
    endIndex: Long,
    charset: Charset = Charsets.UTF_8
  ): String = peekRangeBytes(startIndex, endIndex).toString(charset)

  /**
   * Peeks a byte array of all bytes of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * throwIf(isEmpty()) { ChannelEmptyException() }
   * val bos = ByteArrayBuilder()
   * cursor.moveToFirst()
   * while (true) {
   *   peekLineBytesOrNull()?.also {
   *     bos.append(it)
   *   } ?: break
   * }
   * bos.toByteArray()
   * ```
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws OutOfMemoryError If the required size by the all bytes of this channel cannot be
   *   allocated, for example the channel size is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekAllBytes(): ByteArray

  /**
   * Peeks a byte array of all bytes of this channel.
   *
   * Note that if this channel has no more bytes, the `null` is returned.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * when(isEmpty()) {
   *   true -> null
   *   else -> {
   *     val bos = ByteArrayBuilder()
   *     cursor.moveToFirst()
   *     while (true) {
   *       peekLineBytesOrNull()?.also {
   *         bos.append(it)
   *       } ?: break
   *     }
   *     bos.toByteArray()
   *   }
   * }
   * ```
   *
   * @throws OutOfMemoryError If the required size by the all bytes of this channel cannot be
   *   allocated, for example the channel size is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekAllBytesOrNull(): ByteArray?

  /**
   * Peeks a string decoded from all bytes of this channel.
   *
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws OutOfMemoryError If the required size by the all bytes of this channel cannot be
   *   allocated, for example the channel size is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    ChannelEmptyException::class,
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekAll(charset: Charset = Charsets.UTF_8): String = peekAllBytes().toString(charset)

  /**
   * Peeks a string decoded from all bytes of this channel.
   *
   * Note that if this channel has no more bytes, the `null` is returned.
   *
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws OutOfMemoryError If the required size by the all bytes of this channel cannot be
   *   allocated, for example the channel size is larger that `2 GB`.
   * @throws ClosedChannelException If this channel is closed.
   * @throws IOException If some other I/O error occurs.
   */
  @Throws(
    OutOfMemoryError::class,
    ClosedChannelException::class,
    IOException::class
  )
  fun peekAllOrNull(charset: Charset = Charsets.UTF_8): String? =
    peekAllBytesOrNull()?.toString(charset)

}