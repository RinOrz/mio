@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

import com.meowool.mio.internal.IoFile
import com.meowool.mio.internal.NioPath
import com.meowool.mio.internal.DefaultPath
import com.meowool.mio.internal.DefaultPathLegacy
import com.meowool.mio.internal.DefaultZipNew
import com.meowool.mio.internal.JavaZipBackend
import com.meowool.mio.internal.backport
import com.meowool.sweekt.cast
import java.net.URI
import java.nio.file.Paths
import java.util.zip.ZipException
import java.util.zip.ZipFile

typealias JavaZip = java.util.zip.ZipFile

/**
 * Opens and returns the zip archive based on the path.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
actual fun Zip(first: CharSequence, vararg more: CharSequence): Zip = backport(
  legacy = { Zip(IoFile(first, *more)) },
  modern = { Zip(NioPath(first, *more)) }
)

/**
 * Opens and returns the zip archive based on the given [path].
 */
actual fun Zip(path: IPath): Zip = when(path) {
  is Zip -> path
  is DefaultPath -> Zip(path.nioPath)
  is DefaultPathLegacy -> Zip(path.ioFile)
  else -> Zip(path.toString())
}

/**
 * Try to use the [IPath] as a [Zip], and return `null` if it already exists and is not a zip file.
 */
actual fun IPath.asZipOrNull(): Zip? = when (this) {
  is Zip -> this
  else -> try {
    JavaZip(this.toString())
    Zip(this)
  } catch (e: ZipException) {
    null
  }
}

/**
 * Get the file based on the uri.
 *
 * @param uri the URI to convert.
 */
fun Zip(uri: URI): Zip = backport(
  legacy = { Zip(IoFile(uri)) },
  modern = { Zip(Paths.get(uri)) }
)

/**
 * Get the MIO file based on the [NioPath].
 *
 * @param nioPath the NIO Zip to convert.
 */
fun Zip(nioPath: NioPath): Zip = DefaultZipNew(nioPath)

/**
 * Get the MIO file based on the [IoFile].
 *
 * @param ioFile the IO Zip to convert.
 */
fun Zip(ioFile: IoFile): Zip = /*RealZipLegacy(ioFile)*/ TODO()

/**
 * Convert [IPath] to [JavaZip].
 */
fun IPath.toJavaZip(): JavaZip = runCatching { this.cast<JavaZipBackend>().javaZip }
  .getOrElse { ZipFile(this.toString()) }

/**
 * Convert [URI] to [Zip].
 */
inline fun URI.toMioZip(): Zip = Zip(this)

/**
 * Convert [NioPath] to [Zip].
 */
inline fun NioPath.toMioZip(): Zip = Zip(this)

/**
 * Convert [IoFile] to [Zip].
 */
inline fun IoFile.toMioZip(): Zip = Zip(this)

/**
 * Convert all [NioPath] to [Zip]s.
 *
 * @see java.nio.file.Path.toMioZip
 */
fun Iterable<NioPath>.mapToMioZips(): List<Zip> = this.map(::Zip)

/**
 * Convert all [NioPath] to [Zip]s.
 *
 * @see java.nio.file.Path.toMioZip
 */
fun Sequence<NioPath>.mapToMioZips(): Sequence<Zip> = this.map(::Zip)

/**
 * Convert all [NioPath] to [Zip]s.
 *
 * @see java.nio.file.Path.toMioZip
 */
fun Array<NioPath>.mapToMioZips(): List<Zip> = this.map(::Zip)

/**
 * Convert all [IoFile] to [Zip]s.
 *
 * @see java.io.File.toMioZip
 */
@JvmName("mapIoFilesToZips")
fun Iterable<IoFile>.mapToMioZips(): List<Zip> = this.map(::Zip)

/**
 * Convert all [IoFile] to [Zip]s.
 *
 * @see java.io.File.toMioZip
 */
@JvmName("mapIoFilesToZips")
fun Sequence<IoFile>.mapToMioZips(): Sequence<Zip> = this.map(::Zip)

/**
 * Convert all [IoFile] to [Zip]s.
 *
 * @see java.io.File.toMioZip
 */
fun Array<IoFile>.mapToMioZips(): List<Zip> = this.map(::Zip)