package com.meowool.io.internal

import com.meowool.io.*
import com.meowool.io.Path
import com.meowool.sweekt.isAndroidSystem
import com.meowool.sweekt.isLinuxSystem
import com.meowool.sweekt.substringAfter
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.fileAttributesView
import kotlin.io.path.fileAttributesViewOrNull
import kotlin.io.path.readSymbolicLink

/**
 * The default implementation of Mio base path based on [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal abstract class BasePathImpl<Actual : BasePath<Actual>>(nioPath: NioPath) :
  CommonPath<Actual>(nioPath.toString()) {
  var nioPath: NioPath = nioPath
    private set

  private val attributeView get() = nioPath.fileAttributesView<BasicFileAttributeView>()
  private val attributeDosView get() = nioPath.fileAttributesViewOrNull<DosFileAttributeView>()
  private val attributePosixView get() = nioPath.fileAttributesViewOrNull<PosixFileAttributeView>()

  private val attributes get() = attributeView.readAttributes()

  override val real: Actual get() = nioPath.toRealPath().materialize()
  override val symbolicLink: Actual get() = nioPath.readSymbolicLink().materialize()

  override val absolute: Actual
    get() = absoluteString.materialize()

  override val normalized: Actual
    get() = normalizedString.materialize()

  override val parent: Directory?
    get() = parentString?.materialize()?.asDir()

  override var lastModifiedTime: Long
    get() = attributes.lastModifiedTime().toMillis()
    set(value) = attributeView.setTimes(FileTime.fromMillis(value), null, null)

  override var lastModifiedInstant: Instant
    get() = attributes.lastModifiedTime().toInstant().toKotlinInstant()
    set(value) = attributeView.setTimes(FileTime.from(value.toJavaInstant()), null, null)

  override var lastAccessTime: Long
    get() = attributes.lastAccessTime().toMillis()
    set(value) = attributeView.setTimes(null, FileTime.fromMillis(value), null)

  override var lastAccessInstant: Instant
    get() = attributes.lastAccessTime().toInstant().toKotlinInstant()
    set(value) = attributeView.setTimes(null, FileTime.from(value.toJavaInstant()), null)

  override var creationTime: Long
    get() = attributes.creationTime().toMillis()
    set(value) = attributeView.setTimes(null, null, FileTime.fromMillis(value))

  override var creationInstant: Instant
    get() = attributes.creationTime().toInstant().toKotlinInstant()
    set(value) = attributeView.setTimes(null, null, FileTime.from(value.toJavaInstant()))

  override var isReadable: Boolean
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

  override var isWritable: Boolean
    get() = Files.isWritable(nioPath.normalize())
    set(value) {
      isReadable = !value
    }

  override var isExecutable: Boolean
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

  override var isHidden: Boolean
    get() = Files.isHidden(nioPath.normalize())
    set(value) {
      attributeDosView?.setHidden(value) ?: run {
        when {
          value -> if (isHidden.not()) name = ".$name"
          name.first() == '.' -> name = name.substringAfter(1)
        }
      }
    }

  override val isRegularFile: Boolean
    get() = exists() && attributes.isRegularFile

  override val isDirectory: Boolean
    get() = exists() && attributes.isDirectory

  override val isSymbolicLink: Boolean
    get() = exists() && attributes.isSymbolicLink

  override val isOther: Boolean
    get() = exists() && attributes.isOther

  override var size: Long
    get() = attributes.size()
    set(value) {
      if (isRegularFile) TODO()
    }

  override val contentType: String
    get() = when {
      isDirectory && isLinuxSystem -> MediaType.Directory.value[0]
      isDirectory && isAndroidSystem -> MediaType.Directory.value[1]
      else -> runCatching { Files.probeContentType(nioPath.normalize()) }.getOrNull().orEmpty()
    }

  override val key: Any?
    get() = attributes.fileKey()

  override fun exists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.exists(nioPath.normalize())
    else -> Files.exists(nioPath.normalize(), LinkOption.NOFOLLOW_LINKS)
  }

  override fun notExists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.notExists(nioPath.normalize())
    else -> Files.notExists(nioPath.normalize(), LinkOption.NOFOLLOW_LINKS)
  }

  override fun createParentDirectories(): Actual = self {
    val parent = nioPath.parent ?: return@self
    try {
      Files.createDirectories(parent)
    } catch (e: FileAlreadyExistsException) {
      val path = Path(e.file)
      if (path.exists() && path.isDirectory.not())
        throw PathExistsAndIsNotDirectoryException(path)
    }
  }

  override fun toReal(followLinks: Boolean): Actual = when {
    followLinks -> nioPath.toRealPath()
    else -> nioPath.toRealPath(LinkOption.NOFOLLOW_LINKS)
  }.materialize()

  override fun startsWith(path: Path): Boolean = nioPath.normalize().startsWith(path.normalizedString)

  override fun endsWith(path: Path): Boolean = nioPath.normalize().endsWith(path.normalizedString)

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

  override fun join(vararg paths: CharSequence): Path = joinAsString(*paths).materialize()

  override fun join(vararg paths: Path): Path = joinAsString(*paths).materialize()

  override fun div(path: CharSequence): Path = joinAsString(path).materialize()

  override fun div(path: Path): Path = joinAsString(path).materialize()

  override fun relativeTo(target: CharSequence): Path = relativeToAsString(target).materialize()

  override fun rename(new: CharSequence) {
    val newPath = nioPath.resolveSibling(new.toString())
    nioPath = Files.move(nioPath, newPath)
    repath(nioPath.toString())
  }

  private fun CharSequence.materialize(): Actual =
    nioPath.fileSystem.getPath(this.toString()).materialize()

  protected abstract fun NioPath.materialize(): Actual
}