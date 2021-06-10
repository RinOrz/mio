@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

import com.meowool.mio.internal.FilePath
import com.meowool.mio.internal.RealPath
import com.meowool.sweekt.cast
import com.meowool.sweekt.safeCast
import java.io.File
import java.net.URI
import java.nio.file.Paths

typealias NioPath = java.nio.file.Path

/**
 * Get the path site based on the path string.
 *
 * @param first the path string or initial part of the path string
 * @param more additional strings to be joined to form the path string
 */
actual fun Path(first: String, vararg more: String): Path = runCatching {
  RealPath(Paths.get(first, *more))
}.getOrElse {
  FilePath(if (more.isEmpty()) File(first) else File(first, more.joinToString(SystemSeparator)))
}

/**
 * Get the path site based on the path string.
 *
 * @param uri the URI to convert.
 *
 * @see Paths.get
 */
fun Path(uri: URI): Path = runCatching {
  Paths.get(uri).toMioPath()
}.getOrElse {
  File(uri).toMioPath()
}

/**
 * Convert [Path] to [URI].
 *
 * @see Path
 */
fun Path.toURI(): URI = runCatching {
  this.toNioPath().toUri()
}.getOrElse {
  this.toFile().toURI()
}

/**
 * Convert [Path] to [File].
 *
 * @see Path
 */
fun Path.toFile(): File = this.safeCast<FilePath>()?.file
  ?: runCatching { this.cast<RealPath>().path.toFile() }.getOrElse { File(this.toString()) }

/**
 * Convert [Path] to [NioPath].
 *
 * @see Path
 */
fun Path.toNioPath(): NioPath = this.safeCast<RealPath>()?.path ?: Paths.get(this.toString())

/**
 * Convert [URI] to [Path].
 */
inline fun URI.toPath(): Path = Path(this)

/**
 * Convert [NioPath] to [Path].
 */
inline fun NioPath.toMioPath(): Path = RealPath(this)

/**
 * Convert [File] to [Path].
 */
fun File.toMioPath(): Path = runCatching {
  toPath().toMioPath()
}.getOrElse {
  FilePath(this)
}

/**
 * Convert all [NioPath] to [Path]s.
 *
 * @see java.nio.file.Path.toMioPath
 */
fun Iterable<NioPath>.mapToPaths(): List<Path> = map { it.toMioPath() }
fun Sequence<NioPath>.mapToPaths(): Sequence<Path> = map { it.toMioPath() }
fun Array<NioPath>.mapToPaths(): List<Path> = map { it.toMioPath() }