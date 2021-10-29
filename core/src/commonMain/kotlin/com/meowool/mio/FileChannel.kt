package com.meowool.mio

import com.meowool.mio.File as AnyFile

/**
 * Represents a file channel with a bidirectional movable cursor ([startCursor], [endCursor]),
 * and provides support for random read-write access to data of [file].
 *
 * Note that this channel holds a buffer, and all write operations will be temporarily stored in
 * the buffer. Only after calling [flush] or [close] will all changes in the buffer be synchronized
 * to the real file.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface FileChannel<File: AnyFile> : DataChannel {

  /**
   * Returns the file object to which this file channel belongs.
   */
  val file: File
}
