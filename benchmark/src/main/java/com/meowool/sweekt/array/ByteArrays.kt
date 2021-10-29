@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.sweekt.array

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
    val byte = bytes[readIndex++]
    if (filter(byte)) this[writeIndex++] = byte
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
