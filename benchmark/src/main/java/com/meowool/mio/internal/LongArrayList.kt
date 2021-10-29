@file:Suppress("NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate", "RedundantCompanionReference")

package com.meowool.mio.internal

import com.meowool.sweekt.toReadableSize
import org.openjdk.jol.vm.VM
import java.lang.instrument.Instrumentation
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Arrays

fun main() {
  val path = Paths.get("/Users/rin/Documents/Develop/Projects/meowool/toolkit/mio/benchmark/src/main/resources/kt-plugin.zip")
  val size = Files.size(path)

  val list = LongArrayList(size)
  for (i in 0L until size) list.add(i)

  val a = list.toArray()

  println(VM.current().sizeOf(a[0]).toReadableSize())

  val bytes = Files.readAllBytes(path)

  println(VM.current().sizeOf(bytes[0]).toReadableSize())
}

/**
 * A map typed of long primitive.
 *
 * Minimization of [fastutil](https://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/longs/Long2LongMap.html)
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class LongArrayList(initialCapacity: Int) {

  constructor(initialCapacity: Long) : this(initialCapacity.toInt())

  private var elements = when (initialCapacity) {
    0 -> EmptyArray
    else -> LongArray(initialCapacity)
  }

  var size: Int = 0

  inline fun size(): Int = size

  inline fun isEmpty(): Boolean = size == 0

  fun contains(element: Long): Boolean {
    for (i in 0 until size) {
      if (elements[i] == element) return true
    }
    return false
  }

  operator fun get(index: Int): Long {
    if (index < size) {
      return elements[index]
    }
    throwOutOfBounds(index)
  }

  inline operator fun get(index: Long): Long  =
    get(index.toInt())

  operator fun set(index: Int, element: Long): Long {
    val previous = this[index]
    elements[index] = element
    return previous
  }

  inline operator fun set(index: Long, element: Long): Long =
    set(index.toInt(), element)

  fun add(element: Long): Boolean {
    if (elements.size == size) {
      ensureCapacityForAdd()
    }
    elements[size] = element
    size++
    return true
  }

  fun addAt(index: Int, element: Long) {
    if (index > -1 && index < size) {
      addAtIndexLessThanSize(index, element)
    } else if (index == size) {
      this.add(element)
    } else {
      throwOutOfBounds(index)
    }
  }

  inline fun addAt(index: Long, element: Long) =
    addAt(index.toInt(), element)

  fun removeAt(index: Int): Long {
    val previous = this[index]
    val totalOffset = size - index - 1
    if (totalOffset > 0) {
      System.arraycopy(elements, index + 1, elements, index, totalOffset)
    }
    --size
    elements[size] = 0L
    return previous
  }

  inline fun removeAt(index: Long): Long =
    removeAt(index.toInt())

  fun clear() {
    Arrays.fill(elements, 0, size, 0L)
    size = 0
  }

  inline fun forEach(action: (Long) -> Unit) =
    elements.forEach(action)

  inline fun forEachIndexed(action: (index: Int, element: Long) -> Unit) =
    elements.forEachIndexed(action)

  fun toArray() = elements.copyOf(size)

  ////////////////////////////////////////////////////////////////////////
  ////                        Internal Apis                           ////
  ////////////////////////////////////////////////////////////////////////

  private fun addAtIndexLessThanSize(index: Int, element: Long) {
    val oldSize = size
    size++
    if (elements.size == oldSize) {
      val newItems = LongArray(sizePlusFiftyPercent(oldSize))
      if (index > 0) {
        System.arraycopy(elements, 0, newItems, 0, index)
      }
      System.arraycopy(elements, index, newItems, index + 1, oldSize - index)
      elements = newItems
    } else {
      System.arraycopy(elements, index, elements, index + 1, oldSize - index)
    }
    elements[index] = element
  }

  private fun ensureCapacityForAdd() {
    if (elements === EmptyArray) elements = LongArray(10)
    else transferItemsToNewArrayWithCapacity(sizePlusFiftyPercent(size))
  }

  private fun sizePlusFiftyPercent(oldSize: Int): Int {
    val result = oldSize + (oldSize shr 1) + 1
    return if (result < oldSize) MaxArraySize else result
  }

  private fun transferItemsToNewArrayWithCapacity(newCapacity: Int) {
    elements = copyItemsWithNewCapacity(newCapacity)
  }

  private fun copyItemsWithNewCapacity(newCapacity: Int): LongArray {
    val newItems = LongArray(newCapacity)
    System.arraycopy(elements, 0, newItems, 0, size.coerceAtMost(newCapacity))
    return newItems
  }

  private inline fun throwOutOfBounds(index: Int): Nothing =
    throw IndexOutOfBoundsException("Index: $index Size: $size")

  companion object {
    private const val MaxArraySize = Int.MAX_VALUE - 8
    private val EmptyArray: LongArray = longArrayOf()
  }
}