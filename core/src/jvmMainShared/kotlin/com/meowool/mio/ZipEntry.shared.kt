@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

typealias JavaZipEntry = java.util.zip.ZipEntry

///**
// * Returns the zip file entry based on the given [entry].
// */
//// TODO NEW API
//actual fun ZipEntry(path: Path): ZipFileEntry = when (entry) {
//  is ZipFileEntry -> entry
//  else -> RealZipFileEntry(
//    entry.toNioPath(),
//    entry.holder,
//    entry.compressedSize,
//    entry.crc,
//    entry.extra,
//    entry.comment,
//    entry.compressionLevel
//  )
//}