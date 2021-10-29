@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio

import java.lang.reflect.Array
import java.util.Arrays

/**
 * TODO migrate to sweekt.
 */


fun main() {
  val qwq = "qwq"
  val qaq = StringBuilder(qwq).also { it[1] = 'a' }
  println(qaq)

  val qaq1 = qwq.toCharArray().also { it[1] = 'a' }.concatToString()
  println(qaq1)

  val qaq2 = qwq.toMutableList().apply {
    removeAt(1)
    add(1, 'a')
  }
  val qaq3 = qwq.toMutableList().also { it ->
    it.removeAt(1)
    it.add(1, 'a')
  }

  println(qaq2)
}

/**
 * Write the given [bytes] into this array.
 *
 * For example, `arrayOf(a, b, c, d).write(arrayOf(e, f))` is `arrayOf(e, f, c, d)`.
 *
 * @param offset where to start writing.
 * @param byteCount how many element of [bytes] are writing.
 * @param filter whether to filter out bytes without writing.
 * @return this array
 */
inline fun ByteArray.write(
  bytes: ByteArray,
  offset: Int = 0,
  byteCount: Int = bytes.size,
  filter: (Byte) -> Boolean = { true },
): ByteArray = apply {
  require(offset in 0..this.size) { "offset: $offset, total size: $size" }
  require(byteCount <= bytes.size) {
    "byteCount `$byteCount` cannot be greater than the total size of given the bytes."
  }
  var readIndex = 0
  var writeIndex = offset
  val writeCount = offset + byteCount
  while(writeIndex < writeCount) {
    val byte = bytes[readIndex]
    readIndex++

    if (filter(byte)) {
      this[writeIndex] = byte
      writeIndex++
    }
  }
}

/**
 * Write the given [byte] into this array.
 *
 * For example, `arrayOf(a, b, c, d).write(e, offset = 1)` is `arrayOf(a, e, c, d)`.
 *
 * @param offset where to start writing.
 * @return this array
 */
inline fun ByteArray.write(byte: Byte, offset: Int = 0): ByteArray = apply {
  require(offset in 0..this.size) { "offset: $offset, total size: $size" }
  this[offset] = byte
}

/**
 * Returns the subarray of this array starting from the [startIndex] and ending right
 * before the [endIndex].
 *
 * For example, `arrayOf(a, b, c, d).subarray(0, 2)` is `arrayOf(a, b, c)`.
 *
 * @param startIndex the start index (inclusive).
 * @param endIndex the end index (exclusive).
 */
inline fun ByteArray.subarray(startIndex: Int = 0, endIndex: Int = this.size): ByteArray =
  if (startIndex == 0 && endIndex == this.size) this
  else this.copyOfRange(startIndex, endIndex)

/**
 * Returns true if the beginning of this array with the given [prefix].
 *
 * @param prefix the prefix to be looking.
 * @param offset the offset as the starting index of the looking.
 */
inline fun ByteArray.startsWith(vararg prefix: Byte, offset: Int = 0): Boolean {
  require(offset >= 0) { "offset < 0" }
  if (this.contentEquals(prefix)) return false
  if (prefix.size + offset > this.size) return false
  return prefix.indices.all {
    val expect = prefix[it + offset]
    val actual = this[it + offset]
    expect == actual
  }
}

/**
 * Returns a (new) array and adds the given [value] at the array first.
 *
 * @param value the value to be added.
 */
inline fun ByteArray.addFirst(value: Byte): ByteArray = add(0, value)

/**
 * Returns a (new) array and adds all the given [values] at the array beginning.
 *
 * @param values all values to be added.
 */
inline fun ByteArray.addFirstAll(values: ByteArray): ByteArray = addAll(0, values)

/**
 * Returns a (new) array and inserts the given [value] at the given index (starting from zero).
 *
 * @param index the position within array to add the new [value].
 * @param value the value to be added.
 */
fun ByteArray.add(index: Int, value: Byte): ByteArray = arrayInsertArray(index, byteArrayOf(value))

/**
 * Returns a (new) array and inserts all the given [values] at the given index (starting from zero).
 *
 * @param index the position within array to add the new [values].
 * @param values all values to be added.
 */
fun ByteArray.addAll(index: Int, values: ByteArray): ByteArray = arrayInsertArray(index, values)

/**
 * Returns a (new) array and adds the given [value] at the array last.
 *
 * @param value the value to be added.
 */
inline fun ByteArray.add(value: Byte): ByteArray = plus(value)

/**
 * Returns a (new) array and adds all the given [values] at the array last.
 *
 * @param values all values to be added.
 */
inline fun ByteArray.addAll(vararg values: Byte): ByteArray = plus(values)

/**
 * Returns a new array and removes the element with the specified index.
 *
 * For example:
 * ```
 * arrayOf(a).remove(0)     = emptyArray()
 * arrayOf(a, b).remove(0)  = arrayOf(b)
 * arrayOf(a, b).remove(1)  = emptyArray(a)
 * ```
 *
 * @param index the index of the element to be removed.
*/
fun ByteArray.remove(index: Int): ByteArray = arrayRemove(index)

/**
 * Returns a new array and removes the element with the specified index.
 *
 * For example:
 * ```
 * arrayOf(a).remove(0)              = emptyArray()
 * arrayOf(a, b).remove(0)           = arrayOf(b)
 * arrayOf(a, b).remove(0, 1)        = emptyArray()
 * arrayOf(a, b, c).remove(1, 2)     = arrayOf(a)
 * arrayOf(a, b, c).remove(0, 2)     = arrayOf(b)
 * arrayOf(a, b, c).remove(0, 1, 2)  = emptyArray()
 * ```
 *
 * @param indices the index of the element to be removed.
 */
fun ByteArray.remove(vararg indices: Int): ByteArray = arrayRemove(*indices)

/**
 * Returns a new array and removes all elements in the range from the beginning of the [startIndex]
 * to the end of the [endIndex].
 *
 * For example, `arrayOf(a, b, c, d, e).removeRange(2, 4)` is `arrayOf(a, b, d)`.
 *
 * @param startIndex the start index (inclusive).
 * @param endIndex the end index (exclusive).
 */
fun ByteArray.removeRange(startIndex: Int = 0, endIndex: Int = this.size): ByteArray =
  arrayRemoveRange(startIndex, endIndex)


/**
 * Inserts elements into an array at the given index (starting from zero).
 */
internal fun <T: Any> T.arrayInsertArray(index: Int, newArray: Any): T {
  val length = Array.getLength(this)
  val addedLength = Array.getLength(newArray)
  if (addedLength == 0) return this
  if (index !in 0..length) throw IndexOutOfBoundsException("Index: $index, Length: $length")
  val result = Array.newInstance(this.javaClass.componentType, length + addedLength)
  System.arraycopy(newArray, 0, result, index, addedLength)
  if (index > 0) {
    System.arraycopy(this, 0, result, 0, index)
  }
  if (index < length) {
    System.arraycopy(this, index, result, index + addedLength, length - index)
  }
  @Suppress("UNCHECKED_CAST")
  return result as T
}

/**
 * Removes multiple array elements specified by index.
 *
 * Copy of https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/ArrayUtils.java
 */
internal fun <T: Any> T.arrayRemove(index: Int): T {
  val length = Array.getLength(this)
  if (index !in 0..length) throw IndexOutOfBoundsException("Index: $index, Length: $length")

  @Suppress("UNCHECKED_CAST")
  return Array.newInstance(this.javaClass.componentType, length - 1).also { result ->
    System.arraycopy(this, 0, result, 0, index)
    if (index < length - 1) {
      System.arraycopy(this, index + 1, result, index, length - index - 1)
    }
  } as T
}

/**
 * Removes multiple array elements specified by index.
 *
 * Copy of https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/ArrayUtils.java
 */
internal fun <T: Any> T.arrayRemove(vararg indices: Int): T {
  val length = Array.getLength(this)
  var diff = 0 // number of distinct indexes, i.e. number of entries that will be removed
  Arrays.sort(indices)

  // identify length of result array
  if (indices.isNotEmpty()) {
    var i = indices.size
    var prevIndex = length
    while (--i >= 0) {
      val index = indices[i]
      if (index < 0 || index >= length) {
        throw java.lang.IndexOutOfBoundsException("Index: $index, Length: $length")
      }
      if (index >= prevIndex) {
        continue
      }
      diff++
      prevIndex = index
    }
  }

  // create result array
  val result = Array.newInstance(this.javaClass.componentType, length - diff)
  if (diff < length) {
    var end = length // index just after last copy
    var dest = length - diff // number of entries so far not copied
    for (i in indices.indices.reversed()) {
      val index = indices[i]
      if (end - index > 1) { // same as (cp > 0)
        val cp = end - index - 1
        dest -= cp
        System.arraycopy(this, index + 1, result, dest, cp)
        // After this copy, we still have room for dest items.
      }
      end = index
    }
    if (end > 0) {
      System.arraycopy(this, 0, result, 0, end)
    }
  }
  @Suppress("UNCHECKED_CAST")
  return result as T
}

/**
 * Delete range array
 */
internal fun <T: Any> T.arrayRemoveRange(startIndex: Int, endIndex: Int): T {
  val length = Array.getLength(this)
  if (startIndex !in 0..length) {
    throw IndexOutOfBoundsException("StartIndex: $startIndex, Length: $length")
  }
  if (endIndex > length) {
    throw IndexOutOfBoundsException("EndIndex: $endIndex, Length: $length")
  }
  require(endIndex > startIndex) { "endIndex:$endIndex < startIndex:$startIndex" }
  val newSize = length - (endIndex - startIndex)
  val result = Array.newInstance(this.javaClass.componentType, newSize)

  // First part: 0..startIndex
  System.arraycopy(this, 0, result, 0, startIndex)
  // Second part: endIndex..length
  System.arraycopy(this, endIndex, result, startIndex, newSize - startIndex)

  @Suppress("UNCHECKED_CAST")
  return result as T
}