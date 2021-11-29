@file:Suppress("NO_ACTUAL_FOR_EXPECT", "REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")

package com.meowool.io.channel

import com.meowool.io.internal.closeFinally
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * An object that may hold resources (such as file or socket handles) until it is closed.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface SuspendCloseable {
  /** Close this resource */
  suspend fun close()
}

/**
 * Executes the given [block] function on this resource and then closes ([SuspendCloseable.close])
 * it down correctly whether an exception is thrown or not.
 *
 * In case if the resource is being closed due to an exception occurred in [block], and the closing
 * also fails with an exception, the latter is added to the suppressed ([Throwable.addSuppressed])
 * exceptions to the former.
 *
 * @param block a function to process this [SuspendCloseable] resource.
 * @return the result of [block] function invoked on this resource.
 */
suspend inline fun <T : SuspendCloseable?, R> T.use(block: suspend (T) -> R): R {
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
