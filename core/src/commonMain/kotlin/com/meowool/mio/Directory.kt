@file:Suppress(
  "FunctionName", "SpellCheckingInspection", "NOTHING_TO_INLINE", "NO_ACTUAL_FOR_EXPECT"
)

package com.meowool.mio

/**
 * An object representing the directory in the file system and its path.
 *
 * @param Self represents the return type of members (directory, zip, etc...)
 * @author 凛 (https://github.com/RinOrz)
 */
interface IDirectory<Self : IDirectory<Self>> : IPathGroup<Self, Path, File, Directory> {

  /**
   * Returns the sum of the sizes of all files in this directory (in bytes).
   *
   * If the directory is large, the calculations can be time-consuming, it is recommended to call
   * it in a background thread.
   *
   * @see IPath.size
   */
  val totalSize: Long

  /**
   * Creates this directory. If this directory already exists and the argument [overwrite] is set
   * to `true`, an empty directory will be created to overwrite
   * it (regardless of whether there is a file in the existing directory), but if the
   * argument [overwrite] is set to `false`, nothing will happen.
   *
   * Note that if some parent directories on this path does not exist, they will be invoked
   * [createParentDirectories] first.
   *
   * @return this directory has been created
   *
   * @throws PathExistsAndIsNotDirectoryException if this existing path is a file instead of a
   *   directory, this directory creation fails.
   *
   * @see createStrictly
   */
  fun create(overwrite: Boolean = false): Self

  /**
   * Creates this directory. If this directory already exists and the argument [overwrite] is set
   * to `true`, an empty directory will be created to overwrite
   * it (if the original directory has sub-files, an [DirectoryNotEmptyException] will be thrown),
   * but if the argument [overwrite] is set to `false`, will be throws
   * an [PathAlreadyExistsException].
   *
   * @return this directory has been created
   *
   * @throws DirectoryNotEmptyException if the directory is not empty, even set the value
   *   [overwrite] to `true` will not help, in this case should invoke [clear] first to clear
   *   the directory.
   * @throws ParentDirectoryNotExistsException in the case of strict creation, if the parent
   *   directories does not exist, this directory creation fails.
   * @throws PathExistsAndIsNotDirectoryException if this existing path is a file instead of a
   *   directory, this directory creation fails.
   *
   * @see create
   */
  @Throws(
    DirectoryNotEmptyException::class,
    PathAlreadyExistsException::class,
    ParentDirectoryNotExistsException::class,
    PathExistsAndIsNotDirectoryException::class,
  )
  fun createStrictly(overwrite: Boolean = false): Self

  /**
   * Adds the temporary file to this directory, using the given [prefix] and [suffix] to
   * generate its name.
   *
   * @return the path of the added temporary file
   */
  fun addTempFile(prefix: String? = null, suffix: String? = null): File
}

/**
 * An object representing the any type of directory in the file system and its path.
 *
 * @see IDirectory
 * @see Zip
 * @see ZipDirectoryEntry
 */
typealias Directory = IDirectory<*>

/**
 * Returns the directory based on the path.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
expect fun Directory(first: CharSequence, vararg more: CharSequence): Directory

/**
 * Returns the directory based on the given [path].
 */
expect fun Directory(path: Path): Directory

/**
 * Returns the directory based on the path.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
inline fun Dir(first: CharSequence, vararg more: CharSequence): Directory = Directory(first, *more)

/**
 * Returns the directory based on the given [path].
 */
inline fun Dir(path: Path): Directory = path.asDirectory()

/**
 * Convert [CharSequence] to [Directory].
 *
 * @param more additional char sequence to be joined to form the path
 */
inline fun CharSequence.asDirectory(vararg more: CharSequence): Directory = Directory(this, *more)

/**
 * Convert [IPath] to [Directory].
 */
inline fun Path.asDirectory(): Directory = Directory(this)

/**
 * Try to use the [IPath] as a [Directory], and return `null` if it already exists and is not
 * a directory.
 */
fun Path?.asDirectoryOrNull(): Directory? = when {
  this == null -> null
  this is Directory -> this
  this.exists().not() || this.isDirectory -> Directory(this)
  else -> null
}

/**
 * Convert [CharSequence] to [Directory].
 *
 * @param more additional char sequence to be joined to form the path
 */
inline fun CharSequence.asDir(vararg more: CharSequence): Directory = Directory(this, *more)

/**
 * Convert [IPath] to [Directory].
 */
inline fun Path.asDir(): Directory = Directory(this)

/**
 * Try to use the [IPath] as a [Directory], and return `null` if it already exists and is not
 * a directory.
 */
inline fun Path?.asDirOrNull(): Directory? = this?.asDirectoryOrNull()