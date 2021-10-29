package com.meowool.mio

import java.io.File
import java.nio.file.Paths
import java.lang.reflect.Array as JavaArray

const val slash = '/'.code.toByte()


fun normalizeByPath(bytes: String) {
  Paths.get(bytes).normalize().toString()
}

fun normalizeByPathString(bytes: String) {
  File(bytes).normalize().toString()
}

private fun remove(array: ByteArray, index: Int): ByteArray {
  "".toByteArray()
  "".encodeToByteArray()
  val length: Int = array.size
  if (index < 0 || index >= length) {
    throw IndexOutOfBoundsException("Index: $index, Length: $length")
  }
  val result = ByteArray(length - 1)
  System.arraycopy(array, 0, result, 0, index)
  if (index < length - 1) {
    System.arraycopy(array, index + 1, result, index, length - index - 1)
  }
  return result
}


fun main() {
  //  var path = "/bar/../"
//  println(CommonPath(path.toByteArray()).normalized.decodeToString())

  var path = "//.....////../bar/../.file.txt/."
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "/start/foo/../bar/.."
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "/foo/../bar/../baz/gav"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "\\\\foo//./bar/./"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "//server/../bar"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "foo/../bar"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "/foo/../bar"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "/foo/../../bar"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "/foo/../../bar"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "/../../bar"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))

  println("=====")

  path = "../../bar"
  println(CommonPath(path.toByteArray()).normalized.decodeToString())
  println(PathString(path).normalize())
  println(Paths.get(path).normalize())
  println(doNormalize(path))
}

fun normalizeByBytes() {
  CommonPath("/start/foo/../bar/..".toByteArray()).normalized.toString()
}

fun normalizeByPathString() {
//  println(File("/../../bar././..").normalize().absolutePath)
//  println(Paths.get("/../../bar././..").normalize().toAbsolutePath())
//
//  println(File("/start/foo/../bar/..").normalize().absolutePath)
//  println(Paths.get("/start/foo/../bar/..").normalize().toAbsolutePath())
  PathString("/start/foo/../bar/..").resolve("bb").toString()
}

fun normalizeByPath() {
//  println(File("/../../bar././..").normalize().absolutePath)
//  println(Paths.get("/../../bar././..").normalize().toAbsolutePath())
//
//  println(File("/start/foo/../bar/..").normalize().absolutePath)
//  println(Paths.get("/start/foo/../bar/..").normalize().toAbsolutePath())
  Paths.get("/start/foo/../bar/..").resolve("bb").toString()
}

/**
 * Internal method to perform the normalization.
 *
 * @param fileName  the fileName
 * @return the normalized fileName. Null bytes inside string will be removed.
 */
@JvmOverloads
fun doNormalize(fileName: String = "/start/foo/../bar/.."): String? {
  val separator = '/'
  if (fileName == null) {
    return null
  }
  var size = fileName.length
  if (size == 0) {
    return fileName
  }
  val prefix = 0
  if (prefix < 0) {
    return null
  }
  val array = CharArray(size + 2) // +1 for possible extra slash, +2 for arraycopy
  fileName.toCharArray(array, 0, 0, fileName.length)

  // fix separators throughout
  val otherSeparator = '\\'
  for (i in array.indices) {
    if (array[i] == otherSeparator) {
      array[i] = separator
    }
  }

  // add extra separator on the end to simplify code below
  var lastIsDirectory = true
  if (array[size - 1] != separator) {
    array[size++] = separator
    lastIsDirectory = false
  }

  // adjoining slashes
  // If we get here, prefix can only be 0 or greater, size 1 or greater
  // If prefix is 0, set loop start to 1 to prevent index errors
  run {
    var i = if (prefix != 0) prefix else 1
    while (i < size) {
      if (array[i] == separator && array[i - 1] == separator) {
        System.arraycopy(array, i, array, i - 1, size - i)
        size--
        i--
      }
      i++
    }
  }

  // dot slash
  var i = prefix + 1
  while (i < size) {
    if (array[i] == separator && array[i - 1] == '.' &&
      (i == prefix + 1 || array[i - 2] == separator)
    ) {
      if (i == size - 1) {
        lastIsDirectory = true
      }
      System.arraycopy(array, i + 1, array, i - 1, size - i)
      size -= 2
      i--
    }
    i++
  }
  run {
    var i = prefix + 2
    outer@ while (i < size) {
      if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' &&
        (i == prefix + 2 || array[i - 3] == separator)
      ) {
        if (i == prefix + 2) {
          return null
        }
        if (i == size - 1) {
          lastIsDirectory = true
        }
        var j: Int
        j = i - 4
        while (j >= prefix) {
          if (array[j] == separator) {
            // remove b/../ from a/b/../c
            System.arraycopy(array, i + 1, array, j + 1, size - i)
            size -= i - j
            i = j + 1
            i++
            continue@outer
          }
          j--
        }
        // remove a/../ from a/../c
        System.arraycopy(array, i + 1, array, prefix, size - i)
        size -= i + 1 - prefix
        i = prefix + 1
      }
      i++
    }
  }
  if (size <= 0) {  // should never be less than 0
    return ""
  }
  return if (size <= prefix) {  // should never be less than prefix
    String(array, 0, size)
  } else String(array, 0, size - 1)
  // lose trailing separator
}

fun ByteArray.split(delimiter: Byte, offset: Int = 0): MutableList<ByteArray> =
  splitBy(offset) { it == delimiter }

inline fun ByteArray.splitBy(offset: Int = 0, split: (Byte) -> Boolean): MutableList<ByteArray> =
  ArrayList<ByteArray>(this.size).apply { forEachSplitBy(split, offset, ::add) }

inline fun ByteArray.forEachSplit(
  delimiter: Byte,
  offset: Int = 0,
  action: (segment: ByteArray) -> Unit
) = this.forEachSplitBy({ it == delimiter }, offset, action)

inline fun ByteArray.forEachSplitBy(
  split: (Byte) -> Boolean,
  offset: Int = 0,
  action: (segment: ByteArray) -> Unit
) = forEachSplitIndexedBy(split, offset) { _, byte -> action(byte) }

inline fun ByteArray.forEachSplitIndexedBy(
  split: (Byte) -> Boolean,
  offset: Int = 0,
  action: (index: Int, segment: ByteArray) -> Unit
) {
  var prev = offset
  var actionIndex = 0
  this.forEachIndexed { index, byte ->
    if (split(byte) && index >= prev) {
      action(actionIndex, this.subarray(prev, index))
      actionIndex++
      prev = index + 1
    }
  }
  if (prev < this.size) action(actionIndex, this.subarray(prev, this.size))
}

/**
 * Returns first index of [element], or -1 if the array does not contain element.
 */
fun ByteArray.indexOf(element: Byte, startIndex: Int): Int {
  for (index in indices) {
    if (index >= startIndex && element == this[index]) {
      return index
    }
  }
  return -1
}

/**
 * Inserts elements into an array at the given index (starting from zero).
 */
internal fun <T : Any> T.arrayInsert(index: Int, newArray: Any): T {
  val length = JavaArray.getLength(this)
  val addedLength = JavaArray.getLength(newArray)
  if (addedLength == 0) return this
  if (index !in 0..length) throw IndexOutOfBoundsException("Index: $index, Length: $length")
  val result = JavaArray.newInstance(this.javaClass.componentType, length + addedLength)
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
