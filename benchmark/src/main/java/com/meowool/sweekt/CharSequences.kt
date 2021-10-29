package com.meowool.sweekt


/**
 * Returns the size of this [CharSequence]
 *
 * @author 凛 (https://github.com/RinOrz)
 */
inline val CharSequence.size: Int get() = length

/**
 * Returns a substring of chars from a range of this char sequence starting at the [startIndex] and ending right before the [endIndex].
 *
 * @param startIndex the start index (inclusive).
 * @param endIndex the end index (exclusive). If not specified, the length of the char sequence is used.
 */
inline fun CharSequence.substring(startIndex: Int = 0, endIndex: Int = length): String = subSequence(startIndex, endIndex).toString()


fun CharSequence.split(delimiter: Char, offset: Int = 0): MutableList<CharSequence> =
  splitBy(offset) { it == delimiter }

inline fun CharSequence.splitBy(offset: Int = 0, split: (Char) -> Boolean): MutableList<CharSequence> =
  ArrayList<CharSequence>(this.length).apply { forEachSplitBy(split, offset, ::add) }

fun CharSequence.splitTo(
  destination: MutableList<CharSequence>,
  delimiter: Char,
  offset: Int = 0
): MutableList<CharSequence> = splitTo(destination, offset) { it == delimiter }

inline fun CharSequence.splitTo(
  destination: MutableList<CharSequence>,
  offset: Int = 0,
  split: (Char) -> Boolean
): MutableList<CharSequence> = destination.apply { forEachSplitBy(split, offset, ::add) }

inline fun CharSequence.forEachSplit(
  delimiter: Char,
  offset: Int = 0,
  action: (segment: String) -> Unit
) = this.forEachSplitBy({ it == delimiter }, offset, action)

inline fun CharSequence.forEachSplitBy(
  predicate: (Char) -> Boolean,
  offset: Int = 0,
  action: (segment: String) -> Unit
) = forEachSplitIndexedBy(predicate, offset) { _, segment -> action(segment) }

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