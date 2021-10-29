package com.meowool.mio.channel

import com.meowool.mio.ChannelEmptyException

/**
 * The cursor of data channel, which is the [index] of the data being accessed.
 *
 * This is best explained by analogy. Imagine you're in a terminal, the highlighted cursor is the
 * data position of the channel you are currently accessing.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface DataChannelCursor {

  /**
   * The index of the cursor, which is the position in the accessed channel.
   *
   * Changing this value means moving the cursor to access data in the channel.
   * Note that this value cannot be a negative number.
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
   * @return This cursor
   */
  fun moveTo(index: Long): DataChannelCursor

  /**
   * Moves the cursor to the first index in the data channel.
   *
   * @return This cursor
   *
   * @see DataChannelInfo.firstIndex
   */
  fun moveToFirst(): DataChannelCursor

  /**
   * Moves the cursor to the last index in the data channel.
   *
   * @return This cursor
   *
   * @see DataChannelInfo.lastIndex
   */
  fun moveToLast(): DataChannelCursor

  /**
   * Move the cursor to the next byte in the data channel.
   *
   * For example, current [index] is `1`, the [repeat] is `2`:
   * ```
   * a  b  c  d  e  f  g
   *          ^
   * ```
   *
   * @param repeat The value of repeated moves can be used to control how many times to move right.
   *
   * @return This cursor.
   */
  fun moveRight(repeat: Long = 1): DataChannelCursor

  /**
   * Move the cursor to the previous byte in the data channel.
   *
   * For example, current [index] is `4`, the [repeat] is `2`:
   * ```
   * a  b  c  d  e  f  g
   *       ^
   * ```
   *
   * @param repeat The value of repeated moves can be used to control how many times to move left.
   * @return This cursor.
   */
  fun moveLeft(repeat: Long = 1): DataChannelCursor

  /**
   * Moves the cursor to the beginning of current line of the data channel.
   *
   * For example:
   * ```
   * Initialize:
   * a  b  c  d  e  f  g
   *             ^
   *
   * Result:
   * a  b  c  d  e  f  g
   * ^
   * ```
   *
   * @return This cursor.
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   */
  @Throws(ChannelEmptyException::class)
  fun moveToStartOfLine(): DataChannelCursor

  /**
   * Moves the cursor to the ending of current line of the data channel.
   *
   * For example:
   * ```
   * Initialize:
   * a  b  c  d  e  f \n
   *       ^
   *
   * Result:
   * a  b  c  d  e  f  \n
   *                   ^
   * ```
   *
   * @return This cursor.
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   */
  @Throws(ChannelEmptyException::class)
  fun moveToEndOfLine(): DataChannelCursor

  /**
   * Moves the cursor to the beginning of the next line of the data channel.
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
   * @param repeat The value of repeated moves can be used to control how many times to move down.
   *
   * @return This cursor.
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   */
  @Throws(ChannelEmptyException::class)
  fun moveToNextLine(repeat: Long = 1): DataChannelCursor

  /**
   * Moves the cursor to the beginning of the previous line of the data channel.
   *
   * For example:
   * ```
   * Initialize:
   * a  b  c  d  e  f  g
   * h  i  j  k  l  m  n
   *             ^
   *
   * Result:
   * a  b  c  d  e  f  g
   * ^
   * h  i  j  k  l  m  n
   * ```
   *
   * @param repeat The value of repeated moves can be used to control how many times to move up.
   *
   * @return This cursor.
   *
   * @throws ChannelEmptyException If this channel has no more bytes.
   */
  @Throws(ChannelEmptyException::class)
  fun moveToPreviousLine(repeat: Long = 1): DataChannelCursor

  /**
   * Remembers the [index] of this cursor.
   *
   * @return This cursor.
   */
  fun remember(): DataChannelCursor

  /**
   * Remembers the [index] of this cursor.
   *
   * @return This cursor.
   */
  fun restore(): DataChannelCursor

  /**
   * Moves this cursor to the beginning of the data channel, that is index zero.
   *
   * @return This cursor.
   *
   * @see DataChannelInfo.cursor
   * @see DataChannelInfo.firstIndex
   */
  fun first(): DataChannelCursor

  /**
   * Moves this cursor to the ending of the data channel, that the index is `channel.size - 1`.
   *
   * @return This cursor.
   *
   * @see DataChannelInfo.lastIndex
   */
  fun last(): DataChannelCursor
}

/**
 * Creates a temporary operation [block] for this cursor and returns its execution result.
 *
 * Note that any operation on the cursor will be forgotten after the [block] is executed, that is,
 * reset the cursor.
 *
 * @return This cursor.
 *
 * @see kotlin.apply
 *
 * @author 凛 (https://github.com/RinOrz)
 */
inline fun <R> DataChannelCursor.applyTemporarily(
  block: DataChannelCursor.() -> Unit
): DataChannelCursor = remember().apply(block).restore()

/**
 * Creates a temporary operation [block] for this cursor and returns its execution result.
 *
 * Note that any operation on the cursor will be forgotten after the [block] is executed, that is,
 * reset the cursor.
 *
 * @return The result of [block] execution.
 *
 * @see kotlin.run
 *
 * @author 凛 (https://github.com/RinOrz)
 */
inline fun <R> DataChannelCursor.runTemporarily(
  block: DataChannelCursor.() -> R
): R = remember().block().also { restore() }