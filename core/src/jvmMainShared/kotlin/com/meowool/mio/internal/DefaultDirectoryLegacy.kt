package com.meowool.mio.internal

import com.meowool.mio.CopyErrorSolution
import com.meowool.mio.DeleteErrorSolution
import com.meowool.mio.Directory
import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.File
import com.meowool.mio.IFile
import com.meowool.mio.IoFile
import com.meowool.mio.NioPath
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.Path
import com.meowool.mio.IPath
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotDirectoryException
import com.meowool.mio.PathHandlingErrorSolution
import com.meowool.mio.SystemSeparator
import com.meowool.mio.Zip
import com.meowool.mio.toIoFile
import com.meowool.mio.toMioDirectory
import com.meowool.mio.toMioFile
import com.meowool.sweekt.iteration.isNotNullEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * The directory backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class DefaultDirectoryLegacy(path: IoFile) : Directory, DefaultPathLegacy(path) {
  override val absolute: Directory
    get() = absoluteImpl().toMioDirectory()

  override val real: Directory
    get() = realImpl().toMioDirectory()

  override val normalized: Directory
    get() = normalizedImpl().toMioDirectory()

  override val symbolicLink: Directory
    get() = real

  override val totalSize: Long
    get() = listRecursively().sumOf { it.size }

  override fun flow(depth: Int): Flow<IPath> = when (depth) {
    1 -> ioFile.normalize().list()!!.map(::Path).asFlow()
    else -> ioFile.normalize().walk().maxDepth(depth).filterNot { it == ioFile.normalize() }.map(::Path).asFlow()
  }

  override fun list(depth: Int): List<IPath> = when (depth) {
    1 -> ioFile.normalize().list()!!.map(::Path)
    else -> ioFile.normalize().walk().maxDepth(depth).filterNot { it == ioFile.normalize() }.map(::Path).toList()
  }

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

    fun getErrorResult(path: IPath, throwable: Throwable) = when (onError(path, throwable)) {
      PathHandlingErrorSolution.Skip -> true
      PathHandlingErrorSolution.Stop -> throw throwable
    }

    ioFile.walk().maxDepth(depth)
      .onFail { file, ioException -> getErrorResult(Path(file), ioException) }
      .onEnter {
        val mio = Directory(it)
        when {
          walkDirs.not() -> false
          it.isDirectory -> runCatching {
            val result = filterDirs(mio)
            if (result) {
              paths.add(mio)
              onEnterDirectory(mio)
            }
            result
          }.getOrElse { e ->
            getErrorResult(mio, e)
          }
          else -> true
        }
      }.onLeave {
        if (it.isDirectory) {
          val mio = Directory(it)
          runCatching { onLeaveDirectory(mio) }.getOrElse { e ->
            getErrorResult(mio, e)
          }
        }
      }.forEach {
        if (it.isFile.not() || walkFiles.not()) return@forEach
        val mio = File(it)
        runCatching {
          if (filterFiles(mio)) {
            paths.add(mio)
            onVisitFile(mio)
          }
        }.getOrElse { e -> getErrorResult(mio, e) }
      }
  }

  override fun isEmpty(): Boolean = ioFile.list().isNullOrEmpty()

  override fun isNotEmpty(): Boolean = ioFile.list().isNotNullEmpty()

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
    IoFile.createTempFile(prefix ?: "TEMP-", suffix).toMioFile()

  override fun find(name: String, recursively: Boolean): IPath? = when {
    recursively -> listRecursively().find { it.name == name }
    else -> list().find { it.name == name }
  }

  override fun contains(subpath: String): Boolean =
    join(subpath).run { exists(followLinks = false) && isDirectory }

  override fun get(name: String, recursively: Boolean): IPath = find(name, recursively)
    ?: throw NoSuchElementException("Cannot find a file or directory matching the name `$name`")

  override fun get(vararg names: String, recursively: Boolean): List<IPath> = when {
    recursively -> listRecursively().filter { it.name == name }
    else -> list().filter { it.name == name }
  }

  override fun create(overwrite: Boolean): Directory = apply {
    if (this.exists() && this.isRegularFile) throw PathExistsAndIsNotDirectoryException(this)
    if (this.exists() && overwrite) deleteRecursively()
    createParentDirectories()
    if (this.exists().not()) ioFile.normalize().mkdir()
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
    ioFile.normalize().mkdir()
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
  ): Directory  = this.moveTo(
    target.joinDir(this.name),
    recursively, overwrite, followLinks, filter, onError
  )

  override fun delete(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> DeleteErrorSolution,
  ): Boolean = deleteDir(source = this, recursively, followLinks, filter, onError) {
    it.toIoFile().delete()
  }

  override fun deleteStrictly(
    recursively: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> DeleteErrorSolution,
  ): Boolean = delete(recursively, followLinks, filter, onError)

  override fun clear(
    recursively: Boolean,
    followLinks: Boolean,
    onError: (path: IPath, throwable: Throwable) -> DeleteErrorSolution,
  ): Boolean = deleteDir(source = this, recursively, followLinks, filter = { true }, onError) {
    if (it != this) it.toIoFile().delete()
    null
  }

  override fun createParentDirectories(): Directory = apply { super.createParentDirectories() }

  override fun toReal(followLinks: Boolean): Directory = toRealImpl(followLinks).toMioDirectory()
}