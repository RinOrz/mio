@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

import com.meowool.mio.internal.DefaultZipFileEntryNew
import com.meowool.mio.internal.backport

/**
 * Returns the zip file entry based on the given [entry].
 */
// TODO NEW API
actual fun ZipFileEntry(entry: ZipEntry): ZipFileEntry = when (entry) {
  is ZipFileEntry -> entry
  else -> backport(
    legacy = { TODO() },
    modern = { DefaultZipFileEntry(entry.toNioPath(), entry.holder) }
  )
}