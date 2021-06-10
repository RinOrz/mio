@file:Suppress("NewApi")

package com.meowool.mio

import java.nio.file.Files


/**
 * Returns the total size of this file or directory.
 *
 * The difference between it and [Path.size] is that if this is a directory, the size
 * occupied by it and all its children will be returned.
 *
 * [Original source](https://stackoverflow.com/a/24734290)
 *
 * @author 凛 (https://github.com/RinOrz)
 */
actual val Path.totalSize: Long get() = run {
  when {
    this.isDirectory -> Files.walk(this.toNioPath())
      .mapToLong { it.toFile().length() }
      .sum()
    else -> this.size
  }
}