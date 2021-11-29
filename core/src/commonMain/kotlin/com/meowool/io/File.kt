@file:Suppress("FunctionName", "NOTHING_TO_INLINE", "NO_ACTUAL_FOR_EXPECT")

package com.meowool.io

import com.meowool.io.channel.DataChannel
import com.meowool.io.channel.FileChannel
import com.meowool.io.channel.use
import com.meowool.sweekt.SuspendGetter
import com.meowool.sweekt.SuspendSetter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlin.jvm.JvmName

/**
 * An object representing the file in the file system and its path.
 *
 * @param Actual represents the actual return type of members (such as zip, zip file-entry, etc...).
 * @author 凛 (https://github.com/RinOrz)
 */
interface BaseFile<Actual: BaseFile<Actual>> : BasePath<Actual> {

  /**
   * The extension of this file (not including the dot). If there is only one dot, and it is
   * first in the name, the extension will be empty.
   *
   * For example:
   * ```
   * File("foo.txt").extension           -> "txt"
   * File(".aaa.jpg").extension          -> "jpg"
   * File(".xyz").extension              -> ""
   *
   * File("foo.txt").extension = "zip"   -> "foo.zip"
   * File(".aaa.jpg").extension = "png"  -> ".aaa.zip"
   * File(".xyz").extension = "txt"      -> ".xyz.txt"
   * ```
   */
  var extension: String

  /**
   * The extension of this file (including the dot). If there is only one dot, and it is first in
   * the name, the extension will be empty.
   *
   * For example,
   * ```
   * File("foo.txt").extensionWithDot   -> ".txt"
   * File(".aaa.jpg").extensionWithDot  -> ".jpg"
   * File(".xyz").extensionWithDot      -> ""
   *
   * File("foo.txt").extension = ".zip"   -> "foo.zip"
   * File(".aaa.jpg").extension = ".png"  -> ".aaa.zip"
   * File(".xyz").extension = ".txt"      -> ".xyz.txt"
   * ```
   *
   * @see extension
   */
  var extensionWithDot: String

  /**
   * The file's name without an extension. If there is only one dot, and it is first in the
   * [name], it will be all of [name].
   *
   * For example getting,
   * ```
   * File("foo.txt").nameWithoutExtension           -> "foo"
   * File(".aaa.jpg").nameWithoutExtension          -> ".aaa"
   * File(".xyz").nameWithoutExtension              -> ".xyz"
   *
   * File("foo.txt").nameWithoutExtension = "bar"   -> "bar.txt"
   * File(".aaa.jpg").nameWithoutExtension = "bbb"  -> ".bbb.zip"
   * File(".xyz").nameWithoutExtension = "ccc"      -> ".ccc"
   * ```
   */
  var nameWithoutExtension: String

  /**
   * Reads or changes all bytes in this file. If changes the bytes of an existing file, will
   * overwrite it. Otherwise, will create an empty file to write bytes.
   *
   * Note that this property applies to the simple reads or writes to the entire file, but it does
   * not apply to a file greater than about 2 GB. To read or write large file, please [open] the
   * file operation channel or [append], [lines] or other properties or functions instead.
   *
   * @see text
   * @see write
   */
  @SuspendGetter
  @SuspendSetter
  var bytes: ByteArray

  /**
   * Opens the operation channel for this file to read or write contents.
   *
   * Please remember to call [FileChannel.close] timely to close the channel after operate the
   * file, or use the [open] function with the code block to operate the file, it will
   * automatically close the channel after the code block is completed.
   */
  fun open(): FileChannel

  /**
   * Opens the operation channel of this file and provide a [block] to read or write the contents,
   * the channel will be automatically closed after the [block] is completed.
   */
  suspend fun <R> open(block: suspend FileChannel.() -> R): R = open().use(block)

  /**
   * Creates this file. If this file already exists and the argument [overwrite] is set to `true`,
   * an empty file will be created to overwrite it, but if the argument [overwrite] is set to
   * `false`, nothing will happen.
   *
   * Note that if some parent directories on this path does not exist, they will be invoked
   * [createParentDirectories] first. and if the path of this file already exists and is a
   * directory, throws an [PathExistsAndIsNotFileException].
   *
   * @return this file has been created
   */
  @Throws(PathExistsAndIsNotFileException::class)
  fun create(overwrite: Boolean = false): Actual

  /**
   * Replaces all data of this file with the given [file].
   *
   * In fact, the default implementation of this function is equivalent to the effect of the
   * following expression:
   * ```
   * file.copyTo(
   *   target = this,
   *   overwrite = true,
   *   followLinks = true,
   * )
   * ```
   *
   * @param keepSources if the value is `true`, the given [file] will be keeping after the
   *   replacement is completed, otherwise its will be deleted.
   * @param followLinks if the given [file] is a symbolic link and the value is `true`, then copies
   *   its link target, otherwise copies the symbolic link itself.
   *
   * @return this file replaced
   */
  suspend fun replaceWith(
    file: Actual,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
  ): Actual

  /**
   * Copies this file to the given [target] file.
   *
   * @param overwrite whether to overwrite the [target] file with this file when it exists,
   *   otherwise skip this function.
   * @param followLinks if this file is a symbolic link and the value is `true`, then copies the
   *   link target, otherwise copies the symbolic link itself.
   *
   * @throws PathAlreadyExistsException if the target file already exists and [overwrite] argument
   *   is set to `false`.
   *
   * @return the file of target path that has been copied
   */
  @Throws(PathAlreadyExistsException::class)
  suspend fun <R: File> copyTo(
    target: R,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
  ): R

  /**
   * Copies this file into the given [target] package (directory, archive file, etc...).
   *
   * In fact, this function is equivalent to the following expression:
   * ```
   * val sourceFile = File("/foo/bar.txt")
   *
   * // Copies into directory
   * val targetDir = Directory("/gav/baz")
   * val copied = sourceFile.copyTo(targetDir.joinFile(sourceFile.name))
   * println(copied) // "/gav/baz/bar.txt"
   *
   * // Copies into zip archive file
   * val targetZip = Directory("/foo.zip")
   * val copied = sourceFile.copyTo(targetZip.joinFile(sourceFile.name))
   * println(copied) // "bar.txt" in `foo.zip`.
   * ```
   *
   * @param overwrite whether to overwrite the [target] file with this file when it exists,
   *   otherwise skip this function.
   * @param followLinks if this file is a symbolic link and the value is `true`, then copies the
   *   link target, otherwise copies the symbolic link itself.
   *
   * @return the file of target path that has been copied
   *
   * @see BaseFile.copyTo
   */
  suspend fun copyInto(
    target: PathList,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
  ): File

  /**
   * Moves this file to the given [target] file.
   *
   * @param overwrite whether to overwrite the [target] file with this file when it exists,
   *   otherwise skip this function.
   * @param followLinks if this file is a symbolic link and the value is `true`, then moves the
   *   link target, otherwise moves the symbolic link itself.
   *
   * @throws PathAlreadyExistsException if the target file already exists and [overwrite] argument
   *   is set to `false`.
   *
   * @return the file of target path that has been moved
   */
  @Throws(PathAlreadyExistsException::class)
  suspend fun <R: File> moveTo(
    target: R,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
  ): R

  /**
   * Moves this file into the given [target] package (directory, archive file, etc...).
   *
   * In fact, this function is equivalent to the following expression:
   * ```
   * val sourceFile = File("/foo/bar.txt")
   *
   * // Moves into directory
   * val targetDir = Directory("/gav/baz")
   * val moved = sourceFile.moveTo(targetDir.joinFile(sourceFile.name))
   * println(moved) // "/gav/baz/bar.txt"
   *
   * // Moves into zip archive file
   * val targetZip = Directory("/foo.zip")
   * val moved = sourceFile.moveTo(targetZip.joinFile(sourceFile.name))
   * println(moved) // "bar.txt" in `foo.zip`.
   * ```
   *
   * @param overwrite whether to overwrite the [target] file with this file when it exists,
   *   otherwise skip this function.
   * @param followLinks if this file is a symbolic link and the value is `true`, then moves the
   *   link target, otherwise moves the symbolic link itself.
   *
   * @return the file of target path that has been moved
   *
   * @see BaseFile.moveTo
   */
  suspend fun moveInto(
    target: PathList,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
  ): File

  /**
   * Deletes this file safely. Skip if this file does not exist. If this file is a symbolic link,
   * delete the symbolic link itself not the final target of the link, if you want to change this
   * behavior, set [followLinks] to `true`, this will remove the target of the link.
   *
   * @param followLinks if this file is a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   *
   * @return if the deletion fails, it returns `false`.
   */
  fun delete(followLinks: Boolean = false): Boolean

  /**
   * Opens the file channel, reads all bytes as text, and then close the channel.
   *
   * Note that this property applies to the simple reads to the entire file, but it does not apply
   * to a file greater than about 2 GB. To read large file, please [open] the file operation
   * channel instead.
   *
   * @param charset character set to use for reading text of file.
   * @see bytes
   */
  suspend fun text(charset: Charset = Charsets.UTF_8): String

  /**
   * Opens the file channel, reads all lines of this file, and then close the channel.
   *
   * The 'cold' flow can be read on demand, and the line in the file will be read only when it is
   * collected likes [Flow.collect].
   *
   * @param charset character set to use for reading line of file.
   */
  fun lines(charset: Charset = Charsets.UTF_8): Flow<String>

  /**
   * Opens the file channel, reads all lines of this file, and then close the channel.
   *
   * @param charset character set to use for reading line of file.
   */
  suspend fun lineList(charset: Charset = Charsets.UTF_8): List<String> = lines(charset).toList()

  /**
   * If the file already exists, opens the file channel and appends the given [bytes] to the end of
   * this file, and then close the channel. Otherwise, creates a new file and written.
   *
   * It is not recommended calling this function frequently, because the channel will be opened
   * and closed every time append content. If you want to append content frequently, please
   * consider [open] the file channel and call [FileChannel.close] to close the channel after
   * everything is done.
   *
   * @return the path of this file
   */
  suspend fun append(bytes: ByteArray): Actual

  /**
   * If the file already exists, opens the file channel and appends all data from the given
   * [channel] to the end of this file, and then close the channel. Otherwise, creates a new file
   * and written.
   *
   * It is not recommended calling this function frequently, because the channel will be opened
   * and closed every time append content. If you want to append content frequently, please
   * consider [open] the file channel and call [FileChannel.close] to close the channel after
   * everything is done.
   *
   * @return the path of this file
   */
  suspend fun append(channel: DataChannel): Actual

  /**
   * If the file already exists, opens the file channel and appends the given [text] to the end of
   * this file, and then close the channel. Otherwise, creates a new file and written.
   *
   * It is not recommended calling this function frequently, because the channel will be opened
   * and closed every time append content. If you want to append content frequently, please
   * consider [open] the file channel and call [FileChannel.close] to close the channel after
   * everything is done.
   *
   * @param charset character set to use for writing text.
   *
   * @return the path of this file
   */
  suspend fun append(text: CharSequence, charset: Charset = Charsets.UTF_8): Actual

  /**
   * If the file already exists, opens the file channel and appends the given [lines] to the end of
   * this file, and then close the channel. Otherwise, creates a new file and written.
   *
   * It is not recommended calling this function frequently, because the channel will be opened
   * and closed every time append content. If you want to append content frequently, please
   * consider [open] the file channel and call [FileChannel.close] to close the channel after
   * everything is done.
   *
   * @param charset character set to use for writing text.
   *
   * @return the path of this file
   */
  suspend fun append(lines: Iterable<CharSequence>, charset: Charset = Charsets.UTF_8): Actual

  /**
   * If the file already exists, opens the file channel and appends the given [lines] to the end of
   * this file, and then close the channel. Otherwise, creates a new file and written.
   *
   * It is not recommended calling this function frequently, because the channel will be opened
   * and closed every time append content. If you want to append content frequently, please
   * consider [open] the file channel and call [FileChannel.close] to close the channel after
   * everything is done.
   *
   * @param charset character set to use for writing text.
   *
   * @return the path of this file
   */
  suspend fun append(lines: Sequence<CharSequence>, charset: Charset = Charsets.UTF_8): Actual

  /**
   * If the file already exists, opens the file channel and writes the given [bytes] to this file,
   * and then close the channel. Otherwise, creates a new file and written.
   *
   * @return the path of this file
   */
  suspend fun write(bytes: ByteArray): Actual

  /**
   * If the file already exists, open the file channel and writes all data from the given [channel]
   * to this file, and then close the channel. Otherwise, creates a new file and written.
   *
   * @return the path of this file
   */
  suspend fun write(channel: DataChannel): Actual

  /**
   * If the file already exists, opens the file channel and writes the given [text] to this file,
   * and then close the channel. Otherwise, creates a new file and written.
   *
   * @return the path of this file
   * @param charset character set to use for writing text.
   */
  suspend fun write(text: CharSequence, charset: Charset = Charsets.UTF_8): Actual

  /**
   * If the file already exists, opens the file channel and writes the given [lines] to this file,
   * and then close the channel. Otherwise, creates a new file and written.
   *
   * @return the path of this file
   * @param charset character set to use for writing text.
   */
  suspend fun write(lines: Iterable<CharSequence>, charset: Charset = Charsets.UTF_8): Actual

  /**
   * If the file already exists, opens the file channel and writes the given [lines] to this file,
   * and then close the channel. Otherwise, creates a new file and written.
   *
   * @return the path of this file
   * @param charset character set to use for writing text.
   */
  suspend fun write(lines: Sequence<CharSequence>, charset: Charset = Charsets.UTF_8): Actual
}

/**
 * An object representing the any type of file in the file system and its path.
 *
 * @see BaseFile
 * @see Zip
 * @see ZipFileEntry
 */
typealias File = BaseFile<*>

/**
 * Returns the file based on the path.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
expect fun File(first: CharSequence, vararg more: CharSequence): File

/**
 * Returns the file based on the given [path].
 */
expect fun File(path: Path): File

/**
 * Convert [CharSequence] to [File].
 *
 * @param more additional char sequence to be joined to form the path
 */
inline fun CharSequence.asFile(vararg more: CharSequence): File = File(this, *more)

/**
 * Convert [CharSequence] to [File].
 *
 * @param more additional char sequence to be joined to form the path
 */
@JvmName("nullableAsFile")
inline fun CharSequence?.asFile(vararg more: CharSequence): File? = this?.asFile(*more)

/**
 * Convert [Path] to [File].
 */
inline fun Path.asFile(): File = File(this)

/**
 * Tries to convert [Path] to [File], returns `null` if it already exists and is not a file.
 */
fun Path?.asFileOrNull(): File? = when {
  this == null -> this
  this is File -> this
  this.exists().not() || this.isRegularFile -> File(this)
  else -> null
}