package com.meowool.mio

import com.meowool.sweekt.iteration.endsWith
import com.meowool.sweekt.iteration.startsWith

/**
 * Returns `true` if this path starts with the given [path] char sequence.
 */
fun IPath.startsWith(path: CharSequence): Boolean = startsWith(path.asPath())

/**
 * Returns `true` if this path starts with the given [names].
 *
 * For example:
 * ```
 * Path("a/b/c").startsWith("a", "b") == true
 * ```
 */
fun IPath.startsWith(vararg names: CharSequence): Boolean = this.split().startsWith(*names)

/**
 * Returns `true` if this path starts with the given [names].
 *
 * For example:
 * ```
 * Path("a/b/c").startsWith("a", "b") == true
 * ```
 */
fun IPath.startsWith(names: Iterable<CharSequence>): Boolean = this.split().startsWith(names)

/**
 * Returns `true` if this path starts with the given [names].
 *
 * For example:
 * ```
 * Path("a/b/c").startsWith("a", "b") == true
 * ```
 */
fun IPath.startsWith(names: Sequence<CharSequence>): Boolean = this.split().startsWith(names)

/**
 * Returns `true` if this path ends with the given [path] char sequence.
 */
fun IPath.endsWith(path: CharSequence): Boolean =
  endsWith(path.asPath())

/**
 * Returns `true` if this path ends with the given [names].
 *
 * For example:
 * ```
 * Path("a/b/c").endsWith("b", "c") == true
 * ```
 */
fun IPath.endsWith(vararg names: CharSequence): Boolean = this.split().endsWith(*names)

/**
 * Returns `true` if this path ends with the given [names].
 *
 * For example:
 * ```
 * Path("a/b/c").endsWith("b", "c") == true
 * ```
 */
fun IPath.endsWith(names: Iterable<CharSequence>): Boolean = this.split().endsWith(names)

/**
 * Returns `true` if this path ends with the given [names].
 *
 * For example:
 * ```
 * Path("a/b/c").endsWith("b", "c") == true
 * ```
 */
fun IPath.endsWith(names: Sequence<CharSequence>): Boolean = this.split().endsWith(names)