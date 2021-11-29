@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.io

import com.meowool.io.internal.FileImpl
import com.meowool.io.internal.NioPath
import com.meowool.io.internal.PathImpl
import java.net.URI
import java.nio.file.Paths

typealias IoFile = java.io.File

/**
 * Returns the file based on the path char sequence.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
actual fun File(first: CharSequence, vararg more: CharSequence): File = File(NioPath(first, *more))

/**
 * Returns the file based on the given [path].
 */
actual fun File(path: Path): File = when(path) {
  is File -> path
  is PathImpl -> File(path.nioPath)
  else -> File(path.toNioPath())
}

/**
 * Get the file based on the uri.
 *
 * @param uri the URI to convert.
 */
fun File(uri: URI): File = File(Paths.get(uri))

/**
 * Get the MIO file based on the [NioPath].
 *
 * @param nioPath the NIO File to convert.
 */
fun File(nioPath: NioPath): File = FileImpl(nioPath)

/**
 * Get the MIO file based on the [IoFile].
 *
 * @param ioFile the IO File to convert.
 */
fun File(ioFile: IoFile): File = File(ioFile.toPath())

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