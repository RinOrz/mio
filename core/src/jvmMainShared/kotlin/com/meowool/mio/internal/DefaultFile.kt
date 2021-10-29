@file:Suppress("NewApi",
  "BlockingMethodInNonBlockingContext",
  "RESERVED_MEMBER_INSIDE_INLINE_CLASS",
  "OVERRIDE_BY_INLINE",
  "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.Charset
import com.meowool.mio.Directory
import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.IFile
import com.meowool.mio.NioPath
import com.meowool.mio.NoSuchPathException
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.Path
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotFileException
import com.meowool.mio.Zip
import com.meowool.mio.asPath
import com.meowool.mio.resolveFileExtension
import com.meowool.mio.toMioFile
import com.meowool.mio.toNioPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.BufferedSink
import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.appendBytes
import kotlin.io.path.appendLines
import kotlin.io.path.appendText
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeLines
import kotlin.io.path.writeText

/**
 * The file backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal open class DefaultFile(path: NioPath) : IFile, DefaultPath(path) {
  override val absolute: IFile
    get() = absoluteImpl().toMioFile()

  override val real: IFile
    get() = realImpl().toMioFile()

  override val normalized: IFile
    get() = normalizedImpl().toMioFile()

  override val symbolicLink: IFile
    get() = symbolicLinkImpl().toMioFile()

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

  override val source: Source
    get() = nioPath.source()

  override val bufferedSource: BufferedSource
    get() = nioPath.source().buffer()

  override val sink: Sink
    get() = nioPath.sink()

  override val bufferedSink: BufferedSink
    get() = nioPath.sink().buffer()

  override val appendingSink: Sink
    get() = nioPath.sink(StandardOpenOption.APPEND)

  override val appendingBufferedSink: BufferedSink
    get() = nioPath.sink(StandardOpenOption.APPEND).buffer()

  override var bytes: ByteArray
    get() = nioPath.readBytes()
    set(value) {
      nioPath.writeBytes(value)
    }

  override fun isEmpty(): Boolean = size < 0

  override fun isNotEmpty(): Boolean = size > 0

  private fun createCondition(overwrite: Boolean) {
    if (this.isDirectory && this.exists()) throw PathExistsAndIsNotFileException(this)
    if (overwrite) delete()
  }

  override fun create(overwrite: Boolean): IFile = apply {
    createCondition(overwrite)
    createParentDirectories()
    if (this.exists().not()) Files.createFile(nioPath.normalize())
  }

  override fun createStrictly(overwrite: Boolean): IFile = apply {
    createCondition(overwrite)
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The file already exists, it cannot be created again."
    )
    var parentPath = this.parent
    while (true) {
      if (parentPath?.exists() == false) throw ParentDirectoryNotExistsException(parentPath)
      parentPath = parentPath?.parent ?: break
    }
    Files.createFile(nioPath.normalize())
  }

  override fun replaceWith(
    file: IFile,
    keepSources: Boolean,
    followLinks: Boolean
  ): IFile = copyOrMoveFile(
    isMove = !keepSources,
    source = file,
    target = this,
    overwrite = true,
    followLinks
  )

  override fun copyTo(
    target: IFile,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile = copyOrMoveFile(isMove = false, source = this, target, overwrite, followLinks)

  override fun copyInto(
    target: Directory,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile = this.copyTo(target.joinFile(this.name), overwrite, followLinks)

  override fun copyInto(
    target: Zip,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile = this.copyTo(target.joinFile(this.name), overwrite, followLinks)

  override fun moveTo(
    target: IFile,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile = copyOrMoveFile(isMove = true, source = this, target, overwrite, followLinks)

  override fun moveInto(
    target: Directory,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile = this.moveTo(target.joinFile(this.name), overwrite, followLinks)

  override fun moveInto(
    target: Zip,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile = this.moveTo(target.joinFile(this.name), overwrite, followLinks)

  override fun delete(followLinks: Boolean): Boolean = when {
    isSymbolicLink && followLinks -> real.delete(followLinks)
    else -> {
      if (!isWritable) isWritable = true
      Files.deleteIfExists(nioPath)
    }
  }

  override fun deleteStrictly(followLinks: Boolean): Boolean = when {
    isSymbolicLink && followLinks -> real.deleteStrictly(followLinks)
    else -> {
      if (!isWritable) isWritable = true
      runCatching { Files.delete(nioPath); true }.getOrElse {
        if (it is NoSuchFileException) throw NoSuchPathException(
          it.file.asPath(), it.otherFile?.asPath(), it.reason
        )
        false
      }
    }
  }

  override fun text(charset: Charset): String = nioPath.readText(charset)

  override fun lines(charset: Charset): Flow<String> = Files.newBufferedReader(nioPath, charset).use {
    flow { it.readLine()?.apply { emit(this) } }
  }

  override fun append(bytes: ByteArray): IFile = apply {
    BufferedOutputStream
    nioPath.appendBytes(bytes)
  }

  override fun append(source: Source): IFile = apply {
    appendingBufferedSink.writeAll(source)
  }

  override fun append(text: CharSequence, charset: Charset): IFile = apply {
    nioPath.appendText(text, charset)
  }

  override fun append(lines: Iterable<CharSequence>, charset: Charset): IFile = apply {
    nioPath.appendLines(lines, charset)
  }

  override fun append(lines: Sequence<CharSequence>, charset: Charset): IFile = apply {
    nioPath.appendLines(lines, charset)
  }

  override fun write(bytes: ByteArray): IFile = apply {
    nioPath.writeBytes(bytes)
  }

  override fun write(source: Source): IFile = apply {
    bufferedSink.writeAll(source)
  }

  override fun write(text: CharSequence, charset: Charset): IFile = apply {
    nioPath.writeText(text, charset)
  }

  override fun write(lines: Iterable<CharSequence>, charset: Charset): IFile = apply {
    nioPath.writeLines(lines, charset)
  }

  override fun write(lines: Sequence<CharSequence>, charset: Charset): IFile = apply {
    nioPath.writeLines(lines, charset)
  }

  override fun createParentDirectories(): IFile = apply { super.createParentDirectories() }

  override fun toReal(followLinks: Boolean): IFile = toRealImpl(followLinks).toMioFile()

  private fun copyOrMoveFile(
    isMove: Boolean,
    source: IFile,
    target: IFile,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile {
    val options = listOfNotNull<CopyOption>(
      if (overwrite) StandardCopyOption.REPLACE_EXISTING else null,
      if (isMove.not()) StandardCopyOption.COPY_ATTRIBUTES else null,
      if (followLinks.not()) LinkOption.NOFOLLOW_LINKS else null,
    ).toTypedArray()

    if (target.exists() && overwrite.not()) throw PathAlreadyExistsException(target)

    val sourcePath = source.toNioPath().normalize()
    val targetPath = target.createParentDirectories().toNioPath().normalize()
    return try {
      when {
        isMove -> Files.move(sourcePath, targetPath, *options)
        else -> Files.copy(sourcePath, targetPath, *options)
      }.toMioFile()
    } catch (e: Throwable) {
      throw when (e) {
        is java.nio.file.FileAlreadyExistsException -> PathAlreadyExistsException(Path(e.file), e.otherFile?.asPath(), e.reason)
        is java.nio.file.DirectoryNotEmptyException -> DirectoryNotEmptyException(Path(e.file))
        is NoSuchFileException -> NoSuchPathException(Path(e.file), e.otherFile?.asPath(), e.reason)
        else -> e
      }
    }
  }
}