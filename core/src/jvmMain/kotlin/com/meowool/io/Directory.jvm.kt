@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.io

import com.meowool.io.internal.DirectoryImpl
import com.meowool.io.internal.NioPath
import com.meowool.io.internal.PathImpl
import java.net.URI
import java.nio.file.Paths

/**
 * Returns the file based on the path char sequence.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
actual fun Directory(first: CharSequence, vararg more: CharSequence): Directory =
  Directory(NioPath(first, *more))

/**
 * Returns the directory based on the given [path].
 */
actual fun Directory(path: Path): Directory = when(path) {
  is Directory -> path
  is PathImpl -> Directory(path.nioPath)
  else -> Directory(path.toNioPath())
}

/**
 * Get the file based on the uri.
 *
 * @param uri the URI to convert.
 */
fun Directory(uri: URI): Directory = Directory(Paths.get(uri))

/**
 * Get the MIO file based on the [NioPath].
 *
 * @param nioPath the NIO Directory to convert.
 */
fun Directory(nioPath: NioPath): Directory = DirectoryImpl(nioPath)

/**
 * Get the MIO file based on the [IoFile].
 *
 * @param ioFile the IO Directory to convert.
 */
fun Directory(ioFile: IoFile): Directory = Directory(ioFile.toPath())

/**
 * Convert [URI] to [Directory].
 */
inline fun URI.toMioDirectory(): Directory = Directory(this)

/**
 * Convert [NioPath] to [Directory].
 */
inline fun NioPath.toMioDirectory(): Directory = Directory(this)

/**
 * Convert [IoFile] to [Directory].
 */
inline fun IoFile.toMioDirectory(): Directory = Directory(this)