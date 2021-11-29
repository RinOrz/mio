package com.meowool.io.channel

import com.meowool.io.IOException

/**
 * Represents a data channel.
 *
 * Note that this channel holds a writable buffer, and all write operations will be temporarily
 * stored in the buffer. Only after calling [flush] or [close] will all changes in the buffer be
 * synchronized to the final destination.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface DataChannel : SuspendCloseable, SuspendFlushable {

  /**
   * Convert all data of this channel to byte array.
   */
  suspend fun toBytes(): ByteArray

  /**
   * Flushes this channel writing any buffered data to the underlying I/O destination.
   *
   * @throws IOException If an I/O error occurs
   */
  @Throws(IOException::class)
  override suspend fun flush()

  /**
   * Closes this channel (flushes the channel by calling [flush], and then close other resources).
   * If this channel is already closed then invoking this function will nothing happen.
   *
   * After a channel is closed, any further attempt to invoke I/O operations upon it will cause a
   * [ClosedChannelException] to be thrown.
   *
   * @throws IOException If an I/O error occurs
   */
  @Throws(IOException::class)
  override suspend fun close()
}