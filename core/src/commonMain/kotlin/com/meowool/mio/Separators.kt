package com.meowool.mio

/**
 * Returns the standard separator of the current system.
 */
expect val SystemSeparator: String

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


private const val UnixSeparator = "/"
private const val WindowsSeparator = "\\"