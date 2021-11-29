@file:Suppress("BlockingMethodInNonBlockingContext")

package com.meowool.io.internal

import com.meowool.io.BaseDirectory
import com.meowool.io.CopyErrorSolution
import com.meowool.io.DeleteErrorSolution
import com.meowool.io.Directory
import com.meowool.io.DirectoryNotEmptyException
import com.meowool.io.File
import com.meowool.io.IOException
import com.meowool.io.NioPath
import com.meowool.io.NoSuchPathException
import com.meowool.io.ParentDirectoryNotExistsException
import com.meowool.io.Path
import com.meowool.io.PathAlreadyExistsException
import com.meowool.io.PathExistsAndIsNotDirectoryException
import com.meowool.io.PathHandlingErrorSolution
import com.meowool.io.PathList
import com.meowool.io.SystemSeparator
import com.meowool.io.asDir
import com.meowool.io.asPath
import com.meowool.io.toMioFile
import com.meowool.io.toNioPath
import com.meowool.sweekt.castOrNull
import com.meowool.sweekt.coroutines.flowOnIO
import com.meowool.sweekt.coroutines.withIOContext
import com.meowool.sweekt.run
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.streams.toList
import java.nio.file.DirectoryNotEmptyException as NioDirectoryNotEmptyException
import java.nio.file.NoSuchFileException as NioNoSuchFileException

/**
 * The default implementation of Mio file based on [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class DirectoryImpl(nioPath: NioPath) :
  BasePathImpl<DirectoryImpl>(nioPath),
  BaseDirectory<Path, File, DirectoryImpl, DirectoryImpl> {
  override val totalSize: Long
    get() = Files.walk(nioPath.normalize())
      .mapToLong { run(catching = { 0 }) { Files.size(it) } }
      .sum()

  override fun create(): DirectoryImpl = also {
    if (it.exists() && it.isRegularFile) throw PathExistsAndIsNotDirectoryException(it)
    if (it.exists()) throw PathAlreadyExistsException(it)
    createParentDirectories()
    if (it.exists().not()) Files.createDirectory(nioPath.normalize())
  }

  override suspend fun create(overwrite: Boolean): DirectoryImpl = also {
    withIOContext {
      if (it.exists() && it.isRegularFile) throw PathExistsAndIsNotDirectoryException(it)
      if (it.exists() && overwrite) deleteRecursively()
      createParentDirectories()
      if (it.exists().not()) Files.createDirectory(nioPath.normalize())
    }
  }

  override fun addTempFile(prefix: String?, suffix: String?): File =
    Files.createTempFile(nioPath, prefix, suffix).toMioFile()

  override fun flow(depth: Int): Flow<Path> = streamFlow {
    when (depth) {
      1 -> Files.list(nioPath.normalize())
      else -> Files.walk(nioPath.normalize(), depth).filter { it != nioPath.normalize() }
    }
  }.map(::Path).flowOnIO()

  override fun list(depth: Int): List<Path> = when (depth) {
    1 -> Files.list(nioPath.normalize())
    else -> Files.walk(nioPath.normalize(), depth).filter { it != nioPath.normalize() }
  }.map(::Path).toList()

  override suspend fun walk(
    depth: Int,
    walkDirs: Boolean,
    walkFiles: Boolean,
    followLinks: Boolean,
    filterDirs: suspend (DirectoryImpl) -> Boolean,
    filterFiles: suspend (File) -> Boolean,
    onError: suspend (path: Path, throwable: Throwable) -> PathHandlingErrorSolution,
    onEnterDirectory: suspend (DirectoryImpl) -> Unit,
    onLeaveDirectory: suspend (DirectoryImpl) -> Unit,
    onVisitFile: suspend (File) -> Unit
  ): List<Path> = withIOContext {
    buildList {
      @Suppress("BlockingMethodInNonBlockingContext")
      Files.walkFileTree(
        nioPath.normalize(),
        setOfNotNull(if (followLinks) FileVisitOption.FOLLOW_LINKS else null),
        depth,
        object : SimpleFileVisitor<NioPath>() {
          override fun preVisitDirectory(
            dir: NioPath,
            attrs: BasicFileAttributes
          ): FileVisitResult = runBlocking {
            when {
              walkDirs -> DirectoryImpl(dir).let { mio ->
                run(catching = { getErrorResult(mio, it) }) {
                  when (filterDirs(mio)) {
                    true -> {
                      add(mio)
                      onEnterDirectory(mio)
                      FileVisitResult.CONTINUE
                    }
                    false -> FileVisitResult.SKIP_SUBTREE
                  }
                }
              }
              else -> FileVisitResult.CONTINUE
            }
          }

          override fun visitFile(
            file: NioPath,
            attrs: BasicFileAttributes?
          ): FileVisitResult = runBlocking {
            when {
              walkFiles -> File(file).let { mio ->
                run(catching = { getErrorResult(mio, it) }) {
                  if (filterFiles(mio)) {
                    add(mio)
                    onVisitFile(mio)
                  }
                  FileVisitResult.CONTINUE
                }
              }
              else -> FileVisitResult.CONTINUE
            }
          }

          override fun visitFileFailed(
            file: NioPath,
            exc: IOException
          ): FileVisitResult = runBlocking { getErrorResult(Path(file), exc) }

          override fun postVisitDirectory(
            dir: NioPath,
            exc: IOException?
          ): FileVisitResult = runBlocking {
            val mio = DirectoryImpl(dir)
            run(catching = { getErrorResult(mio, it) }) {
              onLeaveDirectory(mio)
              when (exc) {
                null -> FileVisitResult.CONTINUE
                else -> getErrorResult(mio, exc)
              }
            }
          }

          suspend fun getErrorResult(
            path: Path,
            throwable: Throwable
          ) = when (onError(path, throwable)) {
            PathHandlingErrorSolution.Skip -> FileVisitResult.CONTINUE
            PathHandlingErrorSolution.Stop -> throw throwable
          }
        }
      )
    }
  }

  override fun walkBlocking(
    depth: Int,
    walkDirs: Boolean,
    walkFiles: Boolean,
    followLinks: Boolean,
    filterDirs: (DirectoryImpl) -> Boolean,
    filterFiles: (File) -> Boolean,
    onError: (path: Path, throwable: Throwable) -> PathHandlingErrorSolution,
    onEnterDirectory: (DirectoryImpl) -> Unit,
    onLeaveDirectory: (DirectoryImpl) -> Unit,
    onVisitFile: (File) -> Unit
  ): List<Path> = buildList {
    Files.walkFileTree(
      nioPath.normalize(),
      setOfNotNull(if (followLinks) FileVisitOption.FOLLOW_LINKS else null),
      depth,
      object : SimpleFileVisitor<NioPath>() {
        override fun preVisitDirectory(
          dir: NioPath,
          attrs: BasicFileAttributes
        ): FileVisitResult = when {
          walkDirs -> DirectoryImpl(dir).let { mio ->
            run(catching = { getErrorResult(mio, it) }) {
              when (filterDirs(mio)) {
                true -> {
                  add(mio)
                  onEnterDirectory(mio)
                  FileVisitResult.CONTINUE
                }
                false -> FileVisitResult.SKIP_SUBTREE
              }
            }
          }
          else -> FileVisitResult.CONTINUE
        }

        override fun visitFile(
          file: NioPath,
          attrs: BasicFileAttributes?
        ): FileVisitResult = when {
          walkFiles -> File(file).let { mio ->
            run(catching = { getErrorResult(mio, it) }) {
              if (filterFiles(mio)) {
                add(mio)
                onVisitFile(mio)
              }
              FileVisitResult.CONTINUE
            }
          }
          else -> FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(file: NioPath, exc: IOException): FileVisitResult =
          getErrorResult(Path(file), exc)

        override fun postVisitDirectory(dir: NioPath, exc: IOException?): FileVisitResult {
          val mio = DirectoryImpl(dir)
          return run(catching = { getErrorResult(mio, it) }) {
            onLeaveDirectory(mio)
            when (exc) {
              null -> FileVisitResult.CONTINUE
              else -> getErrorResult(mio, exc)
            }
          }
        }

        fun getErrorResult(path: Path, throwable: Throwable) = when (onError(path, throwable)) {
          PathHandlingErrorSolution.Skip -> FileVisitResult.CONTINUE
          PathHandlingErrorSolution.Stop -> throw throwable
        }
      }
    )
  }

  override suspend fun addFile(subpath: String, overwrite: Boolean): File {
    require(subpath[0] != SystemSeparator[0]) { "The subpath to be added to the directory cannot has a root!" }
    return this.joinFile(subpath).create(overwrite)
  }

  override suspend fun addDirectory(subpath: String, overwrite: Boolean): DirectoryImpl {
    require(subpath[0] != SystemSeparator[0]) { "The subpath to be added to the directory cannot has a root!" }
    val result = this.joinDir(subpath).create(overwrite)
    return result.castOrNull() ?: DirectoryImpl(result.toNioPath())
  }

  override suspend fun <T : PathList> copyTo(
    target: T,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (path: Path, throwable: Throwable) -> CopyErrorSolution
  ): T = copyOrMoveDir(
    isMove = false, source = this,
    target, recursively, overwrite, followLinks, filter, onError
  )

  override suspend fun copyInto(
    target: PathList,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (path: Path, throwable: Throwable) -> CopyErrorSolution
  ): Directory = copyTo(
    target.joinDir(this.name),
    recursively, overwrite, followLinks, filter, onError
  )

  override suspend fun <T : PathList> moveTo(
    target: T,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (path: Path, throwable: Throwable) -> CopyErrorSolution
  ): T = copyOrMoveDir(
    isMove = true, source = this,
    target, recursively, overwrite, followLinks, filter, onError
  )

  override suspend fun moveInto(
    target: PathList,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (path: Path, throwable: Throwable) -> CopyErrorSolution
  ): Directory = moveTo(
    target.joinDir(this.name),
    recursively, overwrite, followLinks, filter, onError
  )

  override suspend fun delete(
    recursively: Boolean,
    followLinks: Boolean,
    filter: suspend (Path) -> Boolean,
    onError: suspend (path: Path, throwable: Throwable) -> DeleteErrorSolution
  ): Boolean = deleteDir(source = this, recursively, followLinks, filter, onError) {
    Files.deleteIfExists(it.toNioPath())
  }

  override suspend fun deleteStrictly(
    recursively: Boolean,
    followLinks: Boolean,
    filter: suspend (Path) -> Boolean,
    onError: suspend (path: Path, throwable: Throwable) -> DeleteErrorSolution
  ): Boolean = deleteDir(source = this, recursively, followLinks, filter, onError) {
    Files.delete(it.toNioPath())
    null
  }

  override fun NioPath.materialize(): DirectoryImpl = DirectoryImpl(this)

  private suspend fun <T : PathList> copyOrMoveDir(
    isMove: Boolean,
    source: Directory,
    target: T,
    recursively: Boolean,
    overwrite: Boolean,
    followLinks: Boolean,
    filter: (Path) -> Boolean,
    onError: (path: Path, throwable: Throwable) -> CopyErrorSolution,
  ): T {
    source.walk(
      depth = if (recursively) Int.MAX_VALUE else 0,
      onError = onError,
      onEnterDirectory = { filter(it) },
    ) {
      val dest = target.joinFile(source.relativeStrTo(it))
      when {
        isMove -> it.moveTo(dest, overwrite, followLinks)
        else -> it.copyTo(dest, overwrite, followLinks)
      }
    }
    return target
  }

  private suspend fun deleteDir(
    source: Directory,
    recursively: Boolean,
    followLinks: Boolean,
    filter: suspend (Path) -> Boolean,
    onError: suspend (path: Path, throwable: Throwable) -> DeleteErrorSolution,
    handler: suspend (Path) -> Boolean?
  ): Boolean {
    var successful = true

    fun result(delete: Boolean) {
      if (successful) successful = delete
    }

    suspend fun delete(mio: Path) {
      // Make sure the path is writable
      if (mio.isWritable.not()) mio.isWritable = true
      run(catching = {
        result(false)
        throw it
      }) { handler(mio)?.let(::result) }
    }

    source.walk(
      depth = if (recursively) Int.MAX_VALUE else 0,
      followLinks = followLinks,
      filterDirs = filter,
      filterFiles = filter,
      onError = { path, e ->
        @Suppress("NewApi")
        val throwable = runCatching {
          when (e) {
            is NioDirectoryNotEmptyException -> DirectoryNotEmptyException(Path(e.file).asDir())
            is NioNoSuchFileException -> NoSuchPathException(
              Path(e.file),
              e.otherFile?.asPath(),
              e.reason
            )
            else -> e
          }
        }.getOrElse { e }
        onError(path, throwable)
      },
      onVisitFile = ::delete,
      onLeaveDirectory = ::delete,
    )

    return successful
  }
}