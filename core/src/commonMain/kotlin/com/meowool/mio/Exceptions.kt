@file:Suppress("MemberVisibilityCanBePrivate")

package com.meowool.mio

private fun constructMessage(path: Path?, other: Path?, reason: String?): String {
  val sb = StringBuilder(path.toString())
  if (other != null) {
    sb.append(" -> $other")
  }
  if (reason != null) {
    sb.append(": $reason")
  }
  return sb.toString()
}

/**
 * Signals that an I/O exception of some sort has occurred. This
 * class is the general class of exceptions produced by failed or
 * interrupted I/O operations.
 */
open class IOException(message: String?) : Exception(message)

/**
 * A base exception class for file path system exceptions.
 *
 * @property path the file path on which the failed operation was performed.
 * @property other the second file path involved in the operation, if any (for example, the target of a copy or move)
 * @property reason the description of the error
 */
open class FileSystemException(
  val path: Path?,
  val other: Path? = null,
  val reason: String? = null,
) : IOException(constructMessage(path, other, reason))

/**
 * An exception class which is used when some file path to create or copy to already exists.
 */
class PathAlreadyExistsException(
  path: Path,
  other: Path? = null,
  reason: String? = null,
) : FileSystemException(path, other, reason)

/**
 * An exception class which is used when we have not enough access for some operation.
 */
class AccessDeniedException(
  path: Path,
  other: Path? = null,
  reason: String? = null,
) : FileSystemException(path, other, reason)

/**
 * An exception class which is used when file path to copy does not exist.
 */
class NoSuchFileException(
  path: Path,
  other: Path? = null,
  reason: String? = null,
) : FileSystemException(path, other, reason)

/**
 * Checked exception thrown when a file system operation fails because a
 * directory is not empty.
 */
class DirectoryNotEmptyException(dir: Path?) : FileSystemException(dir)

/**
 * An exception indicates that the [path] is illegal.
 */
class IllegalPathException(
  path: Path,
  message: String? = null,
) : FileSystemException(
  path,
  reason = buildString {
    append("Path '$path' is illegal")
    if (message.isNullOrEmpty().not()) {
      append(',')
      append(message)
    } else append("!")
  }
)