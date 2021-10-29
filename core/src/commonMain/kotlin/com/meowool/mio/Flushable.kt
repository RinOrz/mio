package com.meowool.mio

/**
 * Represents an interface that can flushes data to the destination. Usually a data channel.
 *
 * @see DataChannel
 * @author 凛 (https://github.com/RinOrz)
 */
interface Flushable {

  /**
   * Flushes this channel writing any buffered data to the underlying I/O destination.
   *
   * @throws IOException If an I/O error occurs
   */
  @Throws(IOException::class)
  fun flush()
}