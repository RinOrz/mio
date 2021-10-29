@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

import com.meowool.mio.internal.IoFile
import com.meowool.mio.internal.NioPath
import com.meowool.mio.internal.DefaultDirectory
import com.meowool.mio.internal.DefaultDirectoryLegacy
import com.meowool.mio.internal.DefaultPath
import com.meowool.mio.internal.DefaultPathLegacy
import com.meowool.mio.internal.backport
import java.net.URI
import java.nio.file.Paths

/**
 * Returns the file based on the path char sequence.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
actual fun Directory(first: CharSequence, vararg more: CharSequence): Directory = backport(
  legacy = { Directory(IoFile(first, *more)) },
  modern = { Directory(NioPath(first, *more)) }
)

/**
 * Returns the directory based on the given [path].
 */
actual fun Directory(path: IPath): Directory = when(path) {
  is Directory -> path
  is DefaultPath -> Directory(path.nioPath)
  is DefaultPathLegacy -> Directory(path.ioFile)
  else -> Directory(path.toString())
}

/**
 * Get the file based on the uri.
 *
 * @param uri the URI to convert.
 */
fun Directory(uri: URI): Directory = backport(
  legacy = { Directory(IoFile(uri)) },
  modern = { Directory(Paths.get(uri)) }
)

/**
 * Get the MIO file based on the [NioPath].
 *
 * @param nioPath the NIO Directory to convert.
 */
fun Directory(nioPath: NioPath): Directory = DefaultDirectory(nioPath)

/**
 * Get the MIO file based on the [IoFile].
 *
 * @param ioFile the IO Directory to convert.
 */
fun Directory(ioFile: IoFile): Directory = DefaultDirectoryLegacy(ioFile)

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

/**
 * Convert all [NioPath] to [Directory]s.
 *
 * @see java.nio.file.Path.toMioDirectory
 */
fun Iterable<NioPath>.mapToMioDirectories(): List<Directory> = this.map(::Directory)

/**
 * Convert all [NioPath] to [Directory]s.
 *
 * @see java.nio.file.Path.toMioDirectory
 */
fun Sequence<NioPath>.mapToMioDirectories(): Sequence<Directory> = this.map(::Directory)

/**
 * Convert all [NioPath] to [Directory]s.
 *
 * @see java.nio.file.Path.toMioDirectory
 */
fun Array<NioPath>.mapToMioDirectories(): List<Directory> = this.map(::Directory)

/**
 * Convert all [IoFile] to [Directory]s.
 *
 * @see java.io.File.toMioDirectory
 */
@JvmName("mapIoFilesToDirectories")
fun Iterable<IoFile>.mapToMioDirectories(): List<Directory> = this.map(::Directory)

/**
 * Convert all [IoFile] to [Directory]s.
 *
 * @see java.io.File.toMioDirectory
 */
@JvmName("mapIoFilesToDirectories")
fun Sequence<IoFile>.mapToMioDirectories(): Sequence<Directory> = this.map(::Directory)

/**
 * Convert all [IoFile] to [Directory]s.
 *
 * @see java.io.File.toMioDirectory
 */
fun Array<IoFile>.mapToMioDirectories(): List<Directory> = this.map(::Directory)