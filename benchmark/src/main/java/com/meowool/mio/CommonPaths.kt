package com.meowool.mio

import com.meowool.sweekt.array.buildByteArray
import okio.BufferedSource
import java.io.DataOutputStream
import java.io.File.separatorChar
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.ArrayDeque

/**
 * A common pure path backend, no need to rely on any file system implementation.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class CommonPath(private val array: ByteArray) {
  private var hashCode: Int = 0
  private var indexOfLastSeparator: Int? = null
  private val noSeparator = indexOfLastSeparator == -1
  private val volumeLabelExists = array.size >= 2 &&
    array[1] == Colon &&
    array[0].toInt().toChar().isEnglishNotPunctuation()

  /** The path is `~` or `~/` */
  private val isUserHomeSymbol = (array.size == 1 && array.first().isTilde) ||
    (array.size == 2 && array.first().isTilde && array.last().isSeparator)

  /** Is the path beginning with `~/` or equals `~` */
  private val isInUserHomeDir = when {
    array.size > 2 -> array[0] == Tilde && array[1].isSeparator
    else -> isUserHomeSymbol
  }

  private inline val Byte?.isColon get() = this == Colon
  private inline val Byte?.isDot get() = this == Dot
  private inline val Byte?.isNotDot get() = this != Dot
  private inline val Byte?.isTilde
    get() = this == Tilde

  private inline val ByteArray.isUNCPath
    get() = this.getOrNull(0).isSeparator && this.getOrNull(1).isSeparator

  private inline val ByteArray.isTilde
    get() = this.size == 1 && this.first().isTilde

  private inline val ByteArray.isDot
    get() = this.size == 1 && this.first().isDot

  private inline val ByteArray.isTwoDot
    get() = this.size == 2 && this.first().isDot && this.last().isDot

  private fun lastSeparatorAt(index: Int): Boolean {
    if (indexOfLastSeparator == null) {
      indexOfLastSeparator = array.lastIndexOf(Slash).takeIf { it != -1 }
        ?: array.lastIndexOf(Backslash)
    }
    return indexOfLastSeparator == index
  }

  private fun make(byteArray: ByteArray) = byteArray

  val isDirectory = false

  // 1. Converts all separators to system flavor
  // 2. Remove duplicate separators like `//`
  // 3. Remove all useless single dot like `./`
  // 4. Resolve all parent paths `..`, like `foo/../bar/../baz` to `baz`
  @Volatile
  private var _normalized: ByteArray? = null
  val normalized: ByteArray
    get() = _normalized ?: synchronized(this) {
      // Simple check whether it has been normalized
      if (
        array.isEmpty() ||
        // `/`
        (array.size == 1 && array.first().isSeparator) ||
        // `foo`
        array.none { it.isSeparator || it.isDot || it == Tilde }
      ) return make(array)

      // Just `////` to `/`
      if (array.all { it.isSeparator }) return make(byteArrayOf(SystemSeparator))

      // Just change `~` or `~/` to `/userHome/`
      if (isUserHomeSymbol) return make(UserHome)

      val list = ArrayDeque<Any>(array.size)
      // If it has no root, do not process the beginning `../`, to as a relative path,
      // only parse parent symbols like of `/../../` when the value is `true`
      var canResolveParentSymbol = hasRoot
      // If it has prefixes, skip them when traversing
      var count = prefixLength

      // In the case of `~/array`, will process to `/userHome/array`
      if (isInUserHomeDir) {
        list.add(UserHome)
        count += UserHome.size
        if (UserHome.last().isNotSeparator) {
          list.add(SystemSeparator)
          count++
        }
      }

      array.forEachSplitIndexedBy(
        offset = prefixLength,
        split = { it.isSeparator }
      ) { curIndex, curSegment ->
        when {
          curSegment.isEmpty() ||
            curSegment.isDot ||
            // Ignore beginning `~`
            (curSegment.isTilde && curIndex == 0) -> {
            // Do nothing
          }
          curSegment.isTwoDot && (hasRoot || canResolveParentSymbol) -> {
            fun removeLast() = list.pollLast()?.apply {
              count -= (this as? ByteArray)?.size ?: 1
            }

            // Remove segment or separator
            val removedLast = removeLast()
            // If the last removed is a separator, remove again to remove the last segment.
            if (removedLast == SystemSeparator) removeLast()
          }
          else -> {
            // When the normal bytes appeared, the parent symbol can be resolved
            if (!canResolveParentSymbol && curSegment.isTwoDot.not()) canResolveParentSymbol = true

            when (curSegment.size) {
              1 -> list.addLast(curSegment.first())
              else -> list.addLast(curSegment)
            }
            list.addLast(SystemSeparator)
            // Segment and Separator
            count += curSegment.size + 1
          }
        }
      }

      if (isDirectory.not() && array.last().isSeparator.not()) {
        list.removeLast()
        count--
      }

      // Join all bytes and the flattened bytearray
      val array = buildByteArray(count) {
        // Restore prefix
        repeat(prefixLength) {
          val byte = array[it]
          when {
            byte.isSeparator -> append(SystemSeparator)
            else -> append(byte)
          }
        }

        list.forEach {
          when (it) {
            is Byte -> append(it)
            is ByteArray -> append(it)
          }
        }
      }

      make(array).also { _normalized = it }
    }

  var hasRoot: Boolean = false

  var isRoot: Boolean = false

  val prefixLength = when {
    // Length of the root of UNC path: `\\`
    array.isUNCPath -> {
      hasRoot = true
      isRoot = array.size == 2
      2
    }
    // Length of volume label
    volumeLabelExists -> when {
      // Absolute path `C:/`
      array.getOrNull(2).isSeparator -> {
        hasRoot = true
        isRoot = array.size == 3
        3
      }
      // Relative path `C:`
      else -> {
        isRoot = array.size == 2
        2
      }
    }
    // Length of the root
    array.first().isSeparator -> {
      hasRoot = true
      isRoot = array.size == 1
      1
    }
    // No prefix
    else -> 0
  }

  fun join(vararg paths: String): ByteArray {
    paths.forEach {
      CommonPath(it.encodeToByteArray())
    }
    "".ifNull {  }
    TODO("Not yet implemented")
  }

  //  private val separatorIndices = kotlin.run {
//
//    val indices = IntArray(array.size)
//    var separatorCount = 0
//    // 找出所有 / 的索引
//    array.forEachIndexed { index, byte ->
//      if (byte.isSeparator) {
//        indices[separatorCount] = index
//        separatorCount++
//      }
//    }
//    indices.copyOf(separatorCount)
//  }
//
//  val segments = ArrayList<ByteArray>(separatorIndices.size).apply {
//    var index = 0
//    while (index < separatorIndices.size) {
//      val startIndex = separatorIndices[index] + 1
//      val endIndex = when (index) {
//        separatorIndices.size - 1 -> array.size
//        else -> separatorIndices[index + 1]
//      }
//      if (endIndex > startIndex) add(array.subarray(startIndex, endIndex))
//      index++
//    }
//  }
  val segments get() = array.split(SystemSeparator)

  companion object {
    private const val Slash = '/'.toByte()
    private const val Backslash = '\\'.toByte()
    private const val Colon = ':'.toByte()
    private const val Dot = '.'.toByte()
    private const val Tilde = '~'.toByte()

    private val SystemSeparator = separatorChar.toByte()

    private val UserHome = userHome.encodeToByteArray()

    private val TwoDot = "..".encodeToByteArray()

    /** Means to return to the previous path */
    private val UpperSlash = "/..".encodeToByteArray()
    private val UpperBackslash = "\\..".encodeToByteArray()


    private inline val Any?.isSeparator get() = this != null && (this == Slash || this == Backslash)
    private inline val Byte?.isNotSeparator get() = isSeparator.not()
  }
}

internal val userHome: String = System.getProperty("user.home")

/**
 * Select the given value based on this boolean value.
 *
 * @return if this boolean is `true`, returns the [true] value, otherwise returns the [false] value.
 */
inline fun <R> Boolean?.select(`true`: R, `false`: R): R = if (this == true) `true` else `false`

/**
 * Returns `true` if this character is english and not english punctuation.
 */
inline fun Char.isEnglishNotPunctuation(): Boolean = this in 'A'..'Z' || this in 'a'..'z'

/**
 * Returns `true` if the instance of this object is null.
 */
inline fun Any?.isNull(): Boolean {
  return this == null
}

inline fun Any?.isNotNull(): Boolean {
  return this != null
}

/**
 * If this [T] is not null then this is returned, otherwise [another] is executed and its result
 * is returned.
 */
inline fun <T> T?.ifNull(another: () -> T): T {
  return this ?: another()
}

//fun ByteArray.split(byte: Byte) {
//
//  val result = ArrayList<ByteArray>(array.size)
//  var lastSegment = 0
//  array.forEachIndexed { index, byte ->
//    if (byte.isSeparator && index > lastSegment) {
//      result.add(array.subarray(lastSegment, index))
//      lastSegment = index + 1
//    }
//  }
//  val endIndex = array.size - 1
//  if (lastSegment < endIndex) {
//    result.add(array.subarray(lastSegment, endIndex))
//  }
//  return result
//}

fun main() {
//  println(Paths.get("C:/foo/bar/c").relativize(Paths.get("C:/foo/file.txt")))
//  println(PathString("C:/foo/bar/file.txt").relativize(PathString("C:/foo/baz/")))
//  println(PathString("foo/").relativize(PathString("baz/")))
//  println(Paths.get("foo/").relativize(Paths.get("baz/")))

  val channel = FileChannel.open(
    Paths.get("/Users/rin/Documents/Develop/Projects/meowool/toolkit/mio/benchmark/src/main/java/com/meowool/mio/BaseZipFileIterateBenchmark.kt"),
    StandardOpenOption.READ
  )

  val byteBuffer = ByteBuffer.allocate(channel.size().toInt())
  println(channel.position())
  channel.read(byteBuffer)
  byteBuffer.position(0)
  println(byteBuffer.char.toString())
  println(byteBuffer.char.toString())
  byteBuffer.position(0)
  println(byteBuffer.char.toString())

  ByteBuffer.allocate(10)
    .put('T'.code.toByte())
    .put('S'.code.toByte())
    .putDouble(1, 5.0)
    .put('V'.code.toByte())
    .put("AB".encodeToByteArray(), 0, 2)
    .array().decodeToString().also(::println)
//  val array = ByteArray(channel.size().toInt())
//  byteBuffer.get(array)
//  println(array.decodeToString())
//  println(CommonPath("C:///foo///".toByteArray()).normalized1.decodeToString())
//  println(CommonPath("C:foo///".toByteArray()).normalized1.decodeToString())
//  println(CommonPath("~/foo///".toByteArray()).normalized1.decodeToString())
//  println(CommonPath("~/foo/../bar/".toByteArray()).normalized1.decodeToString())
}