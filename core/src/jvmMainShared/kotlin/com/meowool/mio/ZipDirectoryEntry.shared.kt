@file:Suppress("NOTHING_TO_INLINE", "NewApi")

package com.meowool.mio

import com.meowool.mio.internal.DefaultZipDirectoryEntryNew
import com.meowool.mio.internal.backport

/**
 * Returns the zip file entry based on the given [entry].
 */
// TODO NEW API
actual fun ZipDirectoryEntry(entry: ZipEntry): ZipDirectoryEntry = backport(
  legacy = { TODO() },
  modern = {
    when (entry) {
      is ZipDirectoryEntry -> entry
      else -> DefaultZipDirectoryEntryNew(entry.toNioPath(), entry.holder)
    }
  }
)