@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package com.meowool.mio

import kotlinx.coroutines.flow.Flow

/**
 * An object representing the file system and its path.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface Path : CharSequence, Comparable<Path> {

  /**
   * Returns the absolute path site of this file/directory.
   */
  val absolute: Path

  /**
   * Returns an absolute path site represent the real path of this file/directory located. If this
   * is a symbolic link, it will be resolved to the final target.
   */
  val real: Path

  /**
   * The canonical path site of this file/directory.
   */
  val canonical: Path

  /**
   * Returns true if the path is absolute.
   * 
   * An absolute path is complete in that it doesn't need to be combined with other path 
   * information in order to locate a file/directory.
   */
  val isAbsolute: Boolean

  /**
   * The name of this file/directory.
   */
  var name: String

  /**
   * The extension of this file (not including the dot). If there is only one dot and it is
   * first in the name, the extension will be empty.
   *
   * For example getting,
   * ```
   * foo.txt     -> "txt"
   * .aaa.jpg    -> "jpg"
   * .xyz        -> ""
   * ```
   *
   * For example setting,
   * ```
   * foo.txt     -- zip    -> foo.zip
   * .aaa.jpg    -- png    -> .aaa.zip
   * .xyz        -- .txt   -> .xyz.txt
   * ```
   */
  var extension: String

  /**
   * The extension of this file (including the dot). If there is only one dot and it is first in
   * the name, the extension will be empty.
   *
   * For example,
   * ```
   * foo.txt     -> ".txt"
   * .aaa.jpg    -> ".jpg"
   * .xyz        -> ""
   * ```
   *
   * @see extension
   */
  val extensionWithDot: String

  /**
   * The file's name without an extension. If there is only one dot and it is first in the
   * [name], it will be all of [name].
   *
   * For example getting,
   * ```
   * foo.txt     -> foo
   * .aaa.jpg    -> .aaa
   * .xyz        -> .xyz
   * ```
   *
   * For example setting,
   * ```
   * foo.txt     -- bar   -> bar.txt
   * .aaa.jpg    -- bbb   -> bbb.zip
   * .xyz        -- ccc   -> ccc
   * ```
   */
  var nameWithoutExtension: String

  /**
   * The parent path site of this file/directory.
   */
  var parent: Path

  /**
   * The time in milliseconds of last modification.
   *
   * If the file system implementation does not support a time stamp to indicate the time of
   * last modification then this property returns an implementation specific default value,
   * typically milliseconds representing the epoch (1970-01-01T00:00:00Z).
   */
  var lastModifiedTime: Long

  /**
   * The time in milliseconds of last access.
   *
   * If the file system implementation does not support a time stamp to indicate the time of
   * last access then this property returns an implementation specific default value, typically
   * the [lastModifiedTime] or milliseconds representing the epoch (1970-01-01T00:00:00Z).
   */
  var lastAccessTime: Long

  /**
   * The creation time in milliseconds is the time that the file was created.
   *
   * If the file system implementation does not support a time stamp to indicate the time when
   * the file was created then this property returns an implementation specific default value,
   * typically the [lastModifiedTime] or milliseconds representing the epoch (1970-01-01T00:00:00Z).
   */
  var creationTime: Long

  /**
   * The file whether is readable.
   */
  var isReadable: Boolean

  /**
   * The file whether is writable.
   */
  var isWritable: Boolean

  /**
   * The file whether is executable.
   */
  var isExecutable: Boolean

  /**
   * This is whether or not a hidden file/directory.
   *
   * The exact definition of hidden is platform or provider dependent.
   * On UNIX for example a file/directory is considered to be hidden if its name begins with a dot.
   * On Windows a file is considered hidden if it isn't a directory and the [isHidden] attribute is set.
   */
  var isHidden: Boolean

  /**
   * Returns `true` if the file is a regular file with opaque content.
   */
  val isRegularFile: Boolean

  /**
   * Returns `true` if the file is a directory.
   */
  val isDirectory: Boolean

  /**
   * Returns `true` if the file is a symbolic link.
   */
  val isSymbolicLink: Boolean

  /**
   * Returns `true` if the file something other than a regular file, directory or symbolic link.
   */
  val isOther: Boolean

  /**
   * Returns the size of the file (in bytes). The size may differ from the actual size on the file
   * system due to compression, support for sparse files, or other reasons. The size of files that
   * are not [isRegularFile] files is implementation specific and therefore unspecified.
   */
  val size: Long

  /**
   * Returns a readable size string.
   *
   * @see size
   * @see com.meowool.toolkit.sweekt.toReadableSize for more details
   */
  val readableSize: String

  /**
   * Return an object that uniquely identifies the given file, or null.
   */
  val key: Any?

  /**
   * Probes the content type (MIME type) of this site.
   *
   * Note that this property is not necessarily accurate, even empty. If you want to get very
   * accurate results, you can use other content detection libraries such as [Apache-Tika](https://tika.apache.org/).
   */
  val contentType: String

  /**
   * Creates a empty file, if the file already exists, nothing will happen. The check for
   * the existence of the file and the creation of the new file if it does not exist are a single
   * operation that is atomic with respect to all other filesystem activities that might affect
   * the directory.
   *
   * @see createStrictFile
   */
  fun createFile(): Path

  /**
   * Creates a empty file, failing if the file already exists. The check for the existence of the file
   * and the creation of the new file if it does not exist are a single operation that is atomic with
   * respect to all other filesystem activities that might affect the directory.
   *
   * @throws PathAlreadyExistsException if a file of that name already exists
   * @see createFile
   */
  @Throws(PathAlreadyExistsException::class)
  fun createStrictFile(): Path

  /**
   * Creates a directory, if the directory already exists, nothing will happen, but note that if it is
   * not a directory, an [PathAlreadyExistsException] will be thrown.
   *
   * @see createStrictDirectory
   */
  @Throws(PathAlreadyExistsException::class)
  fun createDirectory(): Path

  /**
   * Creates a directory, failing if the directory already exists.
   *
   * @see createDirectory
   */
  @Throws(PathAlreadyExistsException::class)
  fun createStrictDirectory(): Path

  /**
   * Creates a directory, including any necessary but nonexistent parent directories.
   */
  fun createDirectories(): Path

  /**
   * Deletes a path file safely. If the file is a symbolic link, then the symbolic link itself, not
   * the final target of the link, is deleted, if you want to change this behavior, set
   * [followLinks] to `true`, this will remove the target of the link.
   *
   * If the path is a directory then the directory must be empty, otherwise the deletion fails.
   * If you want to delete the directory and all its children, set [recursively] to `true`.
   *
   * @param recursively if this path is a directory, deletes it and all its children.
   * @param followLinks if the path is a symbolic link and the value is `true`, then delete the
   * link final target, otherwise delete the symbolic link itself.
   * @param filter if this is a directory and [recursively] is true, you can filter to exclude some
   * files from deleting.
   * @param onError what should be done when an error occurs when deleting this path.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see deleteStrictly
   */
  fun delete(
    recursively: Boolean = false,
    followLinks: Boolean = false,
    filter: (Path) -> Boolean = { true },
    onError: (Path, Throwable) -> DeleteErrorSolution = { _, throwable -> throw throwable }
  ): Boolean

  /**
   * Deletes a path file strictly. If the file is a symbolic link, then the symbolic link itself,
   * not the final target of the link, is deleted, if you want to change this behavior, set
   * [followLinks] to `true`, this will remove the target of the link.
   *
   * If the path is a directory then the directory must be empty, otherwise throw the
   * [DirectoryNotEmptyException].
   * If you want to delete the directory and all its children, set [recursively] to `true`.
   *
   * @param recursively if this path is a directory, deletes it and all its children.
   * @param followLinks if the path is a symbolic link and the value is `true`, then delete the
   * link final target, otherwise delete the symbolic link itself.
   * @param filter if this is a directory and [recursively] is true, you can filter to exclude some
   * files from deleting.
   * @param onError what should be done when an error occurs when deleting this path.
   *
   * @throws DirectoryNotEmptyException if the path is a non-empty directory, and [recursively]
   * is set to `false`, it cannot be deleted.
   *
   * @see delete
   */
  @Throws(NoSuchFileException::class, DirectoryNotEmptyException::class)
  fun deleteStrictly(
    recursively: Boolean = false,
    followLinks: Boolean = false,
    filter: (Path) -> Boolean = { true },
    onError: (Path, Throwable) -> DeleteErrorSolution = { _, throwable -> throw throwable }
  )

  /**
   * Returns `true` if this file/directory is empty.
   *
   * If this path is a file, it means that the file has no content.
   * If this path is a directory, it means that there are no files in this directory.
   */
  fun isEmpty(): Boolean

  /**
   * Returns `true` if this file/directory is not empty.
   *
   * If this path is a file, it means that the file has content.
   * If this path is a directory, it means that there are files in this directory.
   */
  fun isNotEmpty(): Boolean

  /**
   * Returns `true` if this file/directory is exists.
   *
   * @param followLinks if this is a symbolic link, whether to ensure the final real target of link
   * is exists.
   *
   * @see notExists
   */
  fun exists(followLinks: Boolean = true): Boolean

  /**
   * Returns `true` if this file/directory does not exist.
   *
   * @param followLinks if this is a symbolic link, whether to ensure the final real target of link
   * is not exists.
   *
   * @see exists
   */
  fun notExists(followLinks: Boolean = true): Boolean

  /**
   * Return all file entries in the directory.
   *
   * @param recursively represents whether you need to return a recursive list. this is consistent
   * with the [descendants] (1 or Int.MAX_VALUE) behavior.
   */
  fun children(recursively: Boolean = false): Flow<Path>

  /**
   * Return all file entries in this directory by traversing the file tree.
   *
   * Similar to [walk], but does not include itself.
   *
   * @param maxDepth the maximum number of directory levels to traverse. when the value
   * is [Int.MAX_VALUE], recursively traverse all sub directories.
   */
  fun descendants(maxDepth: Int = Int.MAX_VALUE): Flow<Path>

  /**
   * Return all file entries by walking the file tree.
   *
   * Similar to [descendants], but include itself.
   *
   * @param maxDepth the maximum number of directory levels to walk. when the value
   * is [Int.MAX_VALUE], walk all directories.
   */
  fun walk(maxDepth: Int = Int.MAX_VALUE): Flow<Path>

  /**
   * Copies this path to the given [target] path.
   *
   * If this path is a directory, it is copied without its content, i.e. an empty [target]
   * directory is created.
   * If you want to copied directory including its contents, set [recursively] to `true`.
   *
   * @param overwrite whether to overwrite when the target file already exists.
   * @param recursively if this path is a directory, copy it and all its children to destination.
   * @param keepAttributes copied all the attributes of this path to the target file.
   * @param followLinks if the file to be copied is a symbolic link and the value is `true`, then
   * copy the link target, otherwise copy the symbolic link itself.
   * @param filter if this is a directory and [recursively] is true, you can filter to exclude some
   * files from copying.
   * @param onError what should be done when an error occurs when copying this path.
   *
   * @return the path to the [target]
   *
   * @throws PathAlreadyExistsException if the target file already exists and [overwrite]
   * argument is set to `false`.
   * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
   * argument is set to `false`, the copy fails, the target folder must be emptied first.
   */
  @Throws(PathAlreadyExistsException::class, DirectoryNotEmptyException::class)
  fun copyTo(
    target: Path,
    overwrite: Boolean = false,
    recursively: Boolean = false,
    keepAttributes: Boolean = false,
    followLinks: Boolean = true,
    filter: (Path) -> Boolean = { true },
    onError: (Path, Throwable) -> CopyErrorSolution = { _, throwable -> throw throwable }
  ): Path

  /**
   * Copies this path into the given [targetDirectory].
   *
   * If this path is a directory, it is copied without its content, i.e. an empty directory is
   * created to [targetDirectory].
   * If you want to copied directory including its contents, set [recursively] to `true`.
   *
   * @param overwrite whether to overwrite when the target file already exists.
   * @param recursively if this path is a directory, copy it and all its children into destination.
   * @param keepAttributes copied all the attributes of this path to the target file.
   * @param followLinks if the file to be copied is a symbolic link and the value is `true`, then
   * copy the link target, otherwise copy the symbolic link itself.
   * @param filter if this is a directory and [recursively] is true, you can filter to exclude some
   * files from copying.
   * @param onError what should be done when an error occurs when copying this path or this
   * directory sub files.
   *
   * @return the path to the [targetDirectory]
   *
   * @throws PathAlreadyExistsException if the target file already exists and [overwrite]
   * argument is set to `false`.
   * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
   * argument is set to `true`, the copy fails, the target folder must be emptied first.
   */
  @Throws(PathAlreadyExistsException::class, DirectoryNotEmptyException::class)
  fun copyInto(
    targetDirectory: Path,
    overwrite: Boolean = false,
    recursively: Boolean = false,
    keepAttributes: Boolean = false,
    followLinks: Boolean = true,
    filter: (Path) -> Boolean = { true },
    onError: (Path, Throwable) -> CopyErrorSolution = { _, throwable -> throw throwable }
  ): Path

  /**
   * Moves this path to the given [target] path.
   *
   * If this path is a directory, it is moved without its content, i.e. an empty [target]
   * directory is created. If you want to moved directory including its contents, use [recursively].
   *
   * @param overwrite whether to overwrite when the target file already exists.
   * @param recursively if this path is a directory, move it and all its children to destination.
   * @param keepAttributes moved all the attributes of this path to the target file.
   * @param followLinks if the file to be moved is a symbolic link and the value is `true`, then
   * move the link target, otherwise move the symbolic link itself.
   * @param filter if this is a directory and [recursively] is true, you can filter to exclude some
   * files from moving.
   * @param onError what should be done when an error occurs when moving this path.
   *
   * @return the path to the [target]
   *
   * @throws PathAlreadyExistsException if the target file already exists and [overwrite]
   * argument is set to `false`.
   * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
   * argument is set to `true`, the copy fails, the target folder must be emptied first.
   */
  @Throws(PathAlreadyExistsException::class, DirectoryNotEmptyException::class)
  fun moveTo(
    target: Path,
    overwrite: Boolean = false,
    recursively: Boolean = false,
    keepAttributes: Boolean = false,
    followLinks: Boolean = true,
    filter: (Path) -> Boolean = { true },
    onError: (Path, Throwable) -> MoveErrorSolution = { _, throwable -> throw throwable }
  ): Path

  /**
   * Moves this path into the given [targetDirectory].
   *
   * If this path is a directory, it is moved without its content, i.e. an empty directory is
   * created to [targetDirectory]. If you want to moved directory including its contents,
   * set [recursively] to `true`.
   *
   * @param overwrite whether to overwrite when the target file already exists.
   * @param recursively if this path is a directory, move it and all its children into destination.
   * @param keepAttributes moved all the attributes of this path to the target file.
   * @param followLinks if the file to be moved is a symbolic link and the value is `true`, then
   * move the link target, otherwise move the symbolic link itself.
   * @param filter if this is a directory and [recursively] is true, you can filter to exclude some
   * files from moving.
   * @param onError what should be done when an error occurs when moving this path or this
   * directory sub files.
   *
   * @return the path to the [targetDirectory]
   *
   * @throws PathAlreadyExistsException if the target file already exists and [overwrite]
   * argument is set to `false`.
   * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
   * argument is set to `true`, the copy fails, the target folder must be emptied first.
   */
  @Throws(PathAlreadyExistsException::class, DirectoryNotEmptyException::class)
  fun moveInto(
    targetDirectory: Path,
    overwrite: Boolean = false,
    recursively: Boolean = false,
    keepAttributes: Boolean = false,
    followLinks: Boolean = true,
    filter: (Path) -> Boolean = { true },
    onError: (Path, Throwable) -> MoveErrorSolution = { _, throwable -> throw throwable }
  ): Path

  /**
   * Compares two abstract files/directories lexicographically.
   *
   * @see Path.equals for more details
   */
  override operator fun compareTo(other: Path): Int

  /**
   * Compares two abstract files/directories lexicographically.
   *
   * @see toString
   */
  operator fun compareTo(other: String): Int

  /**
   * Tests this file/directory for equality with the given object.
   *
   * @see Path.equals for more details
   */
  override fun equals(other: Any?): Boolean

  /**
   * Computes a hash code for this file/directory.
   *
   * @see Path.hashCode for more details
   */
  override fun hashCode(): Int

  /**
   * Returns the string representation of this file/directory.
   *
   * @see Path.hashCode for more details
   */
  override fun toString(): String
}

/**
 * Get the path site based on the path string.
 *
 * @param first the path string or initial part of the path string
 * @param more additional strings to be joined to form the path string
 */
expect fun Path(first: String, vararg more: String): Path

/**
 * Convert [String] to [Path].
 */
inline fun String.asPath(vararg more: String): Path = Path(this, *more)