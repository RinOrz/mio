@file:Suppress("RESERVED_MEMBER_INSIDE_INLINE_CLASS", "NewApi")

package com.meowool.mio.internal

import com.meowool.mio.CopyErrorSolution
import com.meowool.mio.DeleteErrorSolution
import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.FileHandlingErrorSolution
import com.meowool.mio.MediaType
import com.meowool.mio.MoveErrorSolution
import com.meowool.mio.NioPath
import com.meowool.mio.NoSuchFileException
import com.meowool.mio.Path
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.SystemSeparator
import com.meowool.mio.resolve
import com.meowool.mio.resolveFileExtension
import com.meowool.mio.resolveSibling
import com.meowool.mio.toFile
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import com.meowool.sweekt.isAndroidSystem
import com.meowool.sweekt.isLinuxSystem
import com.meowool.sweekt.iteration.isNotNullEmpty
import com.meowool.sweekt.substringAfter
import com.meowool.sweekt.substringBefore
import com.meowool.sweekt.toReadableSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import java.io.File
import java.net.URLConnection

/**
 * The path backend implement with [File] to compatibility with lower version JDK or ANDROID.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi @JvmInline
internal value class FilePath(val file: File) : Path {
  override val absolute: Path
    get() = file.absoluteFile.toMioPath()

  override val real: Path
    get() = canonical

  override val canonical: Path
    get() = file.canonicalFile.toMioPath()

  override val isAbsolute: Boolean
    get() = file.isAbsolute

  override var name: String
    get() = file.name
    set(value) {
      val target = file.resolveSibling(value)
      if (target.exists()) throw PathAlreadyExistsException(
        path = target.toMioPath(),
        reason = "Cannot rename because the target file already exists!"
      )
      if (file.renameTo(target).not()) {
        // Another solution
        file.copyRecursively(target)
        if (file.deleteRecursively().not()) {
          file.walkBottomUp().forEach { it.deleteOnExit() }
        }
      }
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
    get() = file.parentFile?.toMioPath() ?: Path(
      file.absolutePath.let { path ->
        val lastIndex = path.indexOfLast { it == SystemSeparator[0] }
        path.substringBefore(lastIndex)
      }
    )
    set(value) {
      this.moveTo(this.resolveSibling(value))
    }

  override var lastModifiedTime: Long
    get() = file.lastModified()
    set(value) {
      file.setLastModified(value)
    }

  override var lastAccessTime: Long
    get() = lastModifiedTime
    set(value) {
      lastModifiedTime = value
    }

  override var creationTime: Long
    get() = lastModifiedTime
    set(value) {
      lastModifiedTime = value
    }

  override var isReadable: Boolean
    get() = file.canRead()
    set(value) {
      file.setReadable(value)
    }

  override var isWritable: Boolean
    get() = file.canWrite()
    set(value) {
      file.setWritable(value)
    }

  override var isExecutable: Boolean
    get() = file.canExecute()
    set(value) {
      file.setExecutable(value)
    }

  override var isHidden: Boolean
    get() = file.isHidden
    set(value) {
      when {
        value -> if (file.isHidden.not()) name = ".$name"
        name.first() == '.' -> name = name.substringAfter(1)
      }
    }

  override val isRegularFile: Boolean
    get() = file.isFile

  override val isDirectory: Boolean
    get() = file.isDirectory

  override val isSymbolicLink: Boolean
    get() = file.canonicalFile != file.absoluteFile

  override val isOther: Boolean
    get() = (isRegularFile && isDirectory && isSymbolicLink).not()

  override val size: Long
    get() = file.length()

  override val readableSize: String
    get() = size.toReadableSize()

  override val key: Any
    get() = "$absolute:$real:$size"

  override val contentType: String
    get() = when {
      isDirectory && isLinuxSystem -> MediaType.Directory.value[0]
      isDirectory && isAndroidSystem -> MediaType.Directory.value[1]
      else -> try {
        URLConnection.guessContentTypeFromName(file.name)
      } catch (e: Exception) {
        null
      }.orEmpty()
    }

  override fun createFile(): Path = if (exists()) this else createStrictFile()

  override fun createStrictFile(): Path {
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The '$this' already exists, it cannot be created again."
    )
    return file.apply { createNewFile() }.toMioPath()
  }

  override fun createDirectory(): Path = when {
    exists() -> if (isDirectory) this else throw PathAlreadyExistsException(
      this, reason = "The directory cannot be created because it already exists and is a file."
    )
    else -> file.apply { mkdir() }.toMioPath()
  }

  override fun createStrictDirectory(): Path {
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The '$this' already exists, it cannot be created again."
    )
    return file.apply { mkdir() }.toMioPath()
  }

  override fun createDirectories(): Path = file.apply { mkdirs() }.toMioPath()

  override fun delete(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> DeleteErrorSolution,
  ): Boolean {
    if (!filter(this)) return true

    return when {
      recursively -> file.walkBottomUp()
        .onFail { file, e -> if (onError(file.toMioPath(), e) == FileHandlingErrorSolution.Stop) throw e }
        .filter { filter(it.toMioPath()) }
        .all { it.delete() || it.exists().not() }
      else -> file.delete() || file.exists()
    }
  }

  override fun deleteStrictly(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> DeleteErrorSolution,
  ) {
    if (!filter(this)) return

    when {
      recursively -> file.walkBottomUp()
        .onFail { file, e -> if (onError(file.toMioPath(), e) == FileHandlingErrorSolution.Stop) throw e }
        .filter { filter(it.toMioPath()) }
        .all {
          if (it.exists().not()) onError(it.toMioPath(), NoSuchFileException(it.toMioPath()))
          if (it.isDirectory && it.listFiles().isNotNullEmpty())onError(it.toMioPath(), DirectoryNotEmptyException(it.toMioPath()))
          it.delete()
        }

      else -> {
        if (file.exists().not()) onError(this, NoSuchFileException(this))
        if (file.isDirectory && file.listFiles().isNotNullEmpty()) onError(this, DirectoryNotEmptyException(this))
        file.delete()
      }
    }
  }

  override fun isEmpty(): Boolean = when {
    // Check whether the contents of the directory are empty
    isDirectory -> file.listFiles().isNullOrEmpty()
    else -> file.bufferedReader().read() == -1
  }

  override fun isNotEmpty(): Boolean = !isEmpty()

  override fun exists(followLinks: Boolean): Boolean = if (followLinks) {
    file.canonicalFile.exists()
  } else {
    file.exists()
  }

  override fun notExists(followLinks: Boolean): Boolean = !exists(followLinks)

  override fun children(recursively: Boolean): Flow<Path> = flow {
    file.walkBottomUp()
  }

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

  override fun walk(maxDepth: Int): Flow<Path> = file.walk().maxDepth(maxDepth).map { it.toMioPath() }.asFlow()

  override fun copyTo(
    target: Path,
    overwrite: Boolean,
    recursively: Boolean,
    keepAttributes: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (Path, Throwable) -> CopyErrorSolution,
  ): Path {
    if (!filter(this)) return target

    when {
      recursively -> file.copyRecursively(target.toFile(), overwrite) { file, exception ->
        when(onError(file.toMioPath(), exception)) {
          FileHandlingErrorSolution.Skip -> OnErrorAction.SKIP
          FileHandlingErrorSolution.Stop -> OnErrorAction.TERMINATE
        }
      }
      else -> runCatching { file.copyTo(target.toFile(), overwrite) }.exceptionOrNull()?.let { onError(this, it) }
    }
    return target
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
  ): Path = copyTo(target, overwrite, recursively, keepAttributes, followLinks, filter, onError).also {
    if (recursively) {
      this.file.deleteRecursively()
    } else {
      this.file.delete()
    }
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

  override fun compareTo(other: Path): Int = file.compareTo(other.toFile())

  override fun compareTo(other: String): Int = file.path.compareTo(other)

  override val length: Int
    get() = file.path.length

  override fun get(index: Int): Char = file.path[index]

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
    file.path.subSequence(startIndex, endIndex)

  override fun toString(): String = file.path

  override fun hashCode(): Int = file.hashCode()

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (other is CharSequence) return file.path == other
    if (other is File) return file == other

    runCatching { if (other is NioPath) return this.toNioPath() == other }

    if (other !is Path) return false
    if (javaClass != other.javaClass) return false
    if (file != other.toFile()) return false

    return true
  }
}
