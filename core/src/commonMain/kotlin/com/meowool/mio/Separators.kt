@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package com.meowool.mio

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
val IPath.systemSeparatorsPath: IPath
  get() = if (SystemSeparator == UnixSeparator) unixSeparatorsPath else windowsSeparatorsPath

/**
 * Returns the path that conforms to the Windows system separator.
 */
val IPath.windowsSeparatorsPath: IPath
  get() = Path(this.toString().replace(UnixSeparator, WindowsSeparator))

/**
 * Returns the path that conforms to the UNIX system separator.
 */
val IPath.unixSeparatorsPath: IPath
  get() = Path(this.toString().replace(WindowsSeparator, UnixSeparator))


internal const val UnixSeparator = "/"
internal const val WindowsSeparator = "\\"