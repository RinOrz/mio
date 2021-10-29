package com.meowool.mio.channel

/**
 * Abstract the most basic information of a data channel.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface DataChannelInfo {

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
   * The remaining size to the right of current [cursor] index in this channel.
   */
  val remainingSize: Long get() = size - cursor.index

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

  /**
   * The movable cursor of this channel, which is the `index` of the data being accessed.
   *
   * This is best explained by analogy. Imagine you're in a terminal, the highlighted cursor is the
   * data position of the channel you are currently accessing.
   *
   * @see DataChannelCursor.index
   */
  val cursor: DataChannelCursor

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
}