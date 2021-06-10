@file:Suppress("NewApi")

package com.meowool.mio.internal

import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.FileHandlingErrorSolution
import com.meowool.mio.NioPath
import com.meowool.mio.NoSuchFileException
import com.meowool.mio.Path
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import com.meowool.mio.toRealPath
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * Recursive delete processing of directory.
 *
 * This is a copy modified from the [apache commons-io](https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/file/DeletingPathVisitor.java)
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class DeleteRecursivelyVisitor(
  private val isStrict: Boolean,
  private val followLinks: Boolean,
  private val filter: (Path) -> Boolean,
  private val onError: (Path, Exception) -> FileHandlingErrorSolution,
  private val safeRoot: NioPath? = null,
) : SimpleFileVisitor<NioPath>() {
  private val results = mutableListOf(true)

  /** 表示所有文件都成功删除 */
  val successful get() = results.all { it }

  override fun visitFile(file: NioPath, attrs: BasicFileAttributes): FileVisitResult {
    results += deleteFile(isStrict, file.toMioPath(), followLinks, filter, onError)
    return super.visitFile(file, attrs)
  }

  /** 在文件都删除后才能删除文件夹 */
  override fun postVisitDirectory(dir: NioPath, exc: IOException?): FileVisitResult? {
    if (safeRoot != null && safeRoot == dir) return FileVisitResult.CONTINUE
    results += dir.toMioPath().delete(isStrict, onError)
    return super.postVisitDirectory(dir, exc)
  }

  override fun visitFileFailed(file: NioPath, exc: IOException): FileVisitResult {
    return when (onError(file.toMioPath(), exc)) {
      FileHandlingErrorSolution.Skip -> FileVisitResult.CONTINUE
      FileHandlingErrorSolution.Stop -> {
        results += false
        super.visitFileFailed(file, exc)
      }
    }
  }
}

internal fun deleteFile(
  isStrict: Boolean,
  site: Path,
  followLinks: Boolean,
  filter: (Path) -> Boolean,
  onError: (Path, Exception) -> FileHandlingErrorSolution,
): Boolean {
  // 过滤掉的文件不要执行任何操作
  if (!filter(site)) return true

  return when {
    site.isSymbolicLink && followLinks -> site.toRealPath().delete(isStrict, onError)
    else -> site.delete(isStrict, onError)
  }
}

private fun Path.delete(
  isStrict: Boolean,
  onError: (Path, Exception) -> FileHandlingErrorSolution,
): Boolean {
  // 确保路径可写
  if (!isWritable) isWritable = true
  return try {
    if (isStrict) {
      Files.delete(this.toNioPath())
    } else {
      // 目录不为空时不要删除
      if (isDirectory && isNotEmpty()) return false
      Files.deleteIfExists(this.toNioPath())
    }
    true
  } catch (e: Exception) {
    val exception = when (e) {
      is java.nio.file.DirectoryNotEmptyException -> if (isStrict) DirectoryNotEmptyException(Path(e.file)) else return false
      is java.nio.file.NoSuchFileException -> if (isStrict) NoSuchFileException(Path(e.file)) else return false
      else -> e
    }
    when (onError(this, exception)) {
      // 跳过删除，返回当前文件
      FileHandlingErrorSolution.Skip -> absolute
      // 抛出异常以停止所有删除
      FileHandlingErrorSolution.Stop -> throw e
    }
    false
  }
}