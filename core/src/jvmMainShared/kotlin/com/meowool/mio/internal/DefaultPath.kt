@file:Suppress("NewApi",
  "RESERVED_MEMBER_INSIDE_INLINE_CLASS",
  "BlockingMethodInNonBlockingContext",
  "OVERRIDE_BY_INLINE",
  "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.Directory
import com.meowool.mio.LinkAlreadyExistsException
import com.meowool.mio.MediaType
import com.meowool.mio.NioPath
import com.meowool.mio.Path
import com.meowool.mio.IPath
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotDirectoryException
import com.meowool.mio.asDirectory
import com.meowool.mio.getAttributeView
import com.meowool.mio.getBasicAttributeView
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import com.meowool.sweekt.isAndroidSystem
import com.meowool.sweekt.isLinuxSystem
import com.meowool.sweekt.substringAfter
import com.meowool.sweekt.toReadableSize
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.exists

/**
 * The path backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal open class DefaultPath(final override var nioPath: NioPath) : IPath, NioPathBackend {
  private val attributeView get() = nioPath.getBasicAttributeView()
  private val attributeDosView get() = nioPath.getAttributeView<DosFileAttributeView?>()
  private val attributePosixView get() = nioPath.getAttributeView<PosixFileAttributeView?>()

  private val attributes get() = attributeView.readAttributes()

  override val absolute: IPath
    get() = absoluteImpl().toMioPath()

  override val real: IPath
    get() = realImpl().toMioPath()

  override val normalized: IPath
    get() = normalizedImpl().toMioPath()

  final override val isAbsolute: Boolean
    get() = nioPath.isAbsolute

  final override var name: String
    get() = nioPath.fileName?.toString().orEmpty()
    set(value) {
      val target = nioPath.resolveSibling(value)
      if (target.exists()) throw PathAlreadyExistsException(
        path = target.toMioPath(),
        reason = "Cannot rename because the target path already exists!"
      )
      nioPath = Files.move(nioPath, target)
    }

  final override val parent: Directory?
    get() = nioPath.parent?.toString()?.asDirectory()

  final override var lastModifiedTime: Long
    get() = attributes.lastModifiedTime().toMillis()
    set(value) {
      attributeView.setTimes(FileTime.fromMillis(value), null, null)
    }

  final override var lastAccessTime: Long
    get() = attributes.lastAccessTime().toMillis()
    set(value) {
      attributeView.setTimes(null, FileTime.fromMillis(value), null)
    }

  final override var creationTime: Long
    get() = attributes.creationTime().toMillis()
    set(value) {
      attributeView.setTimes(null, null, FileTime.fromMillis(value))
    }

  override val symbolicLink: IPath
    get() = symbolicLinkImpl().toMioPath()

  final override val isRoot: Boolean
    get() = absolute.parent == null

  final override var isReadable: Boolean
    get() = Files.isReadable(nioPath.normalize())
    set(value) {
      attributePosixView?.let {
        val permissions = it.readAttributes().permissions().apply {
          remove(PosixFilePermission.OWNER_WRITE)
          remove(PosixFilePermission.GROUP_WRITE)
          remove(PosixFilePermission.OTHERS_WRITE)
          if (!value) {
            add(PosixFilePermission.OWNER_WRITE)
            add(PosixFilePermission.OWNER_WRITE)
            add(PosixFilePermission.OWNER_WRITE)
          }
        }
        it.setPermissions(permissions)
      } ?: attributeDosView?.setReadOnly(value)
    }

  final override var isWritable: Boolean
    get() = Files.isWritable(nioPath.normalize())
    set(value) {
      isReadable = !value
    }

  final override var isExecutable: Boolean
    get() = Files.isExecutable(nioPath.normalize())
    set(value) {
      val permissions = attributePosixView?.readAttributes()?.permissions()?.apply {
        remove(PosixFilePermission.OWNER_EXECUTE)
        remove(PosixFilePermission.GROUP_EXECUTE)
        remove(PosixFilePermission.OTHERS_EXECUTE)
        if (value) {
          add(PosixFilePermission.OWNER_EXECUTE)
          add(PosixFilePermission.GROUP_EXECUTE)
          add(PosixFilePermission.OTHERS_EXECUTE)
        }
      }
      attributePosixView?.setPermissions(permissions)
    }

  final override var isHidden: Boolean
    get() = Files.isHidden(nioPath.normalize())
    set(value) {
      attributeDosView?.setHidden(value) ?: run {
        when {
          value -> if (isHidden.not()) name = ".$name"
          name.first() == '.' -> name = name.substringAfter(1)
        }
      }
    }

  final override val isRegularFile: Boolean
    get() = exists() && attributes.isRegularFile

  final override val isDirectory: Boolean
    get() = exists() && attributes.isDirectory

  final override val isSymbolicLink: Boolean
    get() = exists() && attributes.isSymbolicLink

  final override val isOther: Boolean
    get() = exists() && attributes.isOther

  final override var size: Long
    get() = attributes.size()
    set(value) {
      if (isRegularFile) FileSystem.SYSTEM.openReadWrite(nioPath.toOkioPath()).resize(value)
    }

  final override val readableSize: String
    get() = size.toReadableSize()

  final override val contentType: String
    get() = when {
      isDirectory && isLinuxSystem -> MediaType.Directory.value[0]
      isDirectory && isAndroidSystem -> MediaType.Directory.value[1]
      else -> runCatching { Files.probeContentType(nioPath.normalize()) }.getOrNull().orEmpty()
    }

  final override val key: Any?
    get() = attributes.fileKey()

  final override fun exists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.exists(nioPath.normalize())
    else -> Files.exists(nioPath.normalize(), LinkOption.NOFOLLOW_LINKS)
  }

  final override fun notExists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.notExists(nioPath.normalize())
    else -> Files.notExists(nioPath.normalize(), LinkOption.NOFOLLOW_LINKS)
  }

  override fun join(vararg paths: IPath): IPath = nioPath.join(*paths).toMioPath()

  override fun join(vararg paths: CharSequence): IPath = nioPath.join(*paths).toMioPath()

  override fun div(path: IPath): IPath = join(path)

  override fun div(path: CharSequence): IPath = join(path)

  override fun joinToParent(vararg paths: CharSequence): IPath =
    nioPath.joinToParent(*paths).toMioPath()

  override fun joinToParent(vararg paths: IPath): IPath =
    nioPath.joinToParent(*paths).toMioPath()

  override fun relativeTo(target: CharSequence): IPath =
    nioPath.relativeTo(target).toMioPath()

  override fun relativeTo(target: IPath): IPath =
    nioPath.relativeTo(target).toMioPath()

  override fun createParentDirectories(): IPath = apply {
    val parent = nioPath.parent ?: return@apply
    try {
      Files.createDirectories(parent)
    } catch (e: FileAlreadyExistsException) {
      val path = Path(e.file)
      if (path.exists() && path.isDirectory.not())
        throw PathExistsAndIsNotDirectoryException(path)
    }
  }

  final override fun startsWith(path: IPath): Boolean = nioPath.startsWith(path)

  final override fun endsWith(path: IPath): Boolean = nioPath.endsWith(path)

  override fun toReal(followLinks: Boolean): IPath = toRealImpl(followLinks).toMioPath()

  final override fun split(): List<String> = nioPath.split()

  final override fun <R : IPath> linkTo(target: R): R {
    try {
      Files.createLink(nioPath.normalize(), target.normalized.toNioPath())
    } catch (e: FileAlreadyExistsException) {
      throw LinkAlreadyExistsException(target)
    }
    return target
  }

  final override fun <R : IPath> linkSymbolTo(target: R): R {
    try {
      Files.createSymbolicLink(nioPath.normalize(), target.normalized.toNioPath())
    } catch (e: FileAlreadyExistsException) {
      throw PathAlreadyExistsException(Path(e.file), e.otherFile?.let(::Path), e.reason)
    }
    return target
  }

  final override fun compareTo(other: IPath): Int = nioPath.compareTo(other)

  final override fun compareTo(otherPath: String): Int = nioPath.compareTo(otherPath)

  override fun equals(other: Any?): Boolean = equalsTo(other)

  final override fun hashCode(): Int = nioPath.hashCode()

  final override fun toString(): String = nioPath.toString()
}