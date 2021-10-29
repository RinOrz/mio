package com.meowool.sweekt

/**
 * Returns a substring of chars from a range of this char sequence starting at the [startIndex] and ending right before the [endIndex].
 *
 * @param startIndex the start index (inclusive).
 * @param endIndex the end index (exclusive). If not specified, the length of the char sequence is used.
 */
inline fun CharSequence.substring(startIndex: Int = 0, endIndex: Int = length): String =
  subSequence(startIndex, endIndex).toString()

/**
 * Starting from [offset], split this char sequence with [delimiter].
 */
fun CharSequence.split(delimiter: Char, offset: Int = 0): List<CharSequence> =
  splitBy(offset) { it == delimiter }

/**
 * Starting from [offset], split this char sequence by [predicate].
 */
inline fun CharSequence.splitBy(offset: Int = 0, predicate: (Char) -> Boolean): List<CharSequence> =
  ArrayList<CharSequence>(this.length).apply { forEachSplitBy(predicate, offset, ::add) }

/**
 * Starting from [offset], split this char sequence into [destination] with [delimiter].
 */
fun <C: MutableCollection<CharSequence>> CharSequence.splitTo(
  destination: C,
  delimiter: Char,
  offset: Int = 0
): C = splitTo(destination, offset) { it == delimiter }

/**
 * Starting from [offset], split this char sequence into [destination] by [predicate].
 */
inline fun <C: MutableCollection<CharSequence>> CharSequence.splitTo(
  destination: C,
  offset: Int = 0,
  predicate: (Char) -> Boolean
):C = destination.apply { forEachSplitBy(predicate, offset, ::add) }

/**
 * Starting from [offset], split this char sequence with [delimiter] and call the given [action] for each segment
 * after splitting.
 */
inline fun CharSequence.forEachSplit(
  delimiter: Char,
  offset: Int = 0,
  action: (segment: String) -> Unit
) = this.forEachSplitBy({ it == delimiter }, offset, action)

/**
 * Starting from [offset], split this char sequence by [predicate] and call the given [action] for each segment
 * after splitting.
 */
inline fun CharSequence.forEachSplitBy(
  predicate: (Char) -> Boolean,
  offset: Int = 0,
  action: (segment: String) -> Unit
) = forEachSplitIndexedBy(predicate, offset) { _, segment -> action(segment) }

/**
 * Starting from [offset], split this char sequence by [predicate] and call the given [action] for each segment
 * after splitting.
 *
 * @param action the action called for each segment after splitting, receives segment and segment index parameters.
 */
inline fun CharSequence.forEachSplitIndexedBy(
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