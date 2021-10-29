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
 * Signals that an I/O exception to some sort has occurred. This
 * class is the general class of exceptions produced by failed or
 * interrupted I/O operations.
 */
expect open class IOException(message: String?) : Exception

/**
 * Checked exception thrown when an attempt is made to invoke or complete an I/O operation upon
 * channel that is closed, or at least closed to that operation.  That this exception is thrown
 * does not necessarily imply that the channel is completely closed. A socket channel whose write
 * half has been shut down, for example, may still be open for reading.
 */
open class ClosedChannelException : Exception()

/**
 * Channel is empty exception.
 */
open class ChannelEmptyException(message: String? = null) : Exception(message)

/**
 * A base exception class for file path system exceptions.
 *
 * @property path the file path on which the failed operation was performed.
 * @property other the second file path involved in the operation, if
 *   any (for example, the target of a copy or move)
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
  reason: String? = "The path already exists!",
) : FileSystemException(path, other, reason)

/**
 * The parent directories of the given [path] does not exist.
 */
class ParentDirectoryNotExistsException(path: Path) :
  FileSystemException(path, reason = "The parent directories has not been created!")

/**
 * An exception when what is needed is a directory instead of a file.
 */
class PathExistsAndIsNotDirectoryException(path: Path) :
  FileSystemException(path, reason = "The path already exists, and it is a file and not a directory!")

/**
 * An exception when what is needed is a file instead of a directory.
 */
class PathExistsAndIsNotFileException(path: Path) :
  FileSystemException(path, reason = "The path already exists, and it is a directory and not a file!")

/**
 * When a "hard link" already exists for a file [path].
 */
class LinkAlreadyExistsException(path: Path) : FileSystemException(path)

/**
 * An exception class which is used when we have not enough accessed for some operation.
 */
class AccessDeniedException(
  path: Path,
  other: Path? = null,
  reason: String? = null,
) : FileSystemException(path, other, reason)

/**
 * An exception class which is used when path does not exist.
 */
class NoSuchPathException(
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
 * Checked exception thrown when a file system operation fails because a group is not empty.
 */
class GroupNotEmptyException(group: PathGroup?) : FileSystemException(group)

/**
 * An exception indicates that the [path] is illegal.
 */
class IllegalPathException(
  path: Path,
  message: String? = null,
) : FileSystemException(
  path,
  reason = buildString {
    append("Path is illegal")
    if (message.isNullOrEmpty().not()) {
      append(',')
      append(message)
    } else append("!")
  }
)