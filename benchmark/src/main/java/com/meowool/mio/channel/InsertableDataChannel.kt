package com.meowool.mio.channel

import com.meowool.mio.ChannelUnderflowException

/**
 * Abstract the interfaces for inserting data in the data channel.
 *
 * This interface provides the ability to insert data randomly, through the [cursor] can directly
 * insert the data at the beginning, at the middle, and even at the end of the channel.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface InsertableDataChannel : DataChannelInfo {

  /**
   * Pushes (inserts) a [byte] to the specified [index], and then moves the cursor to after the
   * byte being pushed.
   *
   * @param index The starting target index to push data to (default is current [cursor] index).
   *
   * @throws IndexOutOfBoundsException If the specified [index] is less than zero.
   * @throws ChannelUnderflowException If exceeds the extreme size range of this channel.
   */
  fun push(byte: Byte, index: Int = 5)
}