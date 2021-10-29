package com.meowool.mio

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * An object that may hold resources (such as file or socket handles) until it is closed.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
expect interface Closeable {
  /** Close this resource */
  fun close()
}

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether
 * an exception is thrown or not.
 *
 * In case if the resource is being closed due to an exception occurred in [block], and the closing
 * also fails with an exception, the latter is added to the [suppressed][Throwable.addSuppressed]
 * exceptions to the former.
 *
 * @param block a function to process this [Closeable] resource.
 * @return the result of [block] function invoked on this resource.
 */
inline fun <T : Closeable?, R> T.use(block: (T) -> R): R {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  var exception: Throwable? = null
  try {
    return block(this)
  } catch (e: Throwable) {
    exception = e
    throw e
  } finally {
    this.closeFinally(exception)
  }
}

/**
 * Closes this [Closeable], suppressing possible exception or error thrown by
 * [Closeable.close] function when it's being closed due to some other [cause] exception
 * occurred.
 *
 * The suppressed exception is added to the list of suppressed exceptions to [cause] exception.
 */
@PublishedApi
internal fun Closeable?.closeFinally(cause: Throwable?) = when {
  this == null -> {}
  cause == null -> close()
  else ->
    try {
      close()
    } catch (closeException: Throwable) {
      cause.addSuppressed(closeException)
    }
}