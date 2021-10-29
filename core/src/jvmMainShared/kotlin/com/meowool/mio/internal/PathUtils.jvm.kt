@file:Suppress("NOTHING_TO_INLINE", "RemoveRedundantQualifierName")

package com.meowool.mio.internal

import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.File
import com.meowool.mio.IoFile
import com.meowool.mio.NioPath
import com.meowool.mio.NoSuchPathException
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.Path
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotFileException
import com.meowool.mio.asPath
import com.meowool.mio.toIoFile
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption

internal actual val userHome: String = System.getProperty("user.home")
internal actual val currentDir: String = System.getProperty("user.dir")


/**
 * ===============================================================
 * =                    Create implementation                    =
 * ===============================================================
 */

private fun File.preCreate(overwrite: Boolean) {
  if (this.isDirectory && this.exists()) throw PathExistsAndIsNotFileException(this)
  if (overwrite) delete()
}

internal inline fun File.create(overwrite: Boolean, process: () -> Unit) {
  preCreate(overwrite)
  createParentDirectories()
  if (this.exists().not()) process()
}

internal inline fun File.createStrictly(overwrite: Boolean, process: () -> Unit) {
  preCreate(overwrite)
  if (this.exists()) throw PathAlreadyExistsException(
    this, reason = "The file already exists, it cannot be created again."
  )
  var parentPath = this.parent
  while (true) {
    if (parentPath?.exists() == false) throw ParentDirectoryNotExistsException(parentPath)
    parentPath = parentPath?.parent ?: break
  }
  process()
}


/**
 * ===============================================================
 * =                 Copy or move implementation                 =
 * ===============================================================
 */

internal inline fun <R : File> copyOrMoveFile(
  source: File,
  target: R,
  overwrite: Boolean,
  process: (sourceFile: File, targetFile: File) -> Unit,
): R {
  if (target.exists() && overwrite.not()) throw PathAlreadyExistsException(target)
  try {
    process(source, target.createParentDirectories())
  } catch (e: Throwable) {
    runCatching {
      @Suppress("NewApi")
      when (e) {
        is java.nio.file.FileAlreadyExistsException -> throw PathAlreadyExistsException(Path(e.file), e.otherFile?.asPath(), e.reason)
        is java.nio.file.DirectoryNotEmptyException -> throw DirectoryNotEmptyException(Path(e.file))
        is java.nio.file.NoSuchFileException -> throw NoSuchPathException(Path(e.file), e.otherFile?.asPath(), e.reason)
      }; false
    }
    throw when (e) {
      is kotlin.io.FileAlreadyExistsException -> PathAlreadyExistsException(Path(e.file), e.other?.toMioPath(), e.reason)
      is kotlin.io.NoSuchFileException -> NoSuchPathException(Path(e.file), e.other?.toMioPath(), e.reason)
      else -> e
    }
  }
  return target
}


/**
 * ===============================================================
 * =                    Deletes implementation                   =
 * ===============================================================
 */

internal inline fun File.delete(followLinks: Boolean, process: () -> Boolean): Boolean = when {
  isSymbolicLink && followLinks -> real.delete(followLinks)
  else -> {
    if (!isWritable) isWritable = true
    process()
  }
}

internal inline fun File.deleteStrictly(followLinks: Boolean, process: () -> Boolean): Boolean = when {
  isSymbolicLink && followLinks -> real.deleteStrictly(followLinks)
  else -> {
    if (!isWritable) isWritable = true
    @Suppress("NewApi")
    runCatching { process() }.getOrElse {
      when(it) {
        is java.nio.file.NoSuchFileException -> throw NoSuchPathException(
          it.file.asPath(), it.otherFile?.asPath(), it.reason
        )
        is kotlin.io.NoSuchFileException -> throw NoSuchPathException(
          Path(it.file), it.other?.toMioPath(), it.reason
        )
      }
      false
    }
  }
}

internal fun Path.equalsTo(other: Any?): Boolean {
  if (other == null) return false
  if (other is Path) return this.normalizedString == other.normalizedString
  if (other is CharSequence) return this == Path(other)

  if (other is IoFile) return toIoFile().normalize() == other.normalize()
  runCatching { if (other is NioPath) return toNioPath().normalize() == other.normalize() }

  return false
}