package com.meowool.mio

/**
 * Represents the solution taken when an error occurs during the handling of the paths.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
enum class PathHandlingErrorSolution {
  /** Skip handling this file and go to the next. */
  Skip,

  /** Once an error occurs, stop handling all files. */
  Stop
}

/**
 * Represents the solution taken when an error occurs during the copying of the files.
 *
 * @see IDirectory.copyTo(recursively = true)
 */
typealias CopyErrorSolution = PathHandlingErrorSolution

/**
 * Represents the solution taken when an error occurs during the moving of the files.
 *
 * @see IDirectory.moveTo(recursively = true)
 */
typealias MoveErrorSolution = PathHandlingErrorSolution

/**
 * Represents the solution taken when an error occurs during the deleting of the files.
 *
 * @see IDirectory.delete(recursively = true)
 * @see IDirectory.deleteStrictly(recursively = true)
 */
typealias DeleteErrorSolution = PathHandlingErrorSolution