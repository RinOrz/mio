@file:Suppress("NewApi")

package com.meowool.mio

import com.meowool.mio.internal.backport
import java.io.File
import java.nio.file.FileSystems

/**
 * Returns the char of standard separator of the current system.
 */
actual val SystemSeparatorChar: Char get() = backport(
  legacy = { File.separatorChar },
  modern = { FileSystems.getDefault().separator[0] }
)