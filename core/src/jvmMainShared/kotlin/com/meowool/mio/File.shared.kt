@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

import com.meowool.mio.internal.IoFile
import com.meowool.mio.internal.NioPath
import com.meowool.mio.internal.DefaultFile
import com.meowool.mio.internal.DefaultFileLegacy
import com.meowool.mio.internal.DefaultPath
import com.meowool.mio.internal.DefaultPathLegacy
import com.meowool.mio.internal.backport
import java.net.URI
import java.nio.file.Paths

typealias IoFile = java.io.File

/**
 * Returns the file based on the path char sequence.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
actual fun File(first: CharSequence, vararg more: CharSequence): File = backport(
  legacy = { File(IoFile(first, *more)) },
  modern = { File(NioPath(first, *more)) }
)

/**
 * Returns the file based on the given [path].
 */
actual fun File(path: Path): File = when(path) {
  is File -> path
  is DefaultPath -> File(path.nioPath)
  is DefaultPathLegacy -> File(path.ioFile)
  else -> File(path.toString())
}

/**
 * Get the file based on the uri.
 *
 * @param uri the URI to convert.
 */
fun File(uri: URI): File = backport(
  legacy = { File(IoFile(uri)) },
  modern = { File(Paths.get(uri)) }
)

/**
 * Get the MIO file based on the [NioPath].
 *
 * @param nioPath the NIO File to convert.
 */
fun File(nioPath: NioPath): File = DefaultFile(nioPath)

/**
 * Get the MIO file based on the [IoFile].
 *
 * @param ioFile the IO File to convert.
 */
fun File(ioFile: IoFile): File = DefaultFileLegacy(ioFile)

/**
 * Convert [URI] to [File].
 */
inline fun URI.toMioFile(): File = File(this)

/**
 * Convert [NioPath] to [File].
 */
inline fun NioPath.toMioFile(): File = File(this)

/**
 * Convert [IoFile] to [File].
 */
inline fun IoFile.toMioFile(): File = File(this)

/**
 * Convert all [NioPath] to [File]s.
 *
 * @see java.nio.file.Path.toMioFile
 */
fun Iterable<NioPath>.mapToMioFiles(): List<File> = this.map(::File)

/**
 * Convert all [NioPath] to [File]s.
 *
 * @see java.nio.file.Path.toMioFile
 */
fun Sequence<NioPath>.mapToMioFiles(): Sequence<File> = this.map(::File)

/**
 * Convert all [NioPath] to [File]s.
 *
 * @see java.nio.file.Path.toMioFile
 */
fun Array<NioPath>.mapToMioFiles(): List<File> = this.map(::File)

/**
 * Convert all [IoFile] to [File]s.
 *
 * @see java.io.File.toMioFile
 */
@JvmName("mapIoFilesToFiles")
fun Iterable<IoFile>.mapToMioFiles(): List<File> = this.map(::File)

/**
 * Convert all [IoFile] to [File]s.
 *
 * @see java.io.File.toMioFile
 */
@JvmName("mapIoFilesToFiles")
fun Sequence<IoFile>.mapToMioFiles(): Sequence<File> = this.map(::File)

/**
 * Convert all [IoFile] to [File]s.
 *
 * @see java.io.File.toMioFile
 */
fun Array<IoFile>.mapToMioFiles(): List<File> = this.map(::File)