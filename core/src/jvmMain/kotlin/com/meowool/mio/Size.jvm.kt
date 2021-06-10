package com.meowool.mio

import java.nio.file.Files

/**
 * Returns the block size of this file or directory.
 *
 * Generally, it is used to means the size of the file block, such as obtaining the size of
 * the android internal storage space.
 */
actual val Path.blockSize: Long get() = runCatching {
  Files.getFileStore(this.toNioPath()).blockSize
}.getOrElse {
  this.toFile().totalSpace
}

/**
 * Returns the available size of this file or directory.
 *
 * @see blockSize
 */
actual val Path.availableSize: Long get() = runCatching {
  Files.getFileStore(this.toNioPath()).usableSpace
}.getOrElse {
  this.toFile().freeSpace
}