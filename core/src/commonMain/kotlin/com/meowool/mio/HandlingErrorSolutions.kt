package com.meowool.mio

/**
 * Represents the solution taken when an error occurs during the handling of the files.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
enum class FileHandlingErrorSolution {
  /** Skip handling this file and go to the next. */
  Skip,

  /** Once an error occurs, stop handling all files. */
  Stop
}

/**
 * Represents the solution taken when an error occurs during the copying of the files.
 *
 * @see Path.copyTo(recursively = true)
 */
typealias CopyErrorSolution = FileHandlingErrorSolution

/**
 * Represents the solution taken when an error occurs during the moving of the files.
 *
 * @see Path.moveTo(recursively = true)
 */
typealias MoveErrorSolution = FileHandlingErrorSolution

/**
 * Represents the solution taken when an error occurs during the deleting of the files.
 *
 * @see Path.delete(recursively = true)
 * @see Path.deleteStrictly(recursively = true)
 */
typealias DeleteErrorSolution = FileHandlingErrorSolution