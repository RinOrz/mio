package com.meowool.mio.internal

import com.meowool.mio.IPath
import com.meowool.mio.IoFile
import com.meowool.mio.LinkAlreadyExistsException
import com.meowool.mio.MediaType
import com.meowool.mio.NioPath
import com.meowool.mio.Path
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotDirectoryException
import com.meowool.mio.getAttributeView
import com.meowool.mio.getBasicAttributeView
import com.meowool.mio.readBasicAttributes
import com.meowool.mio.toNioPath
import com.meowool.sweekt.isAndroidSystem
import com.meowool.sweekt.isLinuxSystem
import com.meowool.sweekt.substringAfter
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSameFileAs
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.readSymbolicLink
import kotlin.io.path.setLastModifiedTime

/**
 * The path backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@Suppress("NewApi", "UNCHECKED_CAST", "EqualsOrHashCode")
internal abstract class PathImpl<Self : IPath<Self>>(chars: CharSequence) :
  CommonPath<Self>(chars),
  NioPathBackend {
  @Volatile private var _nioPath: NioPath? = null

  private val attributes get() = nioPath.readBasicAttributes()
  private val attributeView get() = nioPath.getBasicAttributeView()
  private val attributeDosView get() = nioPath.getAttributeView<DosFileAttributeView?>()
  private val attributePosixView get() = nioPath.getAttributeView<PosixFileAttributeView?>()

  override val nioPath: NioPath
    get() = _nioPath ?: NioPath(normalizedString).also { _nioPath = it }

  override val real: Self
    get() = nioPath.toRealPath().toString().produce()

  override val symbolicLink: Self
    get() = nioPath.readSymbolicLink().toString().produce()

  override var lastModifiedTime: Long
    get() = nioPath.getLastModifiedTime().toMillis()
    set(value) {
      nioPath.setLastModifiedTime(FileTime.fromMillis(value))
    }

  override var lastAccessTime: Long
    get() = attributes.lastAccessTime().toMillis()
    set(value) {
      attributeView.setTimes(null, FileTime.fromMillis(value), null)
    }

  override var creationTime: Long
    get() = attributes.creationTime().toMillis()
    set(value) {
      attributeView.setTimes(null, null, FileTime.fromMillis(value))
    }

  override var isReadable: Boolean
    get() = Files.isReadable(nioPath)
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

  override var isWritable: Boolean
    get() = Files.isWritable(nioPath)
    set(value) {
      isReadable = !value
    }

  override var isExecutable: Boolean
    get() = Files.isExecutable(nioPath)
    set(value) {
      val permissions = Files.getPosixFilePermissions(nioPath).apply {
        remove(PosixFilePermission.OWNER_EXECUTE)
        remove(PosixFilePermission.GROUP_EXECUTE)
        remove(PosixFilePermission.OTHERS_EXECUTE)
        if (value) {
          add(PosixFilePermission.OWNER_EXECUTE)
          add(PosixFilePermission.GROUP_EXECUTE)
          add(PosixFilePermission.OTHERS_EXECUTE)
        }
      }
      Files.setPosixFilePermissions(nioPath, permissions)
    }

  override var isHidden: Boolean
    get() = nioPath.isHidden()
    set(value) {
      attributeDosView?.setHidden(value) ?: run {
        when {
          value -> {
            if (isHidden.not()) name = "$Dot$name"
          }
          name.first() == Dot -> {
            name = name.substringAfter(1)
          }
        }
      }
    }

  override val isRegularFile: Boolean
    get() = this.exists() && nioPath.isRegularFile()

  override val isDirectory: Boolean
    get() = this.exists() && nioPath.isDirectory()

  override val isSymbolicLink: Boolean
    get() = this.exists() && nioPath.isSymbolicLink()

  override val isOther: Boolean
    get() = this.exists() && attributes.isOther

  override var size: Long
    get() = attributes.size()
    set(value) {
      if (isRegularFile) Files.newByteChannel(nioPath).use {
        it.position(value).truncate(value)
      }
    }

  override val contentType: String
    get() = when {
      isDirectory && isLinuxSystem -> MediaType.Directory.value[0]
      isDirectory && isAndroidSystem -> MediaType.Directory.value[1]
      else -> runCatching { Files.probeContentType(nioPath.normalize()) }.getOrNull().orEmpty()
    }

  override val key: Any
    get() = attributes.fileKey() ?: normalizedString

  override fun exists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.exists(nioPath)
    else -> Files.exists(nioPath, LinkOption.NOFOLLOW_LINKS)
  }

  override fun notExists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.exists(nioPath)
    else -> Files.exists(nioPath, LinkOption.NOFOLLOW_LINKS)
  }

  override fun createParentDirectories(): Self = apply {
    val parent = nioPath.parent ?: return@apply
    try {
      Files.createDirectories(parent)
    } catch (e: FileAlreadyExistsException) {
      val path = Path(e.file)
      if (path.exists() && path.isDirectory.not())
        throw PathExistsAndIsNotDirectoryException(path)
    }
  } as Self

  override fun toReal(followLinks: Boolean): Self = when {
    followLinks -> nioPath.toRealPath()
    else -> nioPath.toRealPath(LinkOption.NOFOLLOW_LINKS)
  }.toString().produce()

  override fun <R : Path> linkTo(target: R): R {
    try {
      Files.createLink(nioPath, target.normalized.toNioPath())
    } catch (e: FileAlreadyExistsException) {
      throw LinkAlreadyExistsException(target)
    }
    return target
  }

  override fun <R : Path> linkSymbolTo(target: R): R {
    try {
      Files.createSymbolicLink(nioPath, target.normalized.toNioPath())
    } catch (e: FileAlreadyExistsException) {
      throw PathAlreadyExistsException(Path(e.file), e.otherFile?.let(::Path), e.reason)
    }
    return target
  }

  override fun rename(new: CharSequence) {
    val newPath = nioPath.resolveSibling(new.toString())
    repath(Files.move(nioPath, newPath).toString())
  }

  override fun isSameAs(other: Path?): Boolean =
    super.isSameAs(other) && nioPath.isSameFileAs(other!!.toNioPath())

  override fun equals(other: Any?): Boolean {
    val equals = super.equals(other)
    if (equals) return true
    if (other is IoFile) return this.normalizedString == Path(other).normalizedString
    runCatching {
      if (other is NioPath) return this.normalizedString == Path(other).normalizedString
    }
    return false
  }
}