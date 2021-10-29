package com.meowool.mio.channel

import com.meowool.mio.ChannelEmptyException
import com.meowool.mio.ChannelUnderflowException
import java.nio.charset.Charset

/**
 * Abstract the interfaces for deleting data in the data channel.
 *
 * This interface provides the ability to delete data randomly, through the [cursor] can directly
 * delete the data at the beginning, at the middle, and even at the end of the channel.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface DeletableDataChannel : ReadableDataChannel {

  /**
   * Pops (returns and deletes) a byte at the specified [index] of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @see Byte.SIZE_BYTES
   * @see peek
   * @see drop
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun pop(index: Long = cursor.index): Byte

  /**
   * Pops (returns and deletes) a byte at the specified [index] of this channel.
   *
   * Note that it returns a boxed byte object, if there is no data at the specified [index] of this
   * channel, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Byte.SIZE_BYTES
   * @see peekOrNull
   * @see drop
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popOrNull(index: Long = cursor.index): Byte?

  /**
   * Pops (returns and deletes) a byte from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by one byte, that is: `cursor.moveLeft()`,
   * and then returns and deletes the byte on the right (not include the byte where the current
   * cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @see Byte.SIZE_BYTES
   * @see peekLeft
   * @see dropLeft
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeft(index: Long = cursor.index): Byte

  /**
   * Pops (returns and deletes) a byte from the left side of the specified [index] of this channel.
   * In other words, first moves the cursor to the left by one byte, that is: `cursor.moveLeft()`,
   * and then returns and deletes the byte on the right (not include the byte where the current
   * cursor).
   *
   * Note that it returns a boxed byte object, if there is no data at the specified [index] of this
   * channel, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Byte.SIZE_BYTES
   * @see peekLeftOrNull
   * @see dropLeft
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftOrNull(index: Long = cursor.index): Byte?

  /**
   * Pops (returns and deletes) a boolean at the specified [index] of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @see peekBoolean
   * @see dropBoolean
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popBoolean(index: Long = cursor.index): Boolean = pop() == 1.toByte()

  /**
   * Pops (returns and deletes) a boolean at the specified [index] of this channel.
   *
   * Note that if there is no data at the specified [index] of this channel, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekBooleanOrNull
   * @see dropBoolean
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popBooleanOrNull(index: Long = cursor.index): Boolean? =
    popOrNull()?.equals(1.toByte())

  /**
   * Pops (returns and deletes) a boolean from the left side of the specified [index] of this
   * channel. In other words, first moves the cursor to the left by one byte, that is:
   * `cursor.moveLeft()`, and then returns and deletes the boolean on the right (not include the
   * byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @see peekLeftBoolean
   * @see dropLeftBoolean
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftBoolean(index: Long = cursor.index): Boolean = popLeft() == 1.toByte()

  /**
   * Pops (returns and deletes) a boolean from the left side of the specified [index] of this
   * channel. In other words, first moves the cursor to the left by one byte, that is:
   * `cursor.moveLeft()`, and then returns and deletes the boolean on the right (not include the
   * byte where the current cursor).
   *
   * Note that if there is no data at the specified [index] of this channel, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekLeftBooleanOrNull
   * @see dropLeftBoolean
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftBooleanOrNull(index: Long = cursor.index): Boolean? =
    popLeftOrNull()?.equals(1.toByte())

  /**
   * Pops (returns and deletes) a short value (two bytes) at the specified [index] of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than two bytes.
   *
   * @see Short.SIZE_BYTES
   * @see peekShort
   * @see dropShort
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popShort(index: Long = cursor.index): Short

  /**
   * Pops (returns and deletes) a short value (two bytes) at the specified [index] of this channel.
   *
   * Note that it returns a boxed short value (two bytes) object, if the right side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Short.SIZE_BYTES
   * @see peekShortOrNull
   * @see dropShort
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popShortOrNull(index: Long = cursor.index): Short?

  /**
   * Pops (returns and deletes) a short value (two bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by two bytes, that
   * is: `cursor.moveTo(index - 2)`, and then returns and deletes the short value (two bytes) on the
   * right (not include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than two bytes.
   *
   * @see Short.SIZE_BYTES
   * @see peekLeftShort
   * @see dropLeftShort
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftShort(index: Long = cursor.index): Short

  /**
   * Pops (returns and deletes) a short value (two bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by two bytes, that
   * is: `cursor.moveTo(index - 2)`, and then returns and deletes the short value (two bytes) on the
   * right (not include the byte where the current cursor).
   *
   * Note that it returns a boxed short value (two bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Short.SIZE_BYTES
   * @see peekLeftShortOrNull
   * @see dropLeftShort
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftShortOrNull(index: Long = cursor.index): Short?

  /**
   * Pops (returns and deletes) a char value (two bytes) at the specified [index] of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than two bytes.
   *
   * @see Char.SIZE_BYTES
   * @see peekChar
   * @see dropChar
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popChar(index: Long = cursor.index): Char

  /**
   * Pops (returns and deletes) a char value (two bytes) at the specified [index] of this channel.
   *
   * Note that it returns a boxed char value (two bytes) object, if the right side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Char.SIZE_BYTES
   * @see peekCharOrNull
   * @see dropChar
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popCharOrNull(index: Long = cursor.index): Char?

  /**
   * Pops (returns and deletes) a char value (two bytes) from the left side of the specified [index]
   * of this channel. In other words, first moves the cursor to the left by two bytes, that is:
   * `cursor.moveTo(index - 2)`, and then returns and deletes the char value (two bytes) on the
   * right (not include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than two bytes.
   *
   * @see Char.SIZE_BYTES
   * @see peekLeftChar
   * @see dropLeftChar
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftChar(index: Long = cursor.index): Char

  /**
   * Pops (returns and deletes) a char value (two bytes) from the left side of the specified [index]
   * of this channel. In other words, first moves the cursor to the left by two bytes, that is:
   * `cursor.moveTo(index - 2)`, and then returns and deletes the char value (two bytes) on the
   * right (not include the byte where the current cursor).
   *
   * Note that it returns a boxed char value (two bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than two bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Char.SIZE_BYTES
   * @see peekLeftCharOrNull
   * @see dropLeftChar
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftCharOrNull(index: Long = cursor.index): Char?

  /**
   * Pops (returns and deletes) an int value (four bytes) at the specified [index] of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than four bytes.
   *
   * @see Int.SIZE_BYTES
   * @see peekInt
   * @see dropInt
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popInt(index: Long = cursor.index): Int

  /**
   * Pops (returns and deletes) an int value (four bytes) at the specified [index] of this channel.
   *
   * Note that it returns a boxed int value (four bytes) object, if the right side of the specified
   * [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Int.SIZE_BYTES
   * @see peekIntOrNull
   * @see dropInt
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popIntOrNull(index: Long = cursor.index): Int?

  /**
   * Pops (returns and deletes) an int value (four bytes) from the left side of the specified
   * [index]
   * of this channel. In other words, first moves the cursor to the left by four bytes, that is:
   * `cursor.moveTo(index - 4)`, and then returns and deletes the int value (four bytes) on the
   * right (not include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than four bytes.
   *
   * @see Int.SIZE_BYTES
   * @see peekLeftInt
   * @see dropLeftInt
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftInt(index: Long = cursor.index): Int

  /**
   * Pops (returns and deletes) an int value (four bytes) from the left side of the specified
   * [index]
   * of this channel. In other words, first moves the cursor to the left by four bytes, that is:
   * `cursor.moveTo(index - 4)`, and then returns and deletes the int value (four bytes) on the
   * right (not include the byte where the current cursor).
   *
   * Note that it returns a boxed int value (four bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Int.SIZE_BYTES
   * @see peekLeftIntOrNull
   * @see dropLeftInt
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftIntOrNull(index: Long = cursor.index): Int?

  /**
   * Pops (returns and deletes) a long value (eight bytes) at the specified [index] of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than eight bytes.
   *
   * @see Long.SIZE_BYTES
   * @see peekLong
   * @see dropLong
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLong(index: Long = cursor.index): Long

  /**
   * Pops (returns and deletes) a long value (eight bytes) at the specified [index] of this channel.
   *
   * Note that it returns a boxed long value (eight bytes) object, if the right side of the
   * specified [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Long.SIZE_BYTES
   * @see peekLongOrNull
   * @see dropLong
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLongOrNull(index: Long = cursor.index): Long?

  /**
   * Pops (returns and deletes) a long value (eight bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by eight bytes,
   * that is: `cursor.moveTo(index - 8)`, and then returns and deletes the long value (eight bytes)
   * on the right (not include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than eight bytes.
   *
   * @see Long.SIZE_BYTES
   * @see peekLeftLong
   * @see dropLeftLong
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftLong(index: Long = cursor.index): Long

  /**
   * Pops (returns and deletes) a long value (eight bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by eight bytes,
   * that is: `cursor.moveTo(index - 8)`, and then returns and deletes the long value (eight bytes)
   * on the right (not include the byte where the current cursor).
   *
   * Note that it returns a boxed long value (eight bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Long.SIZE_BYTES
   * @see peekLeftLongOrNull
   * @see dropLeftLong
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftLongOrNull(index: Long = cursor.index): Long?

  /**
   * Pops (returns and deletes) a float value (four bytes) at the specified [index] of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than four bytes.
   *
   * @see Float.SIZE_BYTES
   * @see peekFloat
   * @see dropFloat
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popFloat(index: Long = cursor.index): Float

  /**
   * Pops (returns and deletes) a float value (four bytes) at the specified [index] of this channel.
   *
   * Note that it returns a boxed float value (four bytes) object, if the right side of the
   * specified [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Float.SIZE_BYTES
   * @see peekFloatOrNull
   * @see dropFloat
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popFloatOrNull(index: Long = cursor.index): Float?

  /**
   * Pops (returns and deletes) a float value (four bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by four bytes, that
   * is: `cursor.moveTo(index - 4)`, and then returns and deletes the float value (four bytes) on
   * the right (not include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than four bytes.
   *
   * @see Float.SIZE_BYTES
   * @see peekLeftFloat
   * @see dropLeftFloat
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftFloat(index: Long = cursor.index): Float

  /**
   * Pops (returns and deletes) a float value (four bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by four bytes, that
   * is: `cursor.moveTo(index - 4)`, and then returns and deletes the float value (four bytes) on
   * the right (not include the byte where the current cursor).
   *
   * Note that it returns a boxed float value (four bytes) object, if the left side of the specified
   * [index] (inclusive) of this channel is less than four bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Float.SIZE_BYTES
   * @see peekLeftFloatOrNull
   * @see dropLeftFloat
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftFloatOrNull(index: Long = cursor.index): Float?

  /**
   * Pops (returns and deletes) a double value (eight bytes) at the specified [index] of this
   * channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than eight bytes.
   *
   * @see Double.SIZE_BYTES
   * @see peekDouble
   * @see dropDouble
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popDouble(index: Long = cursor.index): Double

  /**
   * Pops (returns and deletes) a double value (eight bytes) at the specified [index] of this
   * channel.
   *
   * Note that it returns a boxed double value (eight bytes) object, if the right side of the
   * specified [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peek(index)
   * }.apply {
   *   drop(index)
   * }
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Double.SIZE_BYTES
   * @see peekDoubleOrNull
   * @see dropDouble
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popDoubleOrNull(index: Long = cursor.index): Double?

  /**
   * Pops (returns and deletes) a double value (eight bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by eight bytes,
   * that is: `cursor.moveTo(index - 8)`, and then returns and deletes the double value (eight
   * bytes) on the right (not include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than eight bytes.
   *
   * @see Double.SIZE_BYTES
   * @see peekLeftDouble
   * @see dropLeftDouble
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftDouble(index: Long = cursor.index): Double

  /**
   * Pops (returns and deletes) a double value (eight bytes) from the left side of the specified
   * [index] of this channel. In other words, first moves the cursor to the left by eight bytes,
   * that is: `cursor.moveTo(index - 8)`, and then returns and deletes the double value (eight
   * bytes) on the right (not include the byte where the current cursor).
   *
   * Note that it returns a boxed double value (eight bytes) object, if the left side of the
   * specified [index] (inclusive) of this channel is less than eight bytes, it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.runTemporarily {
   *   peekLeft(index)
   * }.apply {
   *   dropLeft(index)
   * }
   * ```
   *
   * More simply, refer to the following [popLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted & Output: G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see Double.SIZE_BYTES
   * @see peekLeftDoubleOrNull
   * @see dropLeftDouble
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftDoubleOrNull(index: Long = cursor.index): Double?

  /**
   * Pops (returns and deletes) a byte array starting at the specified [index] (inclusive) and
   * consists of [count] bytes to the right.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * ByteArray(count) { pop() }
   * ```
   *
   * @param count The total byte count of the byteArray.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   *
   * @see peekBytes
   * @see dropBytes
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popBytes(count: Int, index: Long = cursor.index): ByteArray

  /**
   * Pops (returns and deletes) a byte array starting at the specified [index] (inclusive) and
   * consists of [count] bytes to the right.
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * ByteArray(count) { pop() }
   * ```
   *
   * @param count The total byte count of the byteArray.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekBytesOrNull
   * @see dropBytes
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popBytesOrNull(count: Int, index: Long = cursor.index): ByteArray?

  /**
   * Pops (returns and deletes) a byte array starting at the specified [index] (inclusive) and
   * consists of [count] bytes to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then returns and deletes the byte array on the right (not
   * include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.moveLeft(count).runTemporarily {
   *   moveRight()
   *   ByteArray(count) { pop() }
   * }
   * ```
   *
   * @param count The total byte count of the byteArray.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   *
   * @see peekLeftBytes
   * @see dropLeftBytes
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftBytes(count: Int, index: Long = cursor.index): ByteArray

  /**
   * Pops (returns and deletes) a byte array starting at the specified [index] (inclusive) and
   * consists of [count] bytes to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then returns and deletes the byte array on the right (not
   * include the byte where the current cursor).
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.moveLeft(count).runTemporarily {
   *   moveRight()
   *   ByteArray(count) { pop() }
   * }
   * ```
   *
   * @param count The total byte count of the byteArray.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekLeftBytesOrNull
   * @see dropLeftBytes
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftBytesOrNull(count: Int, index: Long = cursor.index): ByteArray?

  /**
   * Pops (returns and deletes) a string starting at the specified [index] (inclusive) and consists
   * of [count] bytes to the right.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * ByteArray(count) { pop() }.toString(charset)
   * ```
   *
   * @param count The total byte count of the string.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   *
   * @see peekString
   * @see dropBytes
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popString(
    count: Int,
    index: Long = cursor.index,
    charset: Charset = Charsets.UTF_8
  ): String = popBytes(count).toString(charset)

  /**
   * Pops (returns and deletes) a string starting at the specified [index] (inclusive) and consists
   * of [count] bytes to the right.
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * ByteArray(count) { pop() }.toString(charset)
   * ```
   *
   * @param count The total byte count of the string.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekStringOrNull
   * @see dropBytes
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popStringOrNull(
    count: Int,
    index: Long = cursor.index,
    charset: Charset = Charsets.UTF_8
  ): String? = popBytesOrNull(count)?.toString(charset)

  /**
   * Pops (returns and deletes) a string starting at the specified [index] (inclusive) and consists
   * of [count] bytes to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then returns and deletes the string on the right (not
   * include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.moveLeft(count).runTemporarily {
   *   moveRight()
   *   ByteArray(count) { pop() }.toString(charset)
   * }
   * ```
   *
   * @param count The total byte count of the string.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   *
   * @see peekLeftString
   * @see dropLeftBytes
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftString(
    count: Int,
    index: Long = cursor.index,
    charset: Charset = Charsets.UTF_8
  ): String = popLeftBytes(count).toString(charset)

  /**
   * Pops (returns and deletes) a string starting at the specified [index] (inclusive) and consists
   * of [count] bytes to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then returns and deletes the string on the right (not
   * include the byte where the current cursor).
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.moveLeft(count).runTemporarily {
   *   moveRight()
   *   ByteArray(count) { pop() }.toString(charset)
   * }
   * ```
   *
   * @param count The total byte count of the string.
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekLeftStringOrNull
   * @see dropLeftBytes
   */
  @Throws(IndexOutOfBoundsException::class)
  fun popLeftStringOrNull(
    count: Int,
    index: Long = cursor.index,
    charset: Charset = Charsets.UTF_8
  ): String? = popLeftBytesOrNull(count)?.toString(charset)

  /**
   * Pops (returns and deletes) a line of byte array on the right at the specified [index] of this
   * channel.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the start of the next line terminator
   * (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = pop()) {
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
   * Deleted & Output: F G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   *
   * @see peekLineBytes
   * @see dropLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLineBytes(index: Long = cursor.index): ByteArray

  /**
   * Pops (returns and deletes) a line of byte array on the right at the specified [index] of this
   * channel.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the start of the next line terminator
   * (exclusive), returns `null` if this channel has no more bytes
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = pop()) {
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
   * Deleted & Output: F G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekLineBytesOrNull
   * @see dropLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class
  )
  fun popLineBytesOrNull(index: Long = cursor.index): ByteArray?

  /**
   * Pops (returns and deletes) a line of byte array on the left at the specified [index] of this
   * channel.
   * In other words, first moves the cursor to the end of the previous line, and then returns and
   * deletes the next line (not include the byte where the current cursor).
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the end of the previous line terminator
   * (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = popLeft()) {
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
   * Deleted & Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   *
   * @see peekLeftLineBytes
   * @see dropLeftLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftLineBytes(index: Long = cursor.index): ByteArray

  /**
   * Pops (returns and deletes) a line of byte array on the left at the specified [index] of this
   * channel.
   * In other words, first moves the cursor to the end of the previous line, and then returns and
   * deletes the next line (not include the byte where the current cursor).
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the end of the previous line terminator
   * (exclusive), returns `null` if this channel has no more bytes
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = popLeft()) {
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
   * Deleted & Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekLeftLineBytesOrNull
   * @see dropLeftLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class
  )
  fun popLeftLineBytesOrNull(index: Long = cursor.index): ByteArray?

  /**
   * Pops (returns and deletes) a line of string on the right at the specified [index] of this
   * channel.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the start of the next line terminator
   * (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = pop()) {
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
   * Deleted & Output: F G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   *
   * @see peekLine
   * @see dropLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLine(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String =
    popLineBytes().toString(charset)

  /**
   * Pops (returns and deletes) a line of string on the right at the specified [index] of this
   * channel.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the start of the next line terminator
   * (exclusive), returns `null` if this channel has no more bytes
   *
   * Note that if the right side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = pop()) {
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
   * Deleted & Output: F G
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekLineOrNull
   * @see dropLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class
  )
  fun popLineOrNull(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String? =
    popLineBytesOrNull()?.toString(charset)

  /**
   * Pops (returns and deletes) a line of string on the left at the specified [index] of this
   * channel.
   * In other words, first moves the cursor to the end of the previous line, and then returns and
   * deletes the next line (not include the byte where the current cursor).
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the end of the previous line terminator
   * (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = popLeft()) {
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
   * Deleted & Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   *
   * @see peekLeftLine
   * @see dropLeftLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun popLeftLine(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String =
    popLeftLineBytes().toString(charset)

  /**
   * Pops (returns and deletes) a line of string on the left at the specified [index] of this
   * channel.
   * In other words, first moves the cursor to the end of the previous line, and then returns and
   * deletes the next line (not include the byte where the current cursor).
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result returned not include the terminator, in other words, returns and deletes
   * all bytes from the specified [index] (inclusive) to the end of the previous line terminator
   * (exclusive), returns `null` if this channel has no more bytes
   *
   * Note that if the left side of the specified [index] (inclusive) of this channel is less than
   * [count], it returns `null`.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = popLeft()) {
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
   * Deleted & Output: H I J K L
   * ```
   *
   * @param index The starting index of the data to be popped (default is current [cursor] index).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   *
   * @see peekLeftLineOrNull
   * @see dropLeftLine
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class
  )
  fun popLeftLineOrNull(index: Long = cursor.index, charset: Charset = Charsets.UTF_8): String? = popLeftLineBytesOrNull()?.toString(charset)

  /**
   * Pops (returns and deletes) a byte array from a range of this channel starting at the
   * [startIndex] and ending right before the [endIndex].
   *
   * Note that the default arguments will pop on the current cursor and all remaining bytes
   * afterwards in this channel.
   *
   * @param startIndex The start index (inclusive, default is current [cursor] index).
   * @param endIndex The end index (exclusive, default is channel size).
   *
   * @throws IndexOutOfBoundsException If [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException If [startIndex] is greater than [endIndex].
   *
   * @see peekRangeBytes
   * @see dropRange
   */
  @Throws(
    IndexOutOfBoundsException::class,
    IllegalArgumentException::class
  )
  fun popRangeBytes(startIndex: Long = cursor.index, endIndex: Long = size): ByteArray

  /**
   * Pops (returns and deletes) a string from a range of this channel starting at the [startIndex]
   * and ending right before the [endIndex].
   *
   * Note that the default arguments will pop on the current cursor and all remaining bytes
   * afterwards in this channel.
   *
   * @param startIndex The start index (inclusive, default is current [cursor] index).
   * @param endIndex The end index (exclusive, default is channel size).
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws IndexOutOfBoundsException If [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException If [startIndex] is greater than [endIndex].
   *
   * @see peekRange
   * @see dropRange
   */
  @Throws(
    IndexOutOfBoundsException::class,
    IllegalArgumentException::class
  )
  fun popRange(
    startIndex: Long = cursor.index,
    endIndex: Long = size,
    charset: Charset = Charsets.UTF_8
  ): String = popRangeBytes(startIndex, endIndex).toString(charset)

  /**
   * Pops (returns and deletes) a byte array of all bytes of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * throwIf(isEmpty()) { ChannelEmptyException() }
   * val bos = ByteArrayBuilder()
   * cursor.moveToFirst()
   * while (true) {
   *   popLineBytesOrNull()?.also {
   *     bos.append(it)
   *   } ?: break
   * }
   * bos.toByteArray()
   * ```
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   *
   * @see peekAllBytes
   * @see dropAll
   */
  @Throws(ChannelEmptyException::class)
  fun popAllBytes(): ByteArray

  /**
   * Pops (returns and deletes) a byte array of all bytes of this channel.
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
   *       popLineBytesOrNull()?.also {
   *         bos.append(it)
   *       } ?: break
   *     }
   *     bos.toByteArray()
   *   }
   * }
   * ```
   *
   * @see peekAllBytesOrNull
   * @see dropAll
   */
  fun popAllBytesOrNull(): ByteArray?

  /**
   * Pops (returns and deletes) a string decoded from all bytes of this channel.
   *
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   *
   * @see peekAll
   * @see dropAll
   */
  @Throws(ChannelEmptyException::class)
  fun popAll(charset: Charset = Charsets.UTF_8): String = popAllBytes().toString(charset)

  /**
   * Pops (returns and deletes) a string decoded from all bytes of this channel.
   *
   * Note that if this channel has no more bytes, the `null` is returned.
   *
   * @param charset The charset to use for decoding string (default is utf-8).
   *
   * @see peekAllOrNull
   * @see dropAll
   */
  fun popAllOrNull(charset: Charset = Charsets.UTF_8): String? =
    popAllBytesOrNull()?.toString(charset)

  /**
   * Drops (only deletes without returning) a byte at the specified [index] of this channel.
   *
   * @param index The index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @return Returns `true` if the byte is successfully dropped, otherwise returns `false`.
   *
   * @see Byte.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun drop(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a byte from the left side of the specified [index] of
   * this channel. In other words, first moves the cursor to the left by one byte, that is:
   * `cursor.moveLeft()`, and then only deletes without returning the byte on the right (not include
   * the byte where the current cursor).
   *
   * More simply, refer to the following process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @return Returns `true` if the byte is successfully dropped, otherwise returns `false`.
   *
   * @see Byte.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeft(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a boolean at the specified [index] of this channel.
   *
   * @param index The index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @return Returns `true` if the boolean is successfully dropped, otherwise returns `false`.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropBoolean(index: Long = cursor.index): Boolean = drop()

  /**
   * Drops (only deletes without returning) a boolean from the left side of the specified [index] of
   * this channel. In other words, first moves the cursor to the left by one byte, that is:
   * `cursor.moveLeft()`, and then only deletes without returning the boolean on the right (not
   * include the byte where the current cursor).
   *
   * More simply, refer to the following [dropLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
   *
   * @return Returns `true` if the boolean is successfully dropped, otherwise returns `false`.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftBoolean(index: Long = cursor.index): Boolean = dropLeft()

  /**
   * Drops (only deletes without returning) a short value (two bytes) at the specified [index] of
   * this channel.
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than two bytes.
   *
   * @return Returns `true` if the short value (two bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Short.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropShort(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a short value (two bytes) from the left side of the
   * specified [index] of this channel. In other words, first moves the cursor to the left by two
   * bytes, that is: `cursor.moveTo(index - 2)`, and then only deletes without returning the short
   * value (two bytes) on the right (not include the byte where the current cursor).
   *
   * More simply, refer to the following [dropLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than two bytes.
   *
   * @return Returns `true` if the short value (two bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Short.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftShort(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a char value (two bytes) at the specified [index] of
   * this channel.
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than two bytes.
   *
   * @return Returns `true` if the char value (two bytes) is successfully dropped, otherwise returns
   *   `false`.
   *
   * @see Char.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropChar(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a char value (two bytes) from the left side of the
   * specified [index] of this channel. In other words, first moves the cursor to the left by two
   * bytes, that is: `cursor.moveTo(index - 2)`, and then only deletes without returning the char
   * value (two bytes) on the right (not include the byte where the current cursor).
   *
   * More simply, refer to the following [dropLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than two bytes.
   *
   * @return Returns `true` if the char value (two bytes) is successfully dropped, otherwise returns
   *   `false`.
   *
   * @see Char.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftChar(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) an int value (four bytes) at the specified [index] of
   * this channel.
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than four bytes.
   *
   * @return Returns `true` if the int value (four bytes) is successfully dropped, otherwise returns
   *   `false`.
   *
   * @see Int.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropInt(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) an int value (four bytes) from the left side of the
   * specified [index] of this channel. In other words, first moves the cursor to the left by four
   * bytes, that is: `cursor.moveTo(index - 4)`, and then only deletes without returning the int
   * value (four bytes) on the right (not include the byte where the current cursor).
   *
   * More simply, refer to the following [dropLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than four bytes.
   *
   * @return Returns `true` if the int value (four bytes) is successfully dropped, otherwise returns
   *   `false`.
   *
   * @see Int.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftInt(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a long value (eight bytes) at the specified [index] of
   * this channel.
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than eight bytes.
   *
   * @return Returns `true` if the long value (eight bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Long.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLong(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a long value (eight bytes) from the left side of the
   * specified [index] of this channel. In other words, first moves the cursor to the left by eight
   * bytes, that is: `cursor.moveTo(index - 8)`, and then only deletes without returning the long
   * value (eight bytes) on the right (not include the byte where the current cursor).
   *
   * More simply, refer to the following [dropLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than eight bytes.
   *
   * @return Returns `true` if the long value (eight bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Long.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftLong(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a float value (four bytes) at the specified [index] of
   * this channel.
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than four bytes.
   *
   * @return Returns `true` if the float value (four bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Float.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropFloat(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a float value (four bytes) from the left side of the
   * specified [index] of this channel. In other words, first moves the cursor to the left by four
   * bytes, that is: `cursor.moveTo(index - 4)`, and then only deletes without returning the float
   * value (four bytes) on the right (not include the byte where the current cursor).
   *
   * More simply, refer to the following [dropLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than four bytes.
   *
   * @return Returns `true` if the float value (four bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Float.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftFloat(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a double value (eight bytes) at the specified [index] of
   * this channel.
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than eight bytes.
   *
   * @return Returns `true` if the double value (eight bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Double.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropDouble(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a double value (eight bytes) from the left side of the
   * specified [index] of this channel. In other words, first moves the cursor to the left by eight
   * bytes, that is: `cursor.moveTo(index - 8)`, and then only deletes without returning the double
   * value (eight bytes) on the right (not include the byte where the current cursor).
   *
   * More simply, refer to the following [dropLeft] process:
   * ```
   * Default:
   * ----
   * A B C D E F G
   *             ^
   *
   * Move the cursor to the left:
   * ----
   * A B C D E F G
   *           ^
   *
   * Deleted: G
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than eight bytes.
   *
   * @return Returns `true` if the double value (eight bytes) is successfully dropped, otherwise
   *   returns `false`.
   *
   * @see Double.SIZE_BYTES
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftDouble(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a byte array starting at the specified [index]
   * (inclusive) and consists of [count] bytes to the right.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * ByteArray(count) { drop() }
   * ```
   *
   * @param count The total byte count of the byteArray.
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   *
   * @return Returns `true` if the byte array is successfully dropped, otherwise returns `false`.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropBytes(count: Int, index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a byte array starting at the specified [index]
   * (inclusive) and consists of [count] bytes to the left.
   * In other words, first moves the cursor to the left by [count], that is:
   * `cursor.moveTo(index - count)`, and then only deletes without returning the byte array on the
   * right (not include the byte where the current cursor).
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * cursor.moveLeft(count).runTemporarily {
   *   moveRight()
   *   ByteArray(count) { drop() }
   * }
   * ```
   *
   * @param count The total byte count of the byteArray.
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   *
   * @return Returns `true` if the byte array is successfully dropped, otherwise returns `false`.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftBytes(count: Int, index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a line of byte array on the right at the specified
   * [index] of this channel.
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result deleted not include the terminator, in other words, only deletes without
   * returning all bytes from the specified [index] (inclusive) to the start of the next line
   * terminator (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = drop()) {
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
   * Deleted: F G
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the right side of the specified [index] (inclusive) of
   *   this channel is less than [count].
   *
   * @return Returns `true` if the line is successfully dropped, otherwise returns `false`.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLine(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a line of byte array on the left at the specified
   * [index] of this channel.
   * In other words, first moves the cursor to the end of the previous line, and then only deletes
   * without returning the next line (not include the byte where the current cursor).
   *
   * A line is considered to be terminated by the "Unix" line feed character `\n`, or the "Windows"
   * carriage return character + line feed character `\r\n`, or the "macOS" carriage return
   * character `\r`, or by reaching the end of channel.
   *
   * Note that the result deleted not include the terminator, in other words, only deletes without
   * returning all bytes from the specified [index] (inclusive) to the end of the previous line
   * terminator (exclusive)
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * val bos = ByteArrayBuilder()
   * while (true) {
   *   when (val byte = dropLeft()) {
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
   * Deleted: H I J K L
   * ```
   *
   * @param index The starting index of the data to be dropped (default is current [cursor] index).
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
   *   [size] range of this channel.
   * @throws ChannelUnderflowException If the left side of the specified [index] (inclusive) of this
   *   channel is less than [count].
   *
   * @return Returns `true` if the line is successfully dropped, otherwise returns `false`.
   */
  @Throws(
    ChannelEmptyException::class,
    IndexOutOfBoundsException::class,
    ChannelUnderflowException::class
  )
  fun dropLeftLine(index: Long = cursor.index): Boolean

  /**
   * Drops (only deletes without returning) a byte array from a range of this channel starting at
   * the [startIndex] and ending right before the [endIndex].
   *
   * Note that the default arguments will drop on the current cursor and all remaining bytes
   * afterwards in this channel.
   *
   * @param startIndex The start index (inclusive, default is current [cursor] index).
   * @param endIndex The end index (exclusive, default is channel size).
   *
   * @throws IndexOutOfBoundsException If [startIndex] is less than zero or [endIndex] is greater
   *   than the [size] of this channel.
   * @throws IllegalArgumentException If [startIndex] is greater than [endIndex].
   *
   * @return Returns `true` if the specified range is successfully dropped, otherwise returns
   *   `false`.
   */
  @Throws(
    IndexOutOfBoundsException::class,
    IllegalArgumentException::class
  )
  fun dropRange(startIndex: Long = cursor.index, endIndex: Long = size): Boolean

  /**
   * Drops (only deletes without returning) a byte array of all bytes of this channel.
   *
   * This function is consistent with the behavior of the following expression, but this function
   * has better performance:
   * ```
   * throwIf(isEmpty()) { ChannelEmptyException() }
   * cursor.moveToFirst()
   * while (dropLine()) {}
   * ```
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   *
   * @return Returns `true` if all bytes is successfully dropped, otherwise returns `false`.
   */
  @Throws(ChannelEmptyException::class)
  fun dropAll(): Boolean

}