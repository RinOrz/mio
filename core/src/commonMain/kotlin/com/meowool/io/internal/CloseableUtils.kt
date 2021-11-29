package com.meowool.io.internal

import com.meowool.io.channel.SuspendCloseable

/**
 * Closes this [SuspendCloseable], suppressing possible exception or error thrown by
 * [SuspendCloseable.close] function when it's being closed due to some other [cause] exception
 * occurred.
 *
 * The suppressed exception is added to the list of suppressed exceptions to [cause] exception.
 */
@PublishedApi
internal suspend fun SuspendCloseable?.closeFinally(cause: Throwable?) = when {
  this == null -> {}
  cause == null -> close()
  else ->
    try {
      close()
    } catch (closeException: Throwable) {
      cause.addSuppressed(closeException)
    }
}