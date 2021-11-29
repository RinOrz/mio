package com.meowool.io

import com.meowool.sweekt.LazyInit

/**
 * Represents the solution taken when an error occurs during the handling of the paths.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
enum class PathHandlingErrorSolution {
  /** Skip handling this file and go to the next. */
  Skip,

  /** Once an error occurs, stop handling all files. */
  Stop;

  internal companion object {
    @LazyInit val PathListDefault: (Any, Throwable) -> PathHandlingErrorSolution = { _, e -> throw e }
    @LazyInit val PathListSuspendDefault: suspend (Any, Throwable) -> PathHandlingErrorSolution = { _, e -> throw e }
  }
}

/**
 * Represents the solution taken when an error occurs during the copying of the files.
 *
 * @see BaseDirectory.copyTo(recursively = true)
 */
typealias CopyErrorSolution = PathHandlingErrorSolution

/**
 * Represents the solution taken when an error occurs during the moving of the files.
 *
 * @see BaseDirectory.moveTo(recursively = true)
 */
typealias MoveErrorSolution = PathHandlingErrorSolution

/**
 * Represents the solution taken when an error occurs during the deleting of the files.
 *
 * @see BaseDirectory.delete(recursively = true)
 * @see BaseDirectory.deleteStrictly(recursively = true)
 */
typealias DeleteErrorSolution = PathHandlingErrorSolution