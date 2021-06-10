package com.meowool.mio

import com.meowool.mio.internal.DeleteRecursivelyVisitor
import com.meowool.mio.internal.FilePath
import java.nio.file.Files

/**
 * Cleans a directory including sub-directories without deleting directories.
 *
 * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
 * link final target, otherwise delete the symbolic link itself.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.createFile
 */
actual fun Path.cleanDirectory(
  followLinks: Boolean,
  filter: (Path) -> Boolean,
  onError: (Path, Throwable) -> DeleteErrorSolution
) {
  requireDirectory()
  runCatching {
    Files.walkFileTree(
      absolute.toNioPath(),
      DeleteRecursivelyVisitor(
        isStrict = true,
        followLinks = followLinks,
        filter = filter,
        onError = onError,
        safeRoot = absolute.toNioPath()
      )
    )
  }.getOrElse {
    this.toFile().listFiles()?.forEach {
      FilePath(it).delete(recursively = true, followLinks, filter, onError)
    }
  }
}

/** @see Path.add */
operator fun Path.plus(child: NioPath) = apply { add(child.toMioPath()) }
operator fun Path.plusAssign(child: NioPath) = add(child.toMioPath())