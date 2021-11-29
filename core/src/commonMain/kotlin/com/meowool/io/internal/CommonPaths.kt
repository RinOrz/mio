@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.io.internal

import com.meowool.io.BasePath
import com.meowool.io.Directory
import com.meowool.io.Path
import com.meowool.io.asDir
import com.meowool.sweekt.LazyInit
import com.meowool.sweekt.isEnglishNotPunctuation
import com.meowool.sweekt.resetLazyValues
import com.meowool.sweekt.size
import com.meowool.sweekt.substring
import kotlin.jvm.Volatile

/**
 * A common pure path backend, no need to rely on any file system implementation.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal abstract class CommonPath<Actual : BasePath<Actual>>(private var chars: CharSequence) :
  BasePath<Actual> {
  @Volatile private var _hasRoot: Boolean = false
  @Volatile private var _isRoot: Boolean = false
  @Volatile private var _prefixLength: Int = 0

  @LazyInit protected val pathString = chars.toString()
  @LazyInit private val hashCode = pathString.hashCode()
  @LazyInit private val indexOfLastSeparator: Int = chars.lastIndexOf(Slash)
    .ifTrue({ it == -1 }) { chars.lastIndexOf(Backslash) }

  @LazyInit private val volumeLabelExists: Boolean = chars.size >= 2
    && chars[1] == Colon
    && chars[0].code.toChar().isEnglishNotPunctuation()

  @LazyInit private val prefixLength: Int = detectPrefix(chars) { prefixLength, isRoot, hasRoot ->
    _isRoot = isRoot
    _hasRoot = hasRoot
    _prefixLength = prefixLength
    prefixLength
  }

  @LazyInit override var name: String = when {
    noSeparator -> chars.substring(startIndex = indexOfLastSeparator + 1)
    volumeLabelExists && chars.size == 2 -> "" // "C:" has no name.
    else -> chars.toString()
  }
    set(value) = rename(value)

  @LazyInit override val absoluteString: String = absolute(chars, hasRoot)

  // 1.Converts all separators to system flavor
  // 2.Converts user home symbol (`~`) to real path of user home directory
  // 3.Removes the duplicate slash like `//` but will protect UNC path
  // 4.Removes the all useless single dot like `./`
  // 5.Resolves the all parent paths symbols like `..`
  @LazyInit override val normalizedString: String = normalize(
    chars,
    hasRoot,
    prefixLength,
    isDirectory
  )

  @LazyInit override val parentString: String? = getParent(
    chars,
    noSeparator,
    volumeLabelExists,
    indexOfLastSeparator
  )

  private val noSeparator: Boolean get() = indexOfLastSeparator == -1

  override val volumeLabel: String?
    // Just like `C:`
    get() = when (volumeLabelExists) {
      true -> chars.substring(endIndex = 1)
      else -> null
    }

  override val hasRoot: Boolean get() = run { prefixLength; _hasRoot }

  override val isRoot: Boolean get() = run { prefixLength; _isRoot }

  override fun split(): List<String> = split(normalizedString, prefixLength)

  override fun isSameAs(other: Path?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    if (this != other) return false
    if (this.toString() != other.toString()) return false
    return true
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null) return false
    if (other is Path) {
      if (other is CommonPath<*>) {
        if (chars != other.chars) return false
        if (absoluteString != other.absoluteString) return false
        if (normalizedString != other.normalizedString) return false
        if (name != other.name) return false
        if (parentString != other.parentString) return false
        if (_hasRoot != other._hasRoot) return false
        if (_isRoot != other._isRoot) return false
        if (_prefixLength != other._prefixLength) return false
        if (volumeLabelExists != other.volumeLabelExists) return false
        if (indexOfLastSeparator != other.indexOfLastSeparator) return false
        if (hashCode != other.hashCode) return false
      }
      if (this.pathString == other.toString()) return true
      return this.normalizedString == other.normalizedString
    }
    if (other is CharSequence) {
      if (chars == other) return true
      return this.normalizedString == Path(other).normalizedString
    }
    return false
  }

  override fun compareTo(other: Actual): Int = pathString.compareTo(other.toString())

  override fun compareTo(otherPath: String): Int = pathString.compareTo(otherPath)

  override fun hashCode(): Int = hashCode

  final override inline fun toString(): String = pathString

  protected fun joinAsString(vararg paths: Path) = paths.fold(pathString) { joined, path ->
    cd(sourcePath = joined, newPath = path.toString())
  }

  protected fun joinAsString(vararg paths: CharSequence) = paths.reduce(::cd).toString()

  protected fun joinAsString(path: Path) = cd(sourcePath = chars, newPath = path.toString())

  protected fun joinAsString(path: CharSequence) = cd(sourcePath = chars, newPath = path)

  protected fun relativeToAsString(target: CharSequence) =
    createRelativePath(chars, normalizedString, prefixLength, target)

  protected fun repath(newPath: CharSequence) {
    chars = newPath
    // reset
    _hasRoot = false
    _isRoot = false
    _prefixLength = 0
    resetLazyValues(pathString, hashCode, indexOfLastSeparator, volumeLabelExists, prefixLength)
  }

  @Suppress("UNCHECKED_CAST")
  protected inline fun self(block: () -> Unit): Actual = apply { block() } as Actual

  abstract fun rename(new: CharSequence)
}