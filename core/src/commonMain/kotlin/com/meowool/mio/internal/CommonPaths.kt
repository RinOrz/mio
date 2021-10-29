@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.Directory
import com.meowool.mio.IPath
import com.meowool.mio.Path
import com.meowool.mio.asDir
import com.meowool.sweekt.ifNull
import com.meowool.sweekt.isEnglishNotPunctuation
import com.meowool.sweekt.size
import com.meowool.sweekt.substring
import com.meowool.sweekt.synchronized
import kotlin.jvm.Volatile


/**
 * A common pure path backend, no need to rely on any file system implementation.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal abstract class CommonPath<Self : IPath<Self>>(private var chars: CharSequence) :
  IPath<Self> {

  override val absolute: Self
    get() = absoluteString.produce()

  override val absoluteString: String
    get() = _absoluteString ?: synchronized {
      absolute(chars, hasRoot)
    }

  override val normalized: Self
    get() = normalizedString.produce()

  // 1.Converts all separators to system flavor
  // 2.Converts user home symbol (`~`) to real path of user home directory
  // 3.Removes the duplicate slash like `//` but will protect UNC path
  // 4.Removes the all useless single dot like `./`
  // 5.Resolves the all parent paths symbols like `..`
  override val normalizedString: String
    get() = _normalizedString ?: synchronized {
      normalize(chars, hasRoot, prefixLength, isDirectory).also { _normalizedString = it }
    }

  override var name: String
    get() = _name ?: when {
      noSeparator -> chars.substring(startIndex = indexOfLastSeparator + 1)
      volumeLabelExists && chars.size == 2 -> "" // "C:" has no name.
      else -> chars.toString()
    }.also { _name = it }
    set(value) {
      rename(value)
    }

  override val volumeLabel: String?
    // Just like `C:`
    get() = when (volumeLabelExists) {
      true -> chars.substring(endIndex = 1)
      else -> null
    }

  override val parentString: String?
    get() = _parentString ?: getParent(
      chars, noSeparator, volumeLabelExists, indexOfLastSeparator
    ).also { _parentString = it }

  override val parent: Directory?
    get() = parentString?.produce()?.asDir()

  override val hasRoot: Boolean get() = _hasRoot.ifBoolPlaceholder { prefixLength; _hasRoot }

  override val isRoot: Boolean get() = _isRoot.ifBoolPlaceholder { prefixLength; _isRoot }

  override fun join(vararg paths: Path): Path = paths.fold(chars) { joined, path ->
    cd(sourcePath = joined, newPath = path.toString())
  }.produce()

  override fun join(vararg paths: CharSequence): Path = paths.reduce(::cd).produce()

  override fun div(path: Path): Path = cd(sourcePath = chars, newPath = path.toString()).produce()

  override fun div(path: CharSequence): Path = cd(sourcePath = chars, newPath = path).produce()

  override fun relativeTo(target: CharSequence): Path =
    createRelativePath(chars, normalizedString, prefixLength, target) { it.produce() }

  final override inline fun relativeTo(target: Path): Path = relativeTo(target.toString())

  override fun split(): List<String> = split(normalizedString, prefixLength)

  override fun startsWith(path: Path): Boolean {
    TODO("Not yet implemented")
  }

  override fun endsWith(path: Path): Boolean {
    TODO("Not yet implemented")
  }

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
        if (_absoluteString != other._absoluteString) return false
        if (_normalizedString != other._normalizedString) return false
        if (_name != other._name) return false
        if (_parentString != other._parentString) return false
        if (_hasRoot != other._hasRoot) return false
        if (_isRoot != other._isRoot) return false
        if (_prefixLength != other._prefixLength) return false
        if (_volumeLabelExists != other._volumeLabelExists) return false
        if (_indexOfLastSeparator != other._indexOfLastSeparator) return false
        if (_hashCode != other._hashCode) return false
      }
      if (this.toString() == other.toString()) return true
      return this.normalizedString == other.normalizedString
    }
    if (other is CharSequence) {
      if (chars == other) return true
      return this.normalizedString == Path(other).normalizedString
    }
    return false
  }

  override fun compareTo(other: Self): Int = this.toString().compareTo(other.toString())

  override fun compareTo(otherPath: String): Int = this.toString().compareTo(otherPath)

  override fun hashCode(): Int = _hashCode.ifPlaceholder {
    this.toString().hashCode().also { _hashCode = it }
  }

  final override inline fun toString(): String = chars.toString()


  ////////////////////////////////////////////////////////////////////
  ////                   Internal implementation                  ////
  ////////////////////////////////////////////////////////////////////

  @Volatile private var _absoluteString: String? = null
  @Volatile private var _normalizedString: String? = null
  @Volatile private var _name: String? = null
  @Volatile private var _parentString: String? = null
  @Volatile private var _hasRoot: Int = IntPlaceholder
  @Volatile private var _isRoot: Int = IntPlaceholder
  @Volatile private var _prefixLength: Int = IntPlaceholder
  @Volatile private var _volumeLabelExists: Int = IntPlaceholder
  @Volatile private var _indexOfLastSeparator: Int = IntPlaceholder
  @Volatile private var _hashCode: Int = IntPlaceholder

  private val indexOfLastSeparator: Int
    get() = _indexOfLastSeparator.ifPlaceholder {
      chars.lastIndexOf(Slash)
        .ifPlaceholder { chars.lastIndexOf(Backslash) }
        .also { _indexOfLastSeparator = it }
    }

  private val noSeparator: Boolean get() = indexOfLastSeparator == -1

  private val volumeLabelExists: Boolean
    get() = _volumeLabelExists.ifBoolPlaceholder {
      val exists = chars.size >= 2 &&
        chars[1] == Colon &&
        chars[0].code.toChar().isEnglishNotPunctuation()
      _volumeLabelExists = exists.toInt()
      exists
    }

  private val prefixLength: Int
    get() = _prefixLength.ifPlaceholder {
      detectPrefix(chars) { prefixLength, isRoot, hasRoot ->
        _isRoot = isRoot.toInt()
        _hasRoot = hasRoot.toInt()
        _prefixLength = prefixLength
        prefixLength
      }
    }

  protected fun repath(newPath: CharSequence) {
    chars = newPath
    // reset
    _absoluteString = null
    _normalizedString = null
    _name = null
    _parentString = null
    _hasRoot = IntPlaceholder
    _isRoot = IntPlaceholder
    _prefixLength = IntPlaceholder
    _volumeLabelExists = IntPlaceholder
    _indexOfLastSeparator = IntPlaceholder
    _hashCode = IntPlaceholder
  }

  @Suppress("UNCHECKED_CAST")
  protected inline fun self(block: () -> Unit): Self = apply { block() } as Self

  abstract fun rename(new: CharSequence)

  /**
   * Produces a new [Self] instance based on the path
   */
  abstract fun CharSequence.produce(): Self
}