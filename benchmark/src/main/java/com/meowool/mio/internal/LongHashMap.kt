@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate", "RedundantCompanionReference")

package com.meowool.mio.internal

import java.util.Arrays
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * A map typed of long primitive.
 *
 * Minimization of [fastutil](https://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/longs/Long2LongMap.html)
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class LongHashMap {

  /** The array of keys.  */
  private var key: LongArray

  /** The array of values.  */
  private var value: LongArray

  /** The mask for wrapping a position counter.  */
  private var mask: Int

  /** Whether this map contains the key zero.  */
  private var containsNullKey = false

  /** The current table size.  */
  private var n: Int

  /** Threshold after which we rehash. It must be the table size times [.f].  */
  private var maxFill: Int

  /** We never resize below this threshold, which is the construction-time {#n}.  */
  private val minN: Int

  /** Number of entries in the set (including the key zero, if present).  */
  private var size = 0

  constructor(initialCapacity: Int = Common.InitialIntSize) {
    require(initialCapacity >= 0) { "The expected number of elements must be non negative" }
    n = Common.arraySize(initialCapacity, LoadFactor)
    minN = n
    mask = n - 1
    maxFill = Common.maxFill(n, LoadFactor)
    key = LongArray(n + 1)
    value = LongArray(n + 1)
  }

  constructor(initialCapacity: Long = Common.InitialLongSize) {
    require(initialCapacity >= 0) { "The expected number of elements must be non negative" }
    n = Common.arraySize(initialCapacity, LoadFactor)
    minN = n
    mask = n - 1
    maxFill = Common.maxFill(n, LoadFactor)
    key = LongArray(n + 1)
    value = LongArray(n + 1)
  }

  operator fun set(key: Long, value: Long): Long {
    val pos = find(key)
    if (pos < 0) {
      insert(-pos - 1, key, value)
      return Common.DefaultLong
    }
    val oldValue = this.value[pos]
    this.value[pos] = value
    return oldValue
  }

  operator fun get(key: Long): Long {
    if (key == 0L) return if (containsNullKey) value[n] else DefaultLong
    val keys = this.key
    var curr: Long
    var pos: Int
    // The starting point.
    if (keys[Common.mix(key).toInt() and mask.also { pos = it }].also { curr = it } == 0L)
      return DefaultLong
    if (key == curr) return value[pos]
    // There's always an unused entry.
    while (true) {
      if (keys[pos + 1 and mask.also { pos = it }].also { curr = it } == 0L) return DefaultLong
      if (key == curr) return value[pos]
    }
  }

  fun remove(key: Long): Long {
    if (key == 0L) {
      return if (containsNullKey) removeNullEntry() else DefaultLong
    }
    val keys = this.key
    var curr: Long
    var pos: Int
    // The starting point.
    if (keys[Common.mix(key).toInt() and mask.also { pos = it }].also { curr = it } == 0L) {
      return DefaultLong
    }
    if (key == curr) return removeEntry(pos)
    while (true) {
      if (keys[pos + 1 and mask.also { pos = it }].also { curr = it } == 0L) return DefaultLong
      if (key == curr) return removeEntry(pos)
    }
  }

  inline fun forEach(action: (key: Long, value: Long) -> Unit) {
    if (containsNullKey) action(key[n], value[n])
    var pos = n
    while (pos-- != 0) if (key[pos] != 0L) action(key[pos], value[pos])
  }

  inline fun forEachKeys(action: (Long) -> Unit) {
    if (containsNullKey) action(key[n])
    var pos = n
    while (pos-- != 0) key[pos].also { k ->
      if (k != 0L) action(k)
    }
  }

  inline fun forEachValues(action: (Long) -> Unit) {
    if (containsNullKey) action(value[n])
    var pos = n
    while (pos-- != 0) if (key[pos] != 0L) action(value[pos])
  }

  inline fun size(): Int = size

  inline fun isEmpty(): Boolean = size == 0

  inline fun isNotEmpty(): Boolean = size != 0

  /**
   *  Removes all elements from this map.
   *
   * To increase object reuse, this method does not change the table size.
   * If you want to reduce the table size, you must use [trim].
   *
   */
  fun clear() {
    if (size == 0) return
    size = 0
    containsNullKey = false
    Arrays.fill(key, 0L)
  }

  /**
   * Rehashes this map if the table is too large.
   *
   * Let [n] be the smallest table size that can hold `max(n,[.size])` entries, still satisfying
   * the load factor. If the current table size is smaller than or equal to <var>N</var>, this method does
   * nothing. Otherwise, it rehashes this map in a table of size [n].
   *
   *
   * This method is useful when reusing maps. [clear] leaves the table size untouched. If you are
   * reusing a map many times, you can call this method with a typical size to avoid keeping around
   * a very large table just because of a few large transient maps.
   *
   * @param n the threshold for the trimming.
   * @return true if there was enough memory to trim the map.
   */
  fun trim(n: Int = size): Boolean {
    val l = Common.nextPowerOfTwo(ceil(n / LoadFactor).toInt())
    if (l >= this.n || size > Common.maxFill(l, LoadFactor)) return true
    try {
      rehash(l)
    } catch (cantDoIt: OutOfMemoryError) {
      return false
    }
    return true
  }


  ////////////////////////////////////////////////////////////////////////
  ////                        Internal Apis                           ////
  ////////////////////////////////////////////////////////////////////////

  private fun find(k: Long): Int {
    if (k == 0L) return if (containsNullKey) n else -(n + 1)
    var curr: Long
    val key = key
    var pos: Int
    // The starting point.
    if (key[Common.mix(k)
        .toInt() and mask.also { pos = it }].also { curr = it } == 0L
    ) return -(pos + 1)
    if (k == curr) return pos
    // There's always an unused entry.
    while (true) {
      if (key[pos + 1 and mask.also { pos = it }].also { curr = it } == 0L) return -(pos + 1)
      if (k == curr) return pos
    }
  }

  private fun insert(pos: Int, k: Long, v: Long) {
    if (pos == n) containsNullKey = true
    key[pos] = k
    value[pos] = v
    if (size++ >= maxFill) rehash(Common.arraySize(size + 1, LoadFactor))
  }

  private fun removeEntry(pos: Int): Long {
    val oldValue = value[pos]
    size--
    shiftKeys(pos)
    if (n > minN && size < maxFill / 4 && n > Common.InitialIntSize) rehash(n / 2)
    return oldValue
  }

  private fun removeNullEntry(): Long {
    containsNullKey = false
    val oldValue = value[n]
    size--
    if (n > minN && size < maxFill / 4 && n > Common.InitialIntSize) rehash(n / 2)
    return oldValue
  }

  /**
   * Rehashes the map.
   *
   * This method implements the basic rehashing strategy, and may be
   * overridden by subclasses implementing different rehashing strategies (e.g.,
   * disk-based rehashing). However, you should not override this method
   * unless you understand the internal workings of this class.
   *
   * @param newN the new size
   */
  private fun rehash(newN: Int) {
    val key = key
    val value = value
    val mask = newN - 1 // Note that this is used by the hashing macro
    val newKey = LongArray(newN + 1)
    val newValue = LongArray(newN + 1)
    var i = n
    var pos: Int
    var realSize = if (containsNullKey) size - 1 else size
    @Suppress("ControlFlowWithEmptyBody")
    while (realSize-- != 0) {
      while (key[--i] == 0L);
      if (newKey[Common.mix(key[i]).toInt() and mask.also { pos = it }] != 0L) {
        while (newKey[pos + 1 and mask.also { pos = it }] != 0L);
      }
      newKey[pos] = key[i]
      newValue[pos] = value[i]
    }
    newValue[newN] = value[n]
    n = newN
    this.mask = mask
    maxFill = Common.maxFill(n, LoadFactor)
    this.key = newKey
    this.value = newValue
  }


  /**
   * Shifts left entries with the specified hash code, starting at the specified position,
   * and empties the resulting free entry.
   *
   * @param pos a starting position.
   */
  private fun shiftKeys(pos: Int) {
    // Shift entries with the same hash.
    var p = pos
    var last: Int
    var slot: Int
    var curr: Long
    val key = key
    while (true) {
      p = p.also { last = it } + 1 and mask
      while (true) {
        if (key[p].also { curr = it } == 0L) {
          key[last] = 0
          return
        }
        slot = Common.mix(curr).toInt() and mask
        if (if (last <= p) last >= slot || slot > p else slot in (p + 1)..last) break
        p = p + 1 and mask
      }
      key[last] = curr
      value[last] = value[p]
    }
  }

  private companion object Common {
    /** The initial default size of a hash table. */
    const val InitialIntSize = 16
    const val InitialLongSize = 16L
    /** The acceptable load factor. */
    const val LoadFactor = .75f

    const val DefaultLong = 0L
    const val LongPhi = -0x61c8864680b583ebL

    /**
     * Returns the maximum number of entries that can be filled before rehashing.
     *
     * @param n the size of the backing array.
     * @param f the load factor.
     * @return the maximum number of entries before rehashing.
     */
    fun maxFill(n: Int, f: Float): Int {
      /* We must guarantee that there is always at least
		 * one free entry (even with pathological load factors). */
      return min(ceil(n * f).toInt(), n - 1)
    }

    /**
     * Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than
     * or equal to `Math.ceil(expected / f)`.
     *
     * @param expected the expected number of elements in a hash table.
     * @param f the load factor.
     * @return the minimum possible size for a backing array.
     * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
     */
    fun arraySize(expected: Int, f: Float): Int {
      val s = max(2, nextPowerOfTwo(ceil(expected / f).toLong()))
      require(s <= 1 shl 30) { "Too large ($expected expected elements with load factor $f)" }
      return s.toInt()
    }

    /**
     * Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than
     * or equal to `Math.ceil(expected / f)`.
     *
     * @param expected the expected number of elements in a hash table.
     * @param f the load factor.
     * @return the minimum possible size for a backing array.
     * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
     */
    fun arraySize(expected: Long, f: Float): Int {
      val s = max(2, nextPowerOfTwo(ceil(expected / f).toLong()))
      require(s <= 1 shl 30) { "Too large ($expected expected elements with load factor $f)" }
      return s.toInt()
    }

    /**
     * Returns the least power of two greater than or equal to the specified value.
     *
     *
     * Note that this function will return 1 when the argument is 0.
     *
     * @param x a long integer smaller than or equal to 2<sup>62</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    fun nextPowerOfTwo(x: Long): Long {
      var r = x
      if (r == 0L) return 1
      r--
      r = r or (r shr 1)
      r = r or (r shr 2)
      r = r or (r shr 4)
      r = r or (r shr 8)
      r = r or (r shr 16)
      return (r or (r shr 32)) + 1
    }

    /**
     * Returns the least power of two greater than or equal to the specified value.
     *
     * Note that this function will return 1 when the argument is 0.
     *
     * @param x an integer smaller than or equal to 2<sup>30</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    fun nextPowerOfTwo(x: Int): Int {
      var r = x
      if (r == 0) return 1
      r--
      r = r or (r shr 1)
      r = r or (r shr 2)
      r = r or (r shr 4)
      r = r or (r shr 8)
      return (r or (r shr 16)) + 1
    }

    /**
     * Quickly mixes the bits of a long integer.
     *
     * This method mixes the bits of the argument by multiplying by the golden ratio and
     * xor shifting twice the result.
     *
     * @param x a long integer.
     * @return a hash value obtained by mixing the bits of `x`.
     */
    fun mix(x: Long): Long {
      var h = x * LongPhi
      h = h xor (h ushr 32)
      return h xor (h ushr 16)
    }
  }
}