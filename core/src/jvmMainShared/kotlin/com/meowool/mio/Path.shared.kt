@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

import com.meowool.mio.internal.IoFile
import com.meowool.mio.internal.NioPath
import com.meowool.mio.internal.DefaultPath
import com.meowool.mio.internal.DefaultPathLegacy
import com.meowool.mio.internal.IoFileBackend
import com.meowool.mio.internal.NioPathBackend
import com.meowool.mio.internal.backport
import com.meowool.sweekt.cast
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

typealias NioPath = java.nio.file.Path

/**
 * Returns the path based on the path char sequence.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
actual fun Path(first: CharSequence, vararg more: CharSequence): Path = backport(
  legacy = { Path(IoFile(first, *more)) },
  modern = { Path(NioPath(first, *more)) }
)

/**
 * Get the path based on the uri.
 *
 * @param uri the URI to convert.
 */
fun Path(uri: URI): Path = backport(
  legacy = { Path(IoFile(uri)) },
  modern = { Path(Paths.get(uri)) }
)

/**
 * Get the MIO path based on the [NioPath].
 *
 * @param nioPath the NIO Path to convert.
 */
fun Path(nioPath: NioPath): Path = when {
  Files.isDirectory(nioPath) -> Directory(nioPath)
  Files.isRegularFile(nioPath) -> File(nioPath)
  // May not have been created yet
  else -> DefaultPath(nioPath)
}

/**
 * Get the MIO path based on the [IoFile].
 *
 * @param ioFile the IO File to convert.
 */
fun Path(ioFile: IoFile): Path = when {
  ioFile.isDirectory -> Directory(ioFile)
  ioFile.isFile -> File(ioFile)
  // May not have been created yet
  else -> DefaultPathLegacy(ioFile)
}

/**
 * Convert [Path] to [URI].
 */
fun Path.toURI(): URI = backport(
  legacy = { this.toIoFile().toURI() },
  modern = { this.toNioPath().toUri() }
)

/**
 * Convert [Path] to [IoFile].
 */
fun Path.toIoFile(): IoFile = runCatching {
  backport(
    legacy = { this.cast<IoFileBackend>().ioFile },
    modern = { this.toNioPath().toFile() }
  )
}.getOrElse { IoFile(this.toString()) }

/**
 * Convert [Path] to [NioPath].
 */
fun Path.toNioPath(): NioPath = runCatching {
  backport(
    legacy = { this.toIoFile().toPath() },
    modern = { this.cast<NioPathBackend>().nioPath }
  )
}.getOrElse { Paths.get(this.toString()) }

/**
 * Convert [URI] to [Path].
 */
inline fun URI.toMioPath(): Path = Path(this)

/**
 * Convert [NioPath] to [Path].
 */
inline fun NioPath.toMioPath(): Path = Path(this)

/**
 * Convert [IoFile] to [Path].
 */
inline fun IoFile.toMioPath(): Path = Path(this)

/**
 * Convert all [NioPath] to [Path]s.
 *
 * @see java.nio.file.Path.toMioPath
 */
fun Iterable<NioPath>.mapToMioPaths(): List<Path> = this.map(::Path)

/**
 * Convert all [NioPath] to [Path]s.
 *
 * @see java.nio.file.Path.toMioPath
 */
fun Sequence<NioPath>.mapToMioPaths(): Sequence<Path> = this.map(::Path)

/**
 * Convert all [NioPath] to [Path]s.
 *
 * @see java.nio.file.Path.toMioPath
 */
fun Array<NioPath>.mapToMioPaths(): List<Path> = this.map(::Path)

/**
 * Convert all [IoFile] to [Path]s.
 *
 * @see java.io.File.toMioPath
 */
@JvmName("mapIoFilesToPaths")
fun Iterable<IoFile>.mapToMioPaths(): List<Path> = this.map(::Path)

/**
 * Convert all [IoFile] to [Path]s.
 *
 * @see java.io.File.toMioPath
 */
@JvmName("mapIoFilesToPaths")
fun Sequence<IoFile>.mapToMioPaths(): Sequence<Path> = this.map(::Path)

/**
 * Convert all [IoFile] to [Path]s.
 *
 * @see java.io.File.toMioPath
 */
fun Array<IoFile>.mapToMioPaths(): List<Path> = this.map(::Path)