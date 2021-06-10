@file:Suppress("SpellCheckingInspection", "NewApi")

package com.meowool.mio.internal

import com.meowool.mio.FileHandlingErrorSolution
import com.meowool.mio.NioPath
import com.meowool.mio.Path
import com.meowool.mio.relativeTo
import com.meowool.mio.resolve
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import java.io.IOException
import java.nio.file.CopyOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.ProviderMismatchException
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

/**
 * Recursive copy/move processing of directory.
 *
 * This is a copy modified from the [apache commons-io](https://github.com/apache/commons-io/blob/master/src/main/java/org/apache/commons/io/file/CopyDirectoryVisitor.java)
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class CopyMoveRecursivelyVisitor(
  private val isMove: Boolean,
  private val source: Path,
  private val target: Path,
  private val overwrite: Boolean,
  private val keepAttributes: Boolean,
  private val followLinks: Boolean,
  private val filter: (Path) -> Boolean,
  private val onError: (Path, Exception) -> FileHandlingErrorSolution
) : SimpleFileVisitor<NioPath>() {

  override fun preVisitDirectory(dir: NioPath, attrs: BasicFileAttributes): FileVisitResult {
    // 创建对应的目标文件夹
    resolveRelativeAsString(dir).createDirectory()
    return super.preVisitDirectory(dir, attrs)
  }

  override fun visitFile(file: NioPath, attrs: BasicFileAttributes): FileVisitResult {
    copyOrMoveFile(
      isMove = isMove,
      source = file.toMioPath(),
      target = resolveRelativeAsString(file),
      overwrite = overwrite,
      keepAttributes = keepAttributes,
      followLinks = followLinks,
      filter = filter,
      onError = onError
    )
    return super.visitFile(file, attrs)
  }

  override fun visitFileFailed(file: NioPath, exc: IOException): FileVisitResult {
    return when(onError(file.toMioPath(), exc)) {
      FileHandlingErrorSolution.Skip -> FileVisitResult.CONTINUE
      FileHandlingErrorSolution.Stop -> super.visitFileFailed(file, exc)
    }
  }

  /**
   * Relativizes against [source], then resolves against [target].
   *
   * We have to call [Path.toString] relative value because we cannot use paths belonging to
   * different FileSystems in the Path methods, usually this leads to [ProviderMismatchException].
   *
   * @param directory the directory to relativize.
   * @return a new path, relativized against sourceDirectory, then resolved against targetDirectory.
   */
  private fun resolveRelativeAsString(directory: NioPath): Path =
    target.resolve(source.relativeTo(directory).toString())
}

/** Make copy and move simple. */
internal fun copyOrMoveFile(
  isMove: Boolean,
  source: Path,
  target: Path,
  overwrite: Boolean,
  keepAttributes: Boolean,
  followLinks: Boolean,
  filter: (Path) -> Boolean,
  onError: (Path, Exception) -> FileHandlingErrorSolution
): NioPath {
  // 过滤掉的文件不要执行任何操作
  if (!filter(source)) return source.toNioPath()

  val options = listOfNotNull<CopyOption>(
    if (overwrite) StandardCopyOption.REPLACE_EXISTING else null,
    if (keepAttributes) StandardCopyOption.COPY_ATTRIBUTES else null,
    if (followLinks.not()) LinkOption.NOFOLLOW_LINKS else null,
  ).toTypedArray()

  return try {
    // 一旦设置了 nio-move 不支持的属性，则使用 copy + delete 方案替代
    if (isMove.not() || followLinks.not() || keepAttributes) {
      val sourcePath = source.absolute.toNioPath()
      Files.copy(sourcePath, target.absolute.toNioPath(), *options).apply {
        if (isMove) Files.delete(sourcePath)
      }
    } else {
      Files.move(source.absolute.toNioPath(), target.absolute.toNioPath(), *options)
    }
  } catch (e: Exception) {
    when (onError(source, e)) {
      // 跳过复制，返回当前文件
      FileHandlingErrorSolution.Skip -> source.absolute.toNioPath()
      // 抛出异常以停止所有复制
      FileHandlingErrorSolution.Stop -> throw e
    }
  }
}