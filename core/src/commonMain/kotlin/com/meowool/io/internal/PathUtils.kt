@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package com.meowool.io.internal

import com.meowool.io.CurrentRelativePath
import com.meowool.io.Path
import com.meowool.io.SystemSeparator
import com.meowool.io.SystemSeparatorChar
import com.meowool.sweekt.isEnglishNotPunctuation
import com.meowool.sweekt.isNotNull
import com.meowool.sweekt.isNull
import com.meowool.sweekt.removeLast
import com.meowool.sweekt.removeRange
import com.meowool.sweekt.select
import com.meowool.sweekt.size
import com.meowool.sweekt.splitBy
import com.meowool.sweekt.splitTo
import com.meowool.sweekt.substring
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal expect val userHome: String
internal expect val currentDir: String

internal const val Slash = '/'
internal const val Backslash = '\\'
internal const val Colon = ':'
internal const val Dot = '.'
internal const val Tilde = '~'
internal const val TwoDot = ".."
internal const val SlashTwoDot = "/.."
internal const val BackslashTwoDot = "\\.."

private inline val UserHome: String get() = "$userHome$SystemSeparatorChar"
private inline val Char?.isSeparator: Boolean get() = this != null && (this == Slash || this == Backslash)
private inline val Char?.isNotSeparator: Boolean get() = isSeparator.not()
private inline val Char?.isColon: Boolean get() = this == Colon
private inline val Char?.isDot: Boolean get() = this == Dot
private inline val Char?.isNotDot: Boolean get() = this != Dot
private inline val Char?.isTilde: Boolean get() = this == Tilde

private inline val CharSequence.isUNCPath: Boolean
  get() = this.getOrNull(0).isSeparator && this.getOrNull(1).isSeparator

private inline val CharSequence.isTilde: Boolean
  get() = this.length == 1 && this.first().isTilde

private inline val CharSequence.isDot: Boolean
  get() = this.length == 1 && this.first().isDot

private inline val CharSequence.isTwoDot: Boolean
  get() = this.length == 2 && this.first().isDot && this.last().isDot

/** The path is `~` or `~/` */
private val CharSequence.isUserHomeSymbol: Boolean
  get() = (this.size == 1 && this.first().isTilde) ||
    (this.size == 2 && this.first().isTilde && this.last().isSeparator)

/** Is the path beginning with `~/` or equals `~` */
private val CharSequence.isInUserHomeDir: Boolean
  get() = when {
    this.size > 2 -> this[0].isTilde && this[1].isSeparator
    else -> isUserHomeSymbol
  }

private val CharSequence.volumeLabelExists
  get() = this.length >= 2 && this[1] == Colon && this[0].isEnglishNotPunctuation()

/**
 * Returns the extension of [fileName].
 * `file.txt` -> `txt` or `.txt`
 * `.hidden.zip` -> `zip` or `.zip`
 * `.hidden` -> ``
 */
internal fun getFileExtension(fileName: CharSequence, withDot: Boolean): String =
  isHiddenNameWithoutExtension(fileName).select(
    no = when (val extensionIndex = fileName.lastIndexOf('.')) {
      -1 -> ""
      else -> fileName.substring(startIndex = extensionIndex + withDot.select(0, 1))
    },
    yes = "", // no extension
  )

/** Just only `.file` */
internal fun isHiddenNameWithoutExtension(fileName: CharSequence): Boolean =
  fileName.first() == '.' && fileName.count { it == '.' } == 1

/** Detect possible root prefixes at the beginning of a given [path]. */
internal inline fun <R> detectPrefix(
  path: CharSequence,
  callback: (prefixLength: Int, isRoot: Boolean, hasRoot: Boolean) -> R,
): R {
  contract { callsInPlace(callback, InvocationKind.EXACTLY_ONCE) }
  return when {
    // Length of the path starting with a `~`
    path.isInUserHomeDir -> when {
      // `~/foo/bar`
      path.length > 2 -> callback(2, false, true)
      // Just `~`
      else -> callback(1, true, true)
    }

    // Length of the root of UNC path: `\\`
    path.isUNCPath -> callback(2, path.length == 2, true)

    // Length of volume label
    path.volumeLabelExists -> when {
      // Absolute path `C:/`
      path.getOrNull(2).isSeparator -> callback(3, path.length == 3, true)
      // Relative path `C:`
      else -> callback(2, path.length == 2, false)
    }

    // Length of the root
    path.first().isSeparator -> callback(1, path.length == 1, true)

    // No prefix
    else -> callback(0, false, false)
  }
}

/** Normalize the given [path]. */
internal fun normalize(
  path: CharSequence,
  hasRoot: Boolean,
  prefixLength: Int,
  isDirectory: Boolean = false,
): String {
  // Simple check whether it has been normalized
  if (
    path.isEmpty() ||
    // `/`
    (path.length == 1 && path.first().isSeparator) ||
    // `foo`
    path.none { it.isSeparator || it.isDot || it.isTilde }
  ) return path.toString()

  // Just `////` to `/`
  if (path.all { it.isSeparator }) return SystemSeparator

  // Just change `~` or `~/` to `/userHome/`
  if (path.isUserHomeSymbol) return userHome

  // If it has prefixes, skip them when splitting
  val list = when {
    // In the case of `~/array`, will process to `/userHome/array`
    path.isInUserHomeDir -> ArrayList<CharSequence>(UserHome.length + path.length).apply {
      UserHome.splitTo(this) { it.isSeparator }
      path.splitTo(this, prefixLength) { it.isSeparator }
    }
    else -> path.splitBy(prefixLength) { it.isSeparator }
  }
  // If it has no root, do not process the beginning `../`, to as a relative path,
  // only parse parent symbols like of `/../../` when the value is `true`
  var canResolveParentSymbol = hasRoot

  val result = StringBuilder(path.length)
  var lastSegmentIndex: Int

  // Join all bytes and the flattened bytearray
  list.forEach { curSegment ->
    lastSegmentIndex = result.lastIndex
    when {
      curSegment.isEmpty() || curSegment.isDot -> {
        // Do nothing
      }
      curSegment.isTwoDot && (hasRoot || canResolveParentSymbol) -> {
        result.removeRange(lastSegmentIndex)
      }
      else -> {
        result.append(curSegment)
        result.append(SystemSeparator)
        // When the normal bytes appeared, the parent symbol can be resolved
        if (!canResolveParentSymbol && curSegment.isTwoDot.not()) canResolveParentSymbol = true
      }
    }
  }

  if (isDirectory.not() && path.last().isSeparator.not()) result.removeLast()

  return result.insert(0, path.substring(endIndex = prefixLength)).toString()
}

/** Resolve to current work dir and normalize its */
internal fun absolute(path: CharSequence, pathHasRoot: Boolean): String {
  if (pathHasRoot) return path.toString()
  val absolutely = cd(sourcePath = currentDir, newPath = path)
  // New root
  return detectPrefix(absolutely) { prefixLength, _, hasRoot ->
    normalize(absolutely, hasRoot, prefixLength)
  }
}

/** Returns the path string of the parent of the given [path]. */
internal fun getParent(
  path: CharSequence,
  noSeparator: Boolean,
  volumeLabelExists: Boolean,
  indexOfLastSeparator: Int,
): String? {
  fun lastSeparatorAt(index: Int) = indexOfLastSeparator == index

  if (path.size == 1) when (path.first()) {
    // Terminal path.
    Slash, Backslash, Dot -> return null
  }

  return when (path) {
    TwoDot, SlashTwoDot, BackslashTwoDot -> null
    else -> {
      when {
        // `C:\Windows\`
        lastSeparatorAt(2) && volumeLabelExists -> {
          if (path.size == 3) null // "C:\" has no parent.
          else path.substring(endIndex = 3) // Keep the trailing '\' in C:\.
        }
        // `C:file.txt`
        noSeparator && volumeLabelExists -> {
          if (path.size == 2) null // "C:" has no parent.
          else path.substring(endIndex = 2) // C: is volume-relative.
        }

        // `file.txt` is relative path without parent.
        noSeparator -> null

        // `\\server` is UNC path without parent.
        lastSeparatorAt(1) && path[0] == Backslash -> null

        // Parent is the filesystem root '/'.
        lastSeparatorAt(0) -> path.substring(endIndex = 1)

        // Previous `/`.
        else -> path.substring(endIndex = indexOfLastSeparator)
      }
    }
  }
}

/** Just like `cd` (aka. change directory) on the command line. */
internal fun cd(sourcePath: CharSequence, newPath: CharSequence): String {
  detectPrefix(newPath) { _, isRoot, hasRoot ->
    if (isRoot || hasRoot) return newPath.toString()
  }
  return buildString(sourcePath.length + newPath.length + 1) {
    append(sourcePath)
    if (sourcePath.last().isNotSeparator) append(SystemSeparator)
    append(newPath)
  }
}

/** Split the given [normalizedPath] separator. */
internal fun split(
  normalizedPath: String,
  prefixLength: Int,
): ArrayDeque<String> = normalizedPath.splitTo(
  destination = ArrayDeque<String>(normalizedPath.size),
  delimiter = SystemSeparatorChar,
  offset = prefixLength
).apply {
  // Restore prefix
  if (prefixLength > 0) addFirst(normalizedPath.substring(endIndex = prefixLength))
}

internal fun createRelativePath(
  source: CharSequence,
  sourceNormalized: String,
  sourcePrefixLength: Int,
  target: CharSequence,
): String {
  // Self path
  if (source == target) return CurrentRelativePath.toString()

  val sourceSegments = split(sourceNormalized, sourcePrefixLength)
  val sourcePrefix: String? = if (sourcePrefixLength > 0) sourceSegments.removeFirst() else null

  val targetPrefix: String?
  val targetSegments = detectPrefix(target) { targetPrefixLength, _, hasRoot ->
    normalize(target, hasRoot, targetPrefixLength)
      .also { if (sourceNormalized == it) return CurrentRelativePath.toString() } // Self path
      .let { split(normalizedPath = it, targetPrefixLength) }
      .also { targetPrefix = if (targetPrefixLength > 0) it.removeFirst() else null }
  }

  // Different prefix directly returns to the target
  if (targetPrefix != sourcePrefix) return target.toString()

  val maxSize = sourceSegments.size.coerceAtMost(targetSegments.size)
  var index = 0

  return buildString(maxSize) {
    while (index < maxSize) {
      val from = sourceSegments.getOrNull(index)
      val to = targetSegments.getOrNull(index)

      when {
        // `foo/` -> `foo/file.txt` = `file.txt`
        from.isNull() && to.isNotNull() -> append(to)

        // `foo/bar` -> `foo/` = `../`
        // `foo/bar/baz` -> `foo/file.txt` = `../../file.txt`
        from != to -> {
          append(TwoDot)
          append(SystemSeparatorChar)
        }
      }

      index++
    }
  }
}
