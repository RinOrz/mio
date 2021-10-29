@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.CopyErrorSolution
import com.meowool.mio.DeleteErrorSolution
import com.meowool.mio.Directory
import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.IoFile
import com.meowool.mio.NioPath
import com.meowool.mio.NoSuchPathException
import com.meowool.mio.Path
import com.meowool.mio.IPath
import com.meowool.mio.SystemSeparator
import com.meowool.mio.UnixSeparator
import com.meowool.mio.WindowsSeparator
import com.meowool.mio.asPath
import com.meowool.mio.relativeStringTo
import com.meowool.sweekt.String
import com.meowool.sweekt.iteration.toArray
import com.meowool.sweekt.removeFirst
import com.meowool.sweekt.removeLast
import java.io.File
import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import kotlin.io.path.absolute

internal inline fun IoFile.normalizeZipEntryFile(isDirectory: Boolean): IoFile =
  File(normalizeZipEntry(isDirectory))

internal inline fun IoFile.normalizeZipEntry(isDirectory: Boolean): String =
  absoluteFile.normalize().toString().normalizeZipEntry(isDirectory)

internal inline fun NioPath.normalizeZipEntryPath(isDirectory: Boolean): NioPath =
  NioPath(normalizeZipEntry(isDirectory))

@Suppress("NewApi")
internal inline fun NioPath.normalizeZipEntry(isDirectory: Boolean): String =
  absolute().normalize().toString().normalizeZipEntry(isDirectory)

internal fun String.normalizeZipEntry(isDirectory: Boolean): String {
  var path = this.replace(WindowsSeparator, UnixSeparator)
  // java.util.zip.ZipEntry does not require root separator
  if (path.first() == '/') path = path.removeFirst()
  if (path.last() == '/' && isDirectory.not()) path = path.removeLast()
  return path
}

internal fun deleteDir(
  source: Directory,
  recursively: Boolean,
  followLinks: Boolean,
  filter: (IPath) -> Boolean,
  onError: (path: IPath, throwable: Throwable) -> DeleteErrorSolution,
  handler: (IPath) -> Boolean?
): Boolean {
  var successful = true

  fun result(delete: Boolean) {
    if (successful) successful = delete
  }

  fun delete(mio: IPath) {
    // Make sure the path is writable
    if (mio.isWritable.not()) mio.isWritable = true
    runCatching { handler(mio)?.let(::result) }.getOrElse {
      result(false)
      throw it
    }
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
          is java.nio.file.DirectoryNotEmptyException -> DirectoryNotEmptyException(Path(e.file))
          is NoSuchFileException -> NoSuchPathException(Path(e.file), e.otherFile?.asPath(), e.reason)
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

internal fun copyOrMoveDir(
  isMove: Boolean,
  source: Directory,
  target: Directory,
  recursively: Boolean,
  overwrite: Boolean,
  followLinks: Boolean,
  filter: (IPath) -> Boolean,
  onError: (path: IPath, throwable: Throwable) -> CopyErrorSolution,
): Directory {
  source.walk(
    depth = if (recursively) Int.MAX_VALUE else 0,
    onError = onError,
    onEnterDirectory = { filter(it) },
  ) {
    val dest = target.joinFile(source.relativeStringTo(it))
    when {
      isMove -> it.moveTo(dest, overwrite, followLinks)
      else -> it.copyTo(dest, overwrite, followLinks)
    }
  }
  return target
}

internal inline fun <R> backport(
  legacy: () -> R,
  modern: () -> R,
): R = try {
  modern()
} catch (e: Exception) {
  legacy()
}

@Suppress("NewApi")
internal fun NioPath(first: CharSequence, vararg more: CharSequence): NioPath = Paths.get(
  first.toString(),
  *more.map(::String).toArray()
)

@Suppress("NewApi")
internal fun IoFile(first: CharSequence, vararg more: CharSequence): IoFile = when {
  more.isEmpty() -> IoFile(first.toString())
  else -> IoFile(first.toString(), more.joinToString(SystemSeparator))
}