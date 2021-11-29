@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.io

import com.meowool.io.internal.NioPath
import com.meowool.io.internal.PathImpl
import com.meowool.sweekt.castOrNull
import com.meowool.sweekt.runOrNull
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
actual fun Path(first: CharSequence, vararg more: CharSequence): Path =
  Path(NioPath(first, *more))

/**
 * Get the path based on the uri.
 *
 * @param uri the URI to convert.
 */
fun Path(uri: URI): Path = Path(Paths.get(uri))

/**
 * Get the Mio path based on the [NioPath].
 *
 * @param nioPath the NIO Path to convert.
 */
fun Path(nioPath: NioPath): Path = when {
  Files.isDirectory(nioPath) -> Directory(nioPath)
  Files.isRegularFile(nioPath) -> File(nioPath)
  // May not have been created yet
  else -> PathImpl(nioPath)
}

/**
 * Get the Mio path based on the [IoFile].
 *
 * @param ioFile the IO File to convert.
 */
fun Path(ioFile: IoFile): Path = Path(ioFile.toPath())

/**
 * Convert [Path] to [URI].
 */
fun Path.toURI(): URI = this.toNioPath().toUri()

/**
 * Convert [Path] to [IoFile].
 */
fun Path.toIoFile(): IoFile = runOrNull { toNioPath().toFile() } ?: IoFile(this.toString())

/**
 * Convert [Path] to [NioPath].
 */
fun Path.toNioPath(): NioPath = this.castOrNull<PathImpl>()?.nioPath ?: Paths.get(this.toString())

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