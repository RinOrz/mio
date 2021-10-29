@file:Suppress("RESERVED_MEMBER_INSIDE_INLINE_CLASS",
  "BlockingMethodInNonBlockingContext",
  "OVERRIDE_BY_INLINE",
  "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.Directory
import com.meowool.mio.IoFile
import com.meowool.mio.MediaType
import com.meowool.mio.NioPath
import com.meowool.mio.Path
import com.meowool.mio.IPath
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotDirectoryException
import com.meowool.mio.SystemSeparator
import com.meowool.mio.asDirectory
import com.meowool.mio.normalizedString
import com.meowool.mio.toIoFile
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import com.meowool.sweekt.isAndroidSystem
import com.meowool.sweekt.isLinuxSystem
import com.meowool.sweekt.substringAfter
import com.meowool.sweekt.toReadableSize
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.net.URLConnection

/**
 * The path backend implement with [IoFile].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal open class DefaultPathLegacy(final override var ioFile: IoFile) : IPath, IoFileBackend {

  override val absolute: IPath
    get() = absoluteImpl().toMioPath()

  override val real: IPath
    get() = realImpl().toMioPath()

  override val normalized: IPath
    get() = normalizedImpl().toMioPath()

  final override val isAbsolute: Boolean
    get() = ioFile.isAbsolute

  final override var name: String
    get() = ioFile.name
    set(value) {
      val target = ioFile.resolveSibling(value)
      if (target.exists()) throw PathAlreadyExistsException(
        path = target.toMioPath(),
        reason = "Cannot rename because the target path already exists!"
      )
      if (ioFile.renameTo(target).not()) {
        // Another solution
        ioFile.copyRecursively(target)
        if (ioFile.deleteRecursively().not()) {
          ioFile.walkBottomUp().forEach { it.deleteOnExit() }
        }
      }
      ioFile = target
    }

  final override val parent: Directory?
    get() = ioFile.parent?.toString()?.asDirectory()

  final override var lastModifiedTime: Long
    get() = ioFile.lastModified()
    set(value) {
      ioFile.setLastModified(value)
    }

  final override var lastAccessTime: Long
    get() = lastModifiedTime
    set(_) {}

  final override var creationTime: Long = 0

  override val symbolicLink: IPath
    get() = real

  final override val isRoot: Boolean
    get() = absolute.parent == null

  final override var isReadable: Boolean
    get() = ioFile.canRead()
    set(value) {
      ioFile.setReadable(value)
    }

  final override var isWritable: Boolean
    get() = ioFile.canWrite()
    set(value) {
      ioFile.setWritable(value)
    }

  final override var isExecutable: Boolean
    get() = ioFile.canExecute()
    set(value) {
      ioFile.setExecutable(value)
    }

  final override var isHidden: Boolean
    get() = ioFile.isHidden
    set(value) {
      when {
        value -> if (ioFile.isHidden.not()) name = ".$name"
        name.first() == '.' -> name = name.substringAfter(1)
      }
    }

  final override val isRegularFile: Boolean
    get() = exists() && ioFile.isFile

  final override val isDirectory: Boolean
    get() = exists() && ioFile.isDirectory

  final override val isSymbolicLink: Boolean
    get() = (ioFile.parent?.let { ioFile } ?: ioFile.parentFile!!.canonicalFile.resolve(ioFile.name)).let {
      it.canonicalFile != it.absoluteFile
    }

  final override val isOther: Boolean
    get() = (isRegularFile && isDirectory && isSymbolicLink).not()

  final override var size: Long
    get() = ioFile.length()
    set(value) {
      FileSystem.SYSTEM.openReadWrite(ioFile.toOkioPath()).resize(value)
    }

  final override val readableSize: String
    get() = size.toReadableSize()

  final override val contentType: String
    get() = when {
      isDirectory && isLinuxSystem -> MediaType.Directory.value[0]
      isDirectory && isAndroidSystem -> MediaType.Directory.value[1]
      else -> try {
        ioFile.toURI().toURL().openConnection().contentType
      } catch (e: Exception) {
        URLConnection.guessContentTypeFromName(ioFile.name)
      }.orEmpty()
    }

  final override val key: Any
    get() = "$absolute:$real:$size"

  final override fun exists(followLinks: Boolean): Boolean = when {
    followLinks -> ioFile.canonicalFile.exists()
    else -> ioFile.exists()
  }

  final override fun notExists(followLinks: Boolean): Boolean = exists(followLinks).not()

  override fun join(vararg paths: IPath): IPath =
    joinImpl(*paths).toMioPath()

  override fun join(vararg paths: CharSequence): IPath =
    joinImpl(*paths).toMioPath()

  override fun div(path: IPath): IPath = join(path)

  override fun div(path: CharSequence): IPath = join(path)

  override fun joinToParent(vararg paths: CharSequence): IPath =
    joinToParentImpl(*paths).toMioPath()

  override fun joinToParent(vararg paths: IPath): IPath =
    joinToParentImpl(*paths).toMioPath()

  override fun relativeTo(target: CharSequence): IPath =
    relativeToImpl(target).toMioPath()

  override fun relativeTo(target: IPath): IPath =
    relativeToImpl(target).toMioPath()

  override fun createParentDirectories(): IPath = apply {
    var parent = ioFile.parentFile
    while (parent != null) {
      if (parent.exists() && parent.isDirectory.not())
        throw PathExistsAndIsNotDirectoryException(parent.toMioPath())

      parent = parent.parentFile
    }
    parent?.parentFile?.normalize()?.mkdirs()
  }

  final override fun startsWith(path: IPath): Boolean =
    ioFile.normalize().startsWith(path.toIoFile())

  final override fun endsWith(path: IPath): Boolean =
    ioFile.normalize().endsWith(path.toIoFile())

  override fun toReal(followLinks: Boolean): IPath = toRealImpl(followLinks).toMioPath()

  final override fun split(): List<String> = this.normalizedString.split(SystemSeparator)

  final override fun <R : IPath> linkTo(target: R): R {
    throw UnsupportedOperationException("Not yet implemented")
  }

  final override fun <R : IPath> linkSymbolTo(target: R): R {
    throw UnsupportedOperationException("Not yet implemented")
  }

  final override fun compareTo(other: IPath): Int =
    ioFile.normalize().compareTo(other.toIoFile().normalize())

  final override fun compareTo(otherPath: String): Int =
    ioFile.normalize().compareTo(File(otherPath).normalize())

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other is CharSequence) return normalizedString == Path(other).normalizedString
    if (other is IoFile) return ioFile.normalize() == other.normalize()
    runCatching {
      @Suppress("NewApi")
      if (other is NioPath) return toNioPath().normalize() == other.normalize()
    }
    if (other !is IPath) return false
    if (normalizedString != other.normalizedString) return false

    return true
  }

  final override fun hashCode(): Int = ioFile.normalize().hashCode()

  final override fun toString(): String = ioFile.toString()

  protected inline fun absoluteImpl(): IoFile = ioFile.normalize().absoluteFile

  protected inline fun realImpl(): IoFile = ioFile.normalize().canonicalFile

  protected inline fun normalizedImpl(): IoFile = ioFile.normalize()

  protected inline fun joinImpl(vararg paths: IPath): IoFile = paths.fold(ioFile) { acc, path ->
    acc.resolve(path.toIoFile())
  }

  protected inline fun joinImpl(vararg paths: CharSequence): IoFile = paths.fold(ioFile) { acc, path ->
    acc.resolve(path.toString())
  }

  protected inline fun joinToParentImpl(vararg paths: CharSequence): IoFile =
    ioFile.resolveSibling(paths.fold(IoFile("")) { acc, path ->
      acc.resolve(path.toString())
    })

  protected inline fun joinToParentImpl(vararg paths: IPath): IoFile =
    ioFile.resolveSibling(paths.fold(IoFile("")) { acc, path ->
      acc.resolve(path.toIoFile())
    })

  protected inline fun relativeToImpl(target: CharSequence): IoFile =
    IoFile(target.toString()).relativeTo(ioFile)

  protected inline fun relativeToImpl(target: IPath): IoFile =
    target.toIoFile().relativeTo(ioFile)

  protected inline fun toRealImpl(followLinks: Boolean): IoFile = when {
    followLinks -> ioFile.normalize().canonicalFile
    else -> ioFile.normalize()
  }
}