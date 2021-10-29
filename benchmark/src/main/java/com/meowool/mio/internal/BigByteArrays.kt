@file:Suppress("FunctionName", "SpellCheckingInspection", "NAME_SHADOWING")

package com.meowool.mio.internal

import it.unimi.dsi.fastutil.BigArrays
import kotlin.math.max
import kotlin.math.min

/**
 * The shift used to compute the segment associated with an index
 * (equivalently, the logarithm of the segment size).
 */
private const val SEGMENT_SHIFT = 27

/**
 * The current size of a segment (2<sup>27</sup>) is the largest size that
 * makes the physical memory allocation for a single segment strictly
 * smaller than 2<sup>31</sup> bytes.
 */
private const val SEGMENT_SIZE = 1 shl SEGMENT_SHIFT

/** The mask used to compute the displacement associated to an index.  */
private const val SEGMENT_MASK = SEGMENT_SIZE - 1

internal typealias BigByteArray = Array<ByteArray?>

/**
 * Ensures that a big-array length is legal.
 *
 * @param bigArrayLength a big-array length.
 * @throws IllegalArgumentException if [bigArrayLength] is negative, or larger than or equal
 *   to [SEGMENT_SIZE] * [Integer.MAX_VALUE].
 */
internal fun ensureSize(bigArrayLength: Long) {
  require(bigArrayLength >= 0) { "Negative big-array size: $bigArrayLength" }
  require(bigArrayLength < Int.MAX_VALUE.toLong() shl SEGMENT_SHIFT) { "Big-array size too big: $bigArrayLength" }
}

/**
 * Creates a new big array.
 *
 * @param size the size of the new big array.
 * @return a new big array of given length.
 */
internal fun BigByteArray(size: Long): BigByteArray {
  if (size == 0L) return emptyArray()
  ensureSize(size)
  val baseLength = (size + SEGMENT_MASK ushr SEGMENT_SHIFT).toInt()
  val base = arrayOfNulls<ByteArray>(baseLength)
  val residual = (size and SEGMENT_MASK.toLong()).toInt()
  if (residual != 0) {
    for (i in 0 until baseLength - 1) base[i] = ByteArray(SEGMENT_SIZE)
    base[baseLength - 1] = ByteArray(residual)
  } else for (i in 0 until baseLength) base[i] = ByteArray(SEGMENT_SIZE)
  return base
}

/**
 * Computes the segment associated with a given index.
 *
 * @param index
 * an index into a big array.
 * @return the associated segment.
 */
internal fun segment(index: Long): Int = (index ushr SEGMENT_SHIFT).toInt()

/**
 * Computes the displacement associated with a given index.
 *
 * @param index
 * an index into a big array.
 * @return the associated displacement (in the associated
 * [segment][.segment]).
 */
internal fun displacement(index: Long): Int = (index and SEGMENT_MASK.toLong()).toInt()

/**
 * Computes the starting index of a given segment.
 *
 * @param segment
 * the segment of a big array.
 * @return the starting index of the segment.
 */
internal fun start(segment: Int): Long = segment.toLong() shl SEGMENT_SHIFT

/** Returns the element of the given big array of specified index.
 *
 * @receiver a big array.
 * @param index a position in the big array.
 * @return the element of the big array at the specified position.
 */
internal operator fun BigByteArray.get(index: Long): Byte = this[segment(index)]!![displacement(index)]

/**
 * Sets the element of the given big array of specified index.
 *
 * @receiver array a big array.
 * @param index a position in the big array.
 * @param value the new value for the array element at the specified position.
 */
internal operator fun BigByteArray.set(index: Long, value: Byte) =
  this[segment(index)]!!.set(displacement(index), value)

/**
 * Grows the given big array to the maximum between the given length and
 * the current length increased by 50%, provided that the given
 * length is larger than the current length, preserving just a part of the big array.
 *
 * If you want complete control on the big array growth, you
 * should probably use `ensureCapacity()` instead.
 *
 * **Warning:** the returned array might use part of the segments of the original
 * array, which must be considered read-only after calling this method.
 *
 * @param length the new minimum length for this big array.
 */
internal fun BigByteArray.grow(length: Long): BigByteArray {
  val oldLength = sizeTotal
  return if (length > oldLength) {
    ensureCapacity(max(oldLength + (oldLength shr 1), length), oldLength)
  } else this
}

/**
 * Ensures that a big array can contain the given number of entries, preserving just a part of the big array.
 *
 * **Warning:** the returned array might use part of the segments of the original
 * array, which must be considered read-only after calling this method.
 *
 * @param length the new minimum length for this big array.
 * @param preserve the number of elements of the big array that must be preserved in case a new allocation is necessary.
 * @return `array`, if it can contain `length` entries or more; otherwise,
 * a big array with `length` entries whose first `preserve`
 * entries are the same as those of `array`.
 */
internal fun BigByteArray.ensureCapacity(length: Long, preserve: Long): BigByteArray {
  return if (length > sizeTotal) forceCapacity(length, preserve) else this
}

/**
 * Forces a big array to contain the given number of entries, preserving just a part of the big array.
 *
 * **Warning:** the returned array might use part of the segments of the original
 * array, which must be considered read-only after calling this method.
 *
 * @receiver array a big array.
 * @param length the new minimum length for this big array.
 * @param preserve the number of elements of the big array that must be preserved in case a new allocation is necessary.
 * @return a big array with `length` entries whose first `preserve`
 * entries are the same as those of `array`.
 */
internal fun BigByteArray.forceCapacity(length: Long, preserve: Long): BigByteArray {
  ensureSize(length)
  val valid = this.size - if (this.isEmpty() || this.isNotEmpty() && this[this.size - 1]!!.size == SEGMENT_SIZE) 0 else 1
  val baseLength = (length + SEGMENT_MASK ushr SEGMENT_SHIFT).toInt()
  val base = this.copyOf(baseLength)
  val residual = (length and SEGMENT_MASK.toLong()).toInt()
  if (residual != 0) {
    for (i in valid until baseLength - 1) base[i] = ByteArray(SEGMENT_SIZE)
    base[baseLength - 1] = ByteArray(residual)
  } else for (i in valid until baseLength) base[i] = ByteArray(SEGMENT_SIZE)
  if (preserve - valid * SEGMENT_SIZE.toLong() > 0) copy(
    srcArray = this,
    srcPos = valid * SEGMENT_SIZE.toLong(),
    destArray = base,
    destPos = valid * SEGMENT_SIZE.toLong(),
    size = preserve - valid * SEGMENT_SIZE.toLong()
  )
  return base
}

/**
 * Copies a big array from the specified source big array, beginning at the specified position, to the specified position of the destination big array.
 * Handles correctly overlapping regions of the same big array.
 *
 * @param srcArray the source big array.
 * @param srcPos the starting position in the source big array.
 * @param destArray the destination big array.
 * @param destPos the starting position in the destination data.
 * @param size the number of elements to be copied.
 */
internal fun copy(
  srcArray: BigByteArray,
  srcPos: Long,
  destArray: BigByteArray,
  destPos: Long,
  size: Long,
) {
  var length = size
  if (destPos <= srcPos) {
    var srcSegment = segment(srcPos)
    var destSegment = segment(destPos)
    var srcDisplace = displacement(srcPos)
    var destDisplace = displacement(destPos)
    while (length > 0) {
      val l = min(
        length,
        min(srcArray[srcSegment]!!.size - srcDisplace, destArray[destSegment]!!.size - destDisplace).toLong()
      ).toInt()
      if (l == 0) throw ArrayIndexOutOfBoundsException()
      System.arraycopy(srcArray[srcSegment]!!, srcDisplace, destArray[destSegment]!!, destDisplace, l)
      if (l.let { srcDisplace += it; srcDisplace } == SEGMENT_SIZE) {
        srcDisplace = 0
        srcSegment++
      }
      if (l.let { destDisplace += it; destDisplace } == SEGMENT_SIZE) {
        destDisplace = 0
        destSegment++
      }
      length -= l.toLong()
    }
  } else {
    var srcSegment = segment(srcPos + length)
    var destSegment = segment(destPos + length)
    var srcDisplace = displacement(srcPos + length)
    var destDisplace = displacement(destPos + length)
    while (length > 0) {
      if (srcDisplace == 0) {
        srcDisplace = SEGMENT_SIZE
        srcSegment--
      }
      if (destDisplace == 0) {
        destDisplace = SEGMENT_SIZE
        destSegment--
      }
      val l = min(length, min(srcDisplace, destDisplace).toLong()).toInt()
      if (l == 0) throw ArrayIndexOutOfBoundsException()
      System.arraycopy(srcArray[srcSegment]!!, srcDisplace - l, destArray[destSegment]!!, destDisplace - l, l)
      srcDisplace -= l
      destDisplace -= l
      length -= l.toLong()
    }
  }
}

/**
 * Copies a big array from the specified source big array, beginning at the specified position,
 * to the specified position of the destination array.
 *
 * @receiver the source big array.
 * @param srcPos the starting position in the source big array.
 * @param destArray the destination array.
 * @param destPos the starting position in the destination data.
 * @param length the number of elements to be copied.
 */
internal fun BigByteArray.copyTo(
  destArray: ByteArray,
  destPos: Int = 0,
  srcPos: Long = 0,
  length: Int = destArray.size,
) {
  val srcArray = this
  var destPos = destPos
  var length = length
  var srcSegment = segment(srcPos)
  var srcDispl = displacement(srcPos)
  while (length > 0) {
    val l = min(srcArray[srcSegment]!!.size - srcDispl, length)
    if (l == 0) throw ArrayIndexOutOfBoundsException()
    System.arraycopy(srcArray[srcSegment]!!, srcDispl, destArray, destPos, l)
    if (l.let { srcDispl += it; srcDispl } == SEGMENT_SIZE) {
      srcDispl = 0
      srcSegment++
    }
    destPos += l
    length -= l
  }
}

/**
 * Returns the total size of the bytes of the given big array.
 *
 * @receiver array a big array.
 */
val BigByteArray.sizeTotal: Long
  get() {
    val length = this.size
    return if (length == 0) 0 else start(length - 1) + this[length - 1]!!.size
  }