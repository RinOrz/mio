@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio

/**
 * Represents the entry of the directory kind in the zip archive.
 *
 * @see IZipEntry.isDirectory
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface ZipDirectoryEntry : IDirectory<ZipDirectoryEntry>, IZipEntry<ZipDirectoryEntry>

/**
 * Returns the zip file entry based on the given [entry].
 */
expect fun ZipDirectoryEntry(entry: ZipEntry): ZipDirectoryEntry

/**
 * Convert [ZipEntry] to [ZipDirectoryEntry].
 */
inline fun ZipEntry.asDirectory(): ZipDirectoryEntry = ZipDirectoryEntry(this)

/**
 * Try to use the [ZipEntry] as a [ZipDirectoryEntry], and return `null` if it already exists and
 * is not a zip directory entry.
 */
fun ZipEntry?.asDirectoryOrNull(): ZipDirectoryEntry? = when {
  this == null -> null
  this is ZipDirectoryEntry -> this
  this.exists().not() || this.isDirectory -> ZipDirectoryEntry(this)
  else -> null
}

/**
 * Convert [ZipEntry] to [ZipDirectoryEntry].
 */
inline fun ZipEntry.asDir(): ZipDirectoryEntry = asDirectory()

/**
 * Try to use the [ZipEntry] as a [ZipDirectoryEntry], and return `null` if it already exists and
 * is not a zip directory entry.
 */
fun ZipEntry?.asDirOrNull(): ZipDirectoryEntry? = asDirectoryOrNull()