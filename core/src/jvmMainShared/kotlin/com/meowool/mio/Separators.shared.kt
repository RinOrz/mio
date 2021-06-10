@file:Suppress("NewApi")

package com.meowool.mio

import java.io.File
import java.nio.file.FileSystems

/**
 * Returns the standard separator of the current system.
 */
actual val SystemSeparator: String get() = runCatching {
  FileSystems.getDefault().separator
}.getOrElse {
  File.separator
}