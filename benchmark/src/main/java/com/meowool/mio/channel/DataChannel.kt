package com.meowool.mio.channel

import java.io.Closeable
import java.io.Flushable
import java.io.IOException

/**
 * Represents a data channel with movable [cursor], and provides support for random read-write
 * access to data of channel.
 *
 * Note that this channel holds a buffer, and all write operations will be temporarily stored in
 * the buffer. Only after calling [flush] or [close] will all changes in the buffer be synchronized
 * to the final destination.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface DataChannel :
  Flushable, Closeable,
  ReadableDataChannel, WriteableDataChannel, DeletableDataChannel {

  /**
   * Flushes this channel writing any buffered data to the underlying I/O destination.
   *
   * @throws IOException If an I/O error occurs
   */
  @Throws(IOException::class)
  override fun flush()

  /**
   *
   * Closes this channel (flushes the channel by calling [flush], and then close other resources).
   * If this channel is already closed then invoking this function will nothing happen.
   *
   * After a channel is closed, any further attempt to invoke I/O operations upon it will cause a
   * [ClosedChannelException] to be thrown.
   *
   * @throws IOException If an I/O error occurs
   */
  @Throws(IOException::class)
  override fun close()
}