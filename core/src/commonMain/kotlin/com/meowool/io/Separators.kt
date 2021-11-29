@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package com.meowool.io

/**
 * Returns the char of the standard separator of the current system.
 */
expect val SystemSeparatorChar: Char

/**
 * Returns the standard separator of the current system.
 */
val SystemSeparator: String
  get() = SystemSeparatorChar.toString()

/**
 * Returns the path that conforms to the system separator.
 */
val Path.systemSeparatorsPath: Path
  get() = if (SystemSeparator == UnixSeparator) unixSeparatorsPath else windowsSeparatorsPath

/**
 * Returns the path that conforms to the Windows system separator.
 */
val Path.windowsSeparatorsPath: Path
  get() = Path(this.toString().replace(UnixSeparator, WindowsSeparator))

/**
 * Returns the path that conforms to the UNIX system separator.
 */
val Path.unixSeparatorsPath: Path
  get() = Path(this.toString().replace(WindowsSeparator, UnixSeparator))


internal const val UnixSeparator = "/"
internal const val WindowsSeparator = "\\"