package com.meowool.sweekt

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Calls the specified function [block] and returns its result. If an exception occurs during calling, calls the
 * specified function [catching] to catch exception and returns the result.
 *
 * This is a lighter implementation than [runCatching].
 *
 * @see kotlin.run
 * @author 凛 (https://github.com/RinOrz)
 */
inline fun <T, R> T.run(catching: T.(Throwable) -> R, block: T.() -> R): R {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  return try {
    block()
  } catch (e: Throwable) {
    catching(e)
  }
}

/**
 * Calls the specified function [block] and returns its result. If an exception occurs during calling, `null` will
 * be returned and the exception will not be thrown.
 *
 * This is a lighter implementation than [runCatching].
 *
 * @see kotlin.run
 */
inline fun <T, R> T.runOrNull(block: T.() -> R): R? {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  return try {
    block()
  } catch (e: Throwable) {
    null
  }
}

/**
 * Calls the specified function [block] with `this` value as its receiver and returns `this` value. If an exception
 * occurs during calling, calls the specified function [catching] to catch exception.
 *
 * This is a lighter implementation than [runCatching].
 *
 * @see kotlin.apply
 */
inline fun <T, R> T.apply(catching: T.(Throwable) -> R, block: T.() -> R): R {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  return try {
    block()
  } catch (e: Throwable) {
    catching(e)
  }
}

/**
 * Calls the specified function [block] with `this` value as its receiver and returns `this` value. If an exception
 * occurs during calling, `null` will be returned and the exception will not be thrown.
 *
 * This is a lighter implementation than [runCatching].
 *
 * @see kotlin.apply
 */
inline fun <T> T.applyOrNull(block: T.() -> Unit): T? {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  return try {
    block()
    this
  } catch (e: Throwable) {
    null
  }
}

