package com.meowool.io

import java.io.File

/**
 * Returns the char of standard separator of the current system.
 */
actual val SystemSeparatorChar: Char get() = File.separatorChar