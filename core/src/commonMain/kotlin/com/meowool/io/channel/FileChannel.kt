package com.meowool.io.channel

import com.meowool.io.File

/**
 * Represents a file channel with movable [cursor], and provides support for random read-write
 * access to data of channel.
 *
 * Note that this channel holds a writable buffer, and all write operations will be temporarily
 * stored in the buffer. Only after calling [flush] or [close] will all changes in the buffer be
 * synchronized to the final destination.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface FileChannel : DataChannel {

  /**
   * Returns the file object to which this file channel belongs.
   */
  val file: File
}
