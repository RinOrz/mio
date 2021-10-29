package com.meowool.mio.channel

import com.meowool.mio.ChannelUnderflowException

/**
 * Abstract the interfaces for writing data in the data channel.
 *
 * This interface provides the ability to write data randomly, through the [cursor] can directly
 * write the data at the beginning, at the middle, and even at the end of the channel.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface WriteableDataChannel : DataChannelInfo {

  /**
   * Sets (replaces) a [byte] at the specified [index] of this channel, and then moves the cursor to the right
   * of the byte being set, that is: `cursor.moveRight()`.
   *
   * Note that if there is no data at the specified index or the channel size range is exceeded,
   * empty bytes will be used to grow the channel size, and then fills the given [byte].
   *
   * @param index The index of the data to be replaced (default is current [cursor] index).
   * @param byte The new data to be used to replace old data.
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero.
   */
  operator fun set(index: Long = cursor.index, byte: Byte)

  /**
   * Amends (replaces) a [byte] at the current [cursor] index of this channel, and then moves the cursor to the right
   * of the byte being amended, that is: `cursor.moveRight()`.
   *
   * Note that if there is no data at the current [cursor] index or the channel size range is exceeded,
   * empty bytes will be used to grow the channel size, and then fills the given [byte].
   *
   * @param byte The new data to be used to replace old data.
   */
  fun amend(byte: Byte)

  /**
   * Amends (replaces) a [byte] at the specified [index] of this channel, and then moves the cursor to the right
   * of the byte being set, that is: `cursor.moveRight()`.
   *
   * Note that if there is no data at the specified index or the channel size range is exceeded,
   * empty bytes will be used to grow the channel size, and then fills the given [byte].
   *
   * @param index The index of the data to be amended (default is current [cursor] index).
   * @param byte The new data to be used to replace old data.
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero.
   */
  fun amend(index: Long, byte: Byte)
}
