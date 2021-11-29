package com.meowool.io.channel

import com.meowool.io.IOException

/**
 * Represents an interface that can flushes data to the destination.
 * Such as [DataChannel].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface SuspendFlushable {

  /**
   * Flushes this channel writing any buffered data to the underlying I/O destination.
   *
   * @throws IOException If an I/O error occurs
   */
  @Throws(IOException::class)
  suspend fun flush()
}