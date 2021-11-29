@file:Suppress("FunctionName", "SpellCheckingInspection", "NOTHING_TO_INLINE", "NO_ACTUAL_FOR_EXPECT")

package com.meowool.io

import kotlin.jvm.JvmName

/**
 * An object representing the directory in the file system and its path.
 *
 * @param Actual represents the actual return type of members (such as directory, zip, etc...).
 * @author 凛 (https://github.com/RinOrz)
 */
interface BaseDirectory<
  SubPath : Path,
  SubFile : File,
  SubDirectory : BaseDirectory<SubPath, SubFile, SubDirectory, Actual>,
  Actual : BaseDirectory<SubPath, SubFile, SubDirectory, Actual>> :
  BasePathList<SubPath, SubFile, SubDirectory, Actual> {

  /**
   * Returns the sum of the sizes of all files in this directory (in bytes).
   *
   * If the directory is large, the calculations can be time-consuming, it is recommended to call
   * it in a background thread.
   *
   * @see BasePath.size
   */
  val totalSize: Long

  /**
   * Creates this directory. If this directory already exists, nothing will happen.
   *
   * Note that if some parent directories on this path does not exist, they will be invoked
   * [createParentDirectories] first.
   *
   * @return this directory has been created
   *
   * @throws PathAlreadyExistsException if this directory already exists.
   * @throws PathExistsAndIsNotDirectoryException if this existing path is a file instead of a
   *   directory, this directory creation fails.
   */
  fun create(): Actual

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
   */
  suspend fun create(overwrite: Boolean): Actual

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
 * @see BaseDirectory
 * @see Zip
 * @see ZipDirectoryEntry
 */
typealias Directory = BaseDirectory<*, *, *, *>

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
 * Converts [CharSequence] to [Directory].
 *
 * @param more additional char sequence to be joined to form the path
 */
inline fun CharSequence.asDirectory(vararg more: CharSequence): Directory = Directory(this, *more)

/**
 * Converts [CharSequence] to [Directory].
 *
 * @param more additional char sequence to be joined to form the path
 */
@JvmName("nullableAsDirectory")
inline fun CharSequence?.asDirectory(vararg more: CharSequence): Directory? =
  this?.asDirectory(*more)

/**
 * Converts [CharSequence] to [Directory].
 *
 * @param more additional char sequence to be joined to form the path
 */
inline fun CharSequence.asDir(vararg more: CharSequence): Directory = Directory(this, *more)

/**
 * Converts [CharSequence] to [Directory].
 *
 * @param more additional char sequence to be joined to form the path
 */
@JvmName("nullableAsDir")
inline fun CharSequence?.asDir(vararg more: CharSequence): Directory? =
  this?.asDirectory(*more)

/**
 * Converts [Path] to [Directory].
 */
inline fun Path.asDirectory(): Directory = Directory(this)

/**
 * Convert [Path] to [Directory].
 */
inline fun Path.asDir(): Directory = Directory(this)

/**
 * Tries to convert [Path] to [Directory], returns `null` if it already exists and is not
 * a directory.
 */
fun Path?.asDirectoryOrNull(): Directory? = when {
  this == null -> null
  this is Directory -> this
  this.exists().not() || this.isDirectory -> Directory(this)
  else -> null
}

/**
 * Tries to convert [Path] to [Directory], returns `null` if it already exists and is not
 * a directory.
 */
inline fun Path?.asDirOrNull(): Directory? = this?.asDirectoryOrNull()