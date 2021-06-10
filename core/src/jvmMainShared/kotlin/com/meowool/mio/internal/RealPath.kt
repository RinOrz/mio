@file:Suppress("RESERVED_MEMBER_INSIDE_INLINE_CLASS", "NewApi")

package com.meowool.mio.internal

import com.meowool.mio.CopyErrorSolution
import com.meowool.mio.DeleteErrorSolution
import com.meowool.mio.MediaType
import com.meowool.mio.MoveErrorSolution
import com.meowool.mio.NioPath
import com.meowool.mio.Path
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.getAttributeView
import com.meowool.mio.getBasicAttributeView
import com.meowool.mio.resolve
import com.meowool.mio.resolveFileExtension
import com.meowool.mio.resolveSibling
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import com.meowool.sweekt.coroutines.flowOnIO
import com.meowool.sweekt.isAndroidSystem
import com.meowool.sweekt.isLinuxSystem
import com.meowool.sweekt.iteration.isEmpty
import com.meowool.sweekt.toReadableSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.DosFileAttributeView
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.util.stream.Stream
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists

/**
 * The path backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi @JvmInline
internal value class RealPath(val path: NioPath) : Path {
  private val attributeView get() = path.getBasicAttributeView()
  private val attributeDosView get() = path.getAttributeView<DosFileAttributeView?>()
  private val attributePosixView get() = path.getAttributeView<PosixFileAttributeView?>()

  private val attributes get() = attributeView.readAttributes()

  override val absolute: Path
    get() = path.toAbsolutePath().toMioPath()

  override val real: Path
    get() = path.toRealPath().toMioPath()

  override val canonical: Path
    get() = path.normalize().toMioPath()

  override val isAbsolute: Boolean
    get() = path.isAbsolute

  override var name: String
    get() = path.fileName?.toString().orEmpty()
    set(value) {
      val target = path.resolveSibling(value)
      if (target.exists()) throw PathAlreadyExistsException(
        path = target.toMioPath(),
        reason = "Cannot rename because the target file already exists!"
      )
      Files.move(path, path.resolveSibling(value))
    }

  override var extension: String
    get() = resolveFileExtension(name)
    set(value) {
      name = "$nameWithoutExtension.$value"
    }

  override val extensionWithDot: String
    get() = resolveFileExtension(name, withDot = true)

  override var nameWithoutExtension: String
    get() = name.removeSuffix(extensionWithDot)
    set(value) {
      val extension = extension
      name = "$value.$extension"
    }

  override var parent: Path
    get() = path.parent.toMioPath()
    set(value) {
      this.moveTo(this.resolveSibling(value))
    }

  override var lastModifiedTime: Long
    get() = attributes.lastModifiedTime().toMillis()
    set(value) {
      attributeView.setTimes(FileTime.fromMillis(value), null, null)
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
    get() = Files.isReadable(path)
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
    get() = Files.isWritable(path)
    set(value) {
      isReadable = !value
    }

  override var isExecutable: Boolean
    get() = Files.isExecutable(path)
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
    get() = Files.isHidden(path)
    set(value) {
      attributeDosView?.setHidden(value) ?: run {
        name = ".$name"
      }
    }

  override val isRegularFile: Boolean
    get() = attributes.isRegularFile

  override val isDirectory: Boolean
    get() = attributes.isDirectory

  override val isSymbolicLink: Boolean
    get() = attributes.isSymbolicLink

  override val isOther: Boolean
    get() = attributes.isOther

  override val size: Long
    get() = attributes.size()

  override val readableSize: String
    get() = size.toReadableSize()

  override val key: Any?
    get() = attributes.fileKey()

  override val contentType: String
    get() = when {
      isDirectory && isLinuxSystem -> MediaType.Directory.value[0]
      isDirectory && isAndroidSystem -> MediaType.Directory.value[1]
      else -> try {
        Files.probeContentType(path)
      } catch (e: Exception) {
        null
      }.orEmpty()
    }

  override val length: Int
    get() = path.toString().length

  override fun createFile(): Path = if (exists()) this else createStrictFile()

  override fun createStrictFile(): Path {
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The '$this' already exists, it cannot be created again."
    )
    return Files.createFile(path).toMioPath()
  }

  override fun createDirectory(): Path = when {
    exists() -> if (isDirectory) this else throw PathAlreadyExistsException(
      this, reason = "The directory cannot be created because it already exists and is a file."
    )
    else -> Files.createDirectory(path).toMioPath()
  }

  override fun createStrictDirectory(): Path {
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The '$this' already exists, it cannot be created again."
    )
    return Files.createDirectory(path).toMioPath()
  }

  override fun createDirectories(): Path = Files.createDirectories(path).toMioPath()

  override fun delete(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> DeleteErrorSolution,
  ): Boolean = when (recursively) {
    false -> deleteFile(
      isStrict = false,
      site = this,
      followLinks = followLinks,
      filter = filter,
      onError = onError
    )
    else -> DeleteRecursivelyVisitor(
      isStrict = false,
      followLinks = followLinks,
      filter = filter,
      onError = onError
    ).also {
      Files.walkFileTree(absolute.toNioPath(), it)
    }.successful
  }

  override fun deleteStrictly(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> DeleteErrorSolution,
  ) {
    when (recursively) {
      false -> deleteFile(
        isStrict = true,
        site = this,
        followLinks = followLinks,
        filter = filter,
        onError = onError
      )
      else -> DeleteRecursivelyVisitor(
        isStrict = true,
        followLinks = followLinks,
        filter = filter,
        onError = onError
      ).also {
        Files.walkFileTree(absolute.toNioPath(), it)
      }.successful
    }
  }

  override fun isEmpty(): Boolean = when {
    // Check whether the contents of the directory are empty
    isDirectory -> Files.newDirectoryStream(this.toNioPath()).use { it.isEmpty() }
    else -> this.toNioPath().bufferedReader().read() == -1
  }

  override fun isNotEmpty(): Boolean = !isEmpty()

  override fun exists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.exists(path)
    else -> Files.exists(path, LinkOption.NOFOLLOW_LINKS)
  }

  override fun notExists(followLinks: Boolean): Boolean = when {
    followLinks -> Files.notExists(path)
    else -> Files.notExists(path, LinkOption.NOFOLLOW_LINKS)
  }

  override fun children(recursively: Boolean): Flow<Path> =
    if (recursively) descendants() else pathSiteFlow { Files.list(this.toNioPath()) }

  override fun descendants(maxDepth: Int): Flow<Path> {
    var firstMatch = false
    return walk(maxDepth).mapNotNull {
      // Remove itself
      if (it == this && !firstMatch) {
        firstMatch = true
        null
      } else it
    }
  }

  override fun walk(maxDepth: Int): Flow<Path> = pathSiteFlow { Files.walk(this.toNioPath(), maxDepth) }

  override fun copyTo(
    target: Path,
    overwrite: Boolean,
    recursively: Boolean,
    keepAttributes: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> CopyErrorSolution,
  ): Path {
    val targetPath = when {
      recursively && isDirectory -> Files.walkFileTree(
        absolute.toNioPath(), CopyMoveRecursivelyVisitor(
          isMove = false, source = this,
          target, overwrite, keepAttributes, followLinks, filter, onError
        )
      ).let { target.toNioPath() }
      else -> copyOrMoveFile(
        isMove = false, source = this,
        target, overwrite, keepAttributes, followLinks, filter, onError
      )
    }

    return targetPath.toMioPath()
  }

  override fun copyInto(
    targetDirectory: Path,
    overwrite: Boolean,
    recursively: Boolean,
    keepAttributes: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> CopyErrorSolution,
  ): Path = copyTo(
    targetDirectory.resolve(name),
    overwrite, recursively, keepAttributes, followLinks, filter, onError
  )

  override fun moveTo(
    target: Path,
    overwrite: Boolean,
    recursively: Boolean,
    keepAttributes: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> MoveErrorSolution,
  ): Path {
    val targetPath = when (recursively) {
      recursively && isDirectory -> Files.walkFileTree(
        absolute.toNioPath(), CopyMoveRecursivelyVisitor(
          isMove = true, source = this,
          target, overwrite, keepAttributes, followLinks, filter, onError
        )
      ).let { target.toNioPath() }
      else -> copyOrMoveFile(
        isMove = true, source = this,
        target, overwrite, keepAttributes, followLinks, filter, onError
      )
    }

    return targetPath.toMioPath()
  }

  override fun moveInto(
    targetDirectory: Path,
    overwrite: Boolean,
    recursively: Boolean,
    keepAttributes: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> MoveErrorSolution,
  ): Path = moveTo(
    targetDirectory.resolve(name),
    overwrite, recursively, keepAttributes, followLinks, filter, onError
  )

  override fun get(index: Int): Char = path.toString()[index]

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
    path.toString().subSequence(startIndex, endIndex)

  override fun compareTo(other: Path): Int = path.compareTo(other.toNioPath())

  override fun compareTo(other: String): Int = path.toString().compareTo(other)

  override fun toString(): String = path.toString()

  override fun hashCode(): Int = path.hashCode()

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other is CharSequence) return path.toString() == other
    if (other is File) return path.toFile() == other
    if (other is NioPath) return path == other
    if (other !is Path) return false
    if (javaClass != other.javaClass) return false
    if (path != other.toNioPath()) return false

    return true
  }
}

private inline fun pathSiteFlow(
  crossinline creator: () -> Stream<NioPath>,
) = streamFlow { creator().map { it.toMioPath() } }.flowOnIO()