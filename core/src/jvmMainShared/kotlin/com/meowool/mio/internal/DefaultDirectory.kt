@file:Suppress("NewApi")

package com.meowool.mio.internal

import com.meowool.mio.CopyErrorSolution
import com.meowool.mio.DeleteErrorSolution
import com.meowool.mio.Directory
import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.File
import com.meowool.mio.IFile
import com.meowool.mio.NioPath
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.Path
import com.meowool.mio.IPath
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotDirectoryException
import com.meowool.mio.PathHandlingErrorSolution
import com.meowool.mio.SystemSeparator
import com.meowool.mio.Zip
import com.meowool.mio.toMioDirectory
import com.meowool.mio.toMioFile
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import com.meowool.sweekt.iteration.isEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.name
import kotlin.streams.toList

/**
 * The directory backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal open class DefaultDirectory(path: NioPath) : Directory, DefaultPath(path) {
  override val absolute: Directory
    get() = absoluteImpl().toMioDirectory()

  override val real: Directory
    get() = realImpl().toMioDirectory()

  override val normalized: Directory
    get() = normalizedImpl().toMioDirectory()

  override val symbolicLink: Directory
    get() = symbolicLinkImpl().toMioDirectory()

  override val totalSize: Long
    get() = Files.walk(nioPath.normalize())
      .mapToLong { runCatching { Files.size(it) }.getOrElse { 0 } }
      .sum()

  override fun flow(depth: Int): Flow<IPath> = streamFlow {
    when (depth) {
      1 -> Files.list(nioPath.normalize())
      else -> Files.walk(nioPath.normalize(), depth).filter { it != nioPath.normalize() }
    }
  }.map(::Path)

  override fun list(depth: Int): List<IPath> = when (depth) {
    1 -> Files.list(nioPath.normalize())
    else -> Files.walk(nioPath.normalize(), depth).filter { it != nioPath.normalize() }
  }.map(::Path).toList()

  override fun walk(
    depth: Int,
    walkDirs: Boolean,
    walkFiles: Boolean,
    followLinks: Boolean,
    filterDirs: (Directory) -> Boolean,
    filterFiles: (IFile) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> PathHandlingErrorSolution,
    onEnterDirectory: (Directory) -> Unit,
    onLeaveDirectory: (Directory) -> Unit,
    onVisitFile: (IFile) -> Unit,
  ): List<IPath> = mutableListOf<IPath>().also { paths ->
    Files.walkFileTree(
      nioPath.normalize(),
      setOfNotNull(if (followLinks) FileVisitOption.FOLLOW_LINKS else null),
      depth,
      object : SimpleFileVisitor<NioPath>() {
        override fun preVisitDirectory(dir: NioPath, attrs: BasicFileAttributes): FileVisitResult {
          if (walkDirs.not()) return FileVisitResult.CONTINUE
          val mio = Directory(dir)
          return runCatching {
            when (filterDirs(mio)) {
              true -> {
                paths.add(mio)
                onEnterDirectory(mio)
                FileVisitResult.CONTINUE
              }
              false -> FileVisitResult.SKIP_SUBTREE
            }
          }.getOrElse { getErrorResult(mio, it) }
        }

        override fun visitFile(file: NioPath, attrs: BasicFileAttributes?): FileVisitResult {
          if (walkFiles.not()) return FileVisitResult.CONTINUE
          val mio = File(file)
          return runCatching {
            if (filterFiles(mio)) {
              paths.add(mio)
              onVisitFile(mio)
            }
            FileVisitResult.CONTINUE
          }.getOrElse { getErrorResult(mio, it) }
        }

        override fun visitFileFailed(file: NioPath, exc: IOException): FileVisitResult =
          getErrorResult(Path(file), exc)

        override fun postVisitDirectory(dir: NioPath, exc: IOException?): FileVisitResult {
          val mio = Directory(dir)
          return runCatching {
            onLeaveDirectory(mio)
            when (exc) {
              null -> FileVisitResult.CONTINUE
              else -> getErrorResult(mio, exc)
            }
          }.getOrElse { getErrorResult(mio, it) }
        }

        fun getErrorResult(path: IPath, throwable: Throwable) = when (onError(path, throwable)) {
          PathHandlingErrorSolution.Skip -> FileVisitResult.CONTINUE
          PathHandlingErrorSolution.Stop -> throw throwable
        }
      }
    )
  }

  override fun isEmpty(): Boolean = Files.newDirectoryStream(nioPath).use { it.isEmpty() }

  override fun isNotEmpty(): Boolean = isEmpty().not()

  override fun addFile(subpath: String, overwrite: Boolean): IFile {
    require(subpath[0] != SystemSeparator[0]) { "The subpath to be added to the directory cannot has a root!" }
    return this.joinFile(subpath).create(overwrite)
  }

  override fun addDirectory(subpath: String, overwrite: Boolean): Directory {
    require(subpath[0] != SystemSeparator[0]) { "The subpath to be added to the directory cannot has a root!" }
    return this.joinDir(subpath).create(overwrite)
  }

  override fun add(
    subdirectory: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> PathHandlingErrorSolution
  ): Directory = when {
    keepSources -> subdirectory.copyInto(
      target = this, recursively, overwrite, followLinks, filter, onError
    )
    else -> subdirectory.moveInto(
      target = this, recursively, overwrite, followLinks, filter, onError
    )
  }

  override fun add(
    subfile: IFile,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean
  ): IFile = when {
    keepSources -> subfile.copyInto(
      target = this, overwrite, followLinks
    )
    else -> subfile.moveInto(
      target = this, overwrite, followLinks
    )
  }

  override fun addAll(
    vararg subdirectories: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> PathHandlingErrorSolution
  ): List<Directory> = subdirectories.map {
    add(it, recursively, overwrite, keepSources, followLinks, filter, onError)
  }

  override fun addAll(
    subdirectories: Iterable<Directory>,
    recursively: Boolean,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> PathHandlingErrorSolution
  ): List<Directory> = subdirectories.map {
    add(it, recursively, overwrite, keepSources, followLinks, filter, onError)
  }

  override fun addAll(
    subdirectories: Sequence<Directory>,
    recursively: Boolean,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> PathHandlingErrorSolution
  ): Sequence<Directory> = subdirectories.map {
    add(it, recursively, overwrite, keepSources, followLinks, filter, onError)
  }

  override fun addAll(
    vararg subfiles: IFile,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean
  ): List<IFile> = subfiles.map {
    add(it, overwrite, keepSources, followLinks)
  }

  override fun addAll(
    subfiles: Iterable<IFile>,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean
  ): List<IFile> = subfiles.map {
    add(it, overwrite, keepSources, followLinks)
  }

  override fun addAll(
    subfiles: Sequence<IFile>,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
  ): Sequence<IFile> = subfiles.map {
    add(it, overwrite, keepSources, followLinks)
  }

  override fun addTempFile(prefix: String?, suffix: String?): IFile =
    Files.createTempFile(nioPath, prefix, suffix).toMioFile()

  override fun find(name: String, recursively: Boolean): IPath? = when {
    recursively -> runCatching {
      Files.walk(nioPath).filter { it.name == name }.findFirst().get().toMioPath()
    }.getOrNull()
    else -> runCatching {
      Files.list(nioPath).filter { it.name == name }.findFirst().get().toMioPath()
    }.getOrNull()
  }

  override fun contains(subpath: String): Boolean =
    join(name).run { exists(followLinks = false) && isDirectory }

  override fun get(name: String, recursively: Boolean): IPath = find(name, recursively)
    ?: throw NoSuchElementException("Cannot find a file or directory matching the name `$name`")

  override fun get(vararg names: String, recursively: Boolean): List<IPath> = when {
    recursively -> Files.walk(nioPath).filter { it.name == name }.map(::Path).toList()
    else -> Files.list(nioPath).filter { it.name == name }.map(::Path).toList()
  }

  override fun create(overwrite: Boolean): Directory = apply {
    if (this.exists() && this.isRegularFile) throw PathExistsAndIsNotDirectoryException(this)
    if (this.exists() && overwrite) deleteRecursively()
    createParentDirectories()
    if (this.exists().not()) Files.createDirectory(nioPath.normalize())
  }

  override fun createStrictly(overwrite: Boolean): Directory = apply {
    if (this.exists()) {
      if (this.isRegularFile) throw PathExistsAndIsNotDirectoryException(this)
      if (this.isNotEmpty()) throw DirectoryNotEmptyException(this)
      if (overwrite) deleteRecursively()
    }
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The directory already exists, it cannot be created again."
    )
    var parentPath = this.parent
    while (true) {
      if (parentPath?.exists() == false) throw ParentDirectoryNotExistsException(parentPath)
      parentPath = parentPath?.parent ?: break
    }
    Files.createDirectory(nioPath.normalize())
  }

  override fun copyTo(
    target: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> CopyErrorSolution,
  ): Directory = copyOrMoveDir(
    isMove = false, source = this,
    target, recursively, overwrite, followLinks, filter, onError
  )

  override fun copyInto(
    target: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> CopyErrorSolution,
  ): Directory = this.copyTo(
    target.joinDir(this.name),
    recursively, overwrite, followLinks, filter, onError
  )

  override fun copyInto(
    target: Zip,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> CopyErrorSolution,
  ): Directory = this.copyTo(
    target.joinDir(this.name),
    recursively, overwrite, followLinks, filter, onError
  )

  override fun moveTo(
    target: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> CopyErrorSolution,
  ): Directory = copyOrMoveDir(
    isMove = true, source = this,
    target, recursively, overwrite, followLinks, filter, onError
  )

  override fun moveInto(
    target: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> CopyErrorSolution,
  ): Directory = this.moveTo(
    target.joinDir(this.name),
    recursively, overwrite, followLinks, filter, onError
  )

  override fun moveInto(
    target: Zip,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> CopyErrorSolution,
  ): Directory = this.moveTo(
    target.joinDir(this.name),
    recursively, overwrite, followLinks, filter, onError
  )

  override fun delete(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> DeleteErrorSolution,
  ): Boolean = deleteDir(source = this, recursively, followLinks, filter, onError) {
    Files.deleteIfExists(it.toNioPath())
  }

  override fun deleteStrictly(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> DeleteErrorSolution,
  ): Boolean = deleteDir(source = this, recursively, followLinks, filter, onError) {
    Files.delete(it.toNioPath())
    null
  }

  override fun clear(
    recursively: Boolean,
    followLinks: Boolean,
    onError: (path: IPath, throwable: Throwable) -> DeleteErrorSolution,
  ): Boolean = deleteDir(source = this, recursively, followLinks, filter = { true }, onError) {
    if (it != this) Files.delete(it.toNioPath())
    null
  }

  override fun createParentDirectories(): Directory = apply { super.createParentDirectories() }

  override fun toReal(followLinks: Boolean): Directory = toRealImpl(followLinks).toMioDirectory()
}