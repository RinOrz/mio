@file:Suppress("NO_ACTUAL_FOR_EXPECT", "NOTHING_TO_INLINE")

package com.meowool.mio

/**
 * Creates an empty file in the default temp directory, using
 * the given [prefix] and [suffix] to generate its name.
 *
 * @return the path to the newly created file that did not exist before.
 */
expect fun createTempFile(prefix: String? = null, suffix: String? = null): IFile

/**
 * Creates and returns a new directory in the default temp directory, using the given [prefix] to
 * generate its name.
 */
expect fun createTempDirectory(prefix: String? = null): Directory