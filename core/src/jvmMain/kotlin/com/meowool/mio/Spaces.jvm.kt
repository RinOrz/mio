@file:Suppress("NewApi")

package com.meowool.mio

import com.meowool.mio.internal.backport
import java.nio.file.Files

/**
 * Returns the block space of this path.
 *
 * Generally, it is used to mean the space of the file block, such as obtaining the size of
 * the android internal storage space.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
actual val IPath.blockSpace: Long
  get() = backport(
    legacy = { this.toIoFile().totalSpace },
    modern = { Files.getFileStore(this.toNioPath()).blockSize }
  )

/**
 * Returns the available space of this path.
 *
 * @see blockSpace
 */
actual val IPath.availableSpace: Long
  get() = backport(
    legacy = { this.toIoFile().freeSpace },
    modern = { Files.getFileStore(this.toNioPath()).usableSpace }
  )