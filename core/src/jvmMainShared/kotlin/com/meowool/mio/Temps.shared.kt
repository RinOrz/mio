package com.meowool.mio

import com.meowool.mio.internal.backport

/**
 * Creates an empty file in the default temp directory, using
 * the given [prefix] and [suffix] to generate its name.
 *
 * @return the path to the newly created file that did not exist before.
 */
actual fun createTempFile(prefix: String?, suffix: String?): IFile = backport(
  legacy = { IoFile.createTempFile(prefix ?: "TEMP-", suffix).toMioFile() },
  modern = { kotlin.io.path.createTempFile(prefix, suffix).toMioFile() }
)

/**
 * Creates and returns a new directory in the default temp directory, using the given [prefix] to
 * generate its name.
 */
actual fun createTempDirectory(prefix: String?): Directory = backport(
  legacy = {
    IoFile.createTempFile(prefix ?: "TEMP-", null).toMioFile().apply { delete() }
      .asDirectory().create()
  },
  modern = { kotlin.io.path.createTempDirectory(prefix).toMioDirectory() }
)