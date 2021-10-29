package com.meowool.mio

import java.io.EOFException
import java.io.IOException
import java.nio.file.FileSystemException


/**
 * Signals that an I/O exception to some sort has occurred. This
 * class is the general class of exceptions produced by failed or
 * interrupted I/O operations.
 */
actual typealias IOException = IOException