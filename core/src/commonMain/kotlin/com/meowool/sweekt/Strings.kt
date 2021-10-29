package com.meowool.sweekt

/**
 * Starting from [offset], split this string with [delimiter].
 */
fun String.split(delimiter: Char, offset: Int = 0): List<String> =
  splitBy(offset) { it == delimiter }

/**
 * Starting from [offset], split this string by [predicate].
 */
inline fun String.splitBy(offset: Int = 0, predicate: (Char) -> Boolean): List<String> =
  ArrayList<String>(this.length).apply { forEachSplitBy(predicate, offset, ::add) }

/**
 * Starting from [offset], split this string into [destination] with [delimiter].
 */
fun <C: MutableCollection<String>> String.splitTo(
  destination: C,
  delimiter: Char,
  offset: Int = 0
): C = splitTo(destination, offset) { it == delimiter }

/**
 * Starting from [offset], split this string into [destination] by [predicate].
 */
inline fun <C: MutableCollection<String>> String.splitTo(
  destination: C,
  offset: Int = 0,
  predicate: (Char) -> Boolean
):C = destination.apply { forEachSplitBy(predicate, offset, ::add) }

/**
 * Starting from [offset], split this string with [delimiter] and call the given [action]
 * for each segment after splitting.
 */
inline fun String.forEachSplit(
  delimiter: Char,
  offset: Int = 0,
  action: (segment: String) -> Unit
) = this.forEachSplitBy({ it == delimiter }, offset, action)

/**
 * Starting from [offset], split this string by [predicate] and call the given [action] for
 * each segment after splitting.
 */
inline fun String.forEachSplitBy(
  predicate: (Char) -> Boolean,
  offset: Int = 0,
  action: (segment: String) -> Unit
) = forEachSplitIndexedBy(predicate, offset) { _, segment -> action(segment) }

/**
 * Starting from [offset], split this string by [predicate] and call the given [action] for
 * each segment after splitting.
 *
 * @param action the action called for each segment after splitting, provides segment and segment
 *   index parameters.
 */
inline fun String.forEachSplitIndexedBy(
  predicate: (Char) -> Boolean,
  offset: Int = 0,
  action: (index: Int, segment: String) -> Unit
) {
  var prev = offset
  var actionIndex = 0
  this.forEachIndexed { index, char ->
    if (predicate(char) && index >= prev) {
      action(actionIndex, this.substring(prev, index))
      actionIndex++
      prev = index + 1
    }
  }
  if (prev < this.length) action(actionIndex, this.substring(prev, this.length))
}