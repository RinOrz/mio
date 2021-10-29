package com.meowool.sweekt

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * Calls the given function [block] and returns the result of execution. If an exception occurs
 * during running, calls the given function [catching] and returns the catching result.
 */
inline fun <R> runSafety(catching: (Throwable) -> R, block: () -> R): R {
  contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
  return try {
    block()
  } catch (e: Throwable) {
    catching(e)
  }
}

/**
 * Calls the given function [block] and returns the result of execution. If an exception occurs
 * during running, `null` will be returned and the exception will not be delivered.
 */
inline fun <R> runSafety(block: () -> R): R? {
  contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
  return try {
    block()
  } catch (e: Throwable) {
    null
  }
}
