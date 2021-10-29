@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio

/**
 * Represents the entry of the file kind in the zip archive.
 *
 * @see IZipEntry.isRegularFile
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface ZipFileEntry : IFile<ZipFileEntry>, IZipEntry<ZipFileEntry>

/**
 * Returns the zip file entry based on the given [entry].
 */
expect fun ZipFileEntry(entry: ZipEntry): ZipFileEntry

/**
 * Convert [ZipEntry] to [ZipFileEntry].
 */
inline fun ZipEntry.asFile(): ZipFileEntry = ZipFileEntry(this)

/**
 * Try to use the [ZipEntry] as a [ZipFileEntry], and return `null` if it already exists and is not
 * a zip file entry.
 */
fun ZipEntry?.asFileOrNull(): ZipFileEntry? = when {
  this == null -> null
  this is ZipFileEntry -> this
  this.exists().not() || this.isRegularFile -> ZipFileEntry(this)
  else -> null
}