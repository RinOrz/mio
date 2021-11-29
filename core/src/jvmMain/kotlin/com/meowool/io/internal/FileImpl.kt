@file:Suppress("BlockingMethodInNonBlockingContext", "OVERRIDE_BY_INLINE")

package com.meowool.io.internal

import com.meowool.io.BaseFile
import com.meowool.io.Charset
import com.meowool.io.DirectoryNotEmptyException
import com.meowool.io.File
import com.meowool.io.NioPath
import com.meowool.io.NoSuchPathException
import com.meowool.io.Path
import com.meowool.io.PathAlreadyExistsException
import com.meowool.io.PathExistsAndIsNotFileException
import com.meowool.io.PathList
import com.meowool.io.asDir
import com.meowool.io.asPath
import com.meowool.io.channel.DataChannel
import com.meowool.io.channel.FileChannel
import com.meowool.io.toNioPath
import com.meowool.sweekt.coroutines.flowOnIO
import com.meowool.sweekt.coroutines.withDefaultContext
import com.meowool.sweekt.coroutines.withIOContext
import com.meowool.sweekt.suspendGetter
import com.meowool.sweekt.suspendSetter
import kotlinx.coroutines.flow.Flow
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.appendBytes
import kotlin.io.path.appendLines
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.io.path.writeLines
import java.nio.file.DirectoryNotEmptyException as NioDirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException as NioFileAlreadyExistsException
import java.nio.file.NoSuchFileException as NioNoSuchFileException

/**
 * The default implementation of Mio file based on [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class FileImpl(nioPath: NioPath) : BasePathImpl<FileImpl>(nioPath), BaseFile<FileImpl> {
  override var extension: String
    get() = getFileExtension(name, withDot = false)
    set(value) = rename("$nameWithoutExtension.$value")

  override var extensionWithDot: String
    get() = getFileExtension(name, withDot = true)
    set(value) = rename("$nameWithoutExtension$value")

  override var nameWithoutExtension: String
    get() = name.removeSuffix(extensionWithDot)
    set(value) = rename("$value$extensionWithDot")

  override var bytes: ByteArray
    get() = suspendGetter {
      withIOContext { nioPath.readBytes() }
    }
    set(value) = suspendSetter { write(value) }

  override fun open(): FileChannel {
    TODO("Not yet implemented")
  }

  override fun create(overwrite: Boolean): FileImpl = apply {
    preCreate(overwrite)
    createParentDirectories()
    if (this.exists().not()) Files.createFile(nioPath)
  }

  override suspend fun replaceWith(
    file: FileImpl,
    keepSources: Boolean,
    followLinks: Boolean
  ): FileImpl = apply {
    copyOrMoveFile(
      isMove = !keepSources,
      source = file,
      target = this,
      overwrite = true,
      followLinks
    )
  }

  override suspend fun <R : File> copyTo(target: R, overwrite: Boolean, followLinks: Boolean): R =
    copyOrMoveFile(isMove = false, source = this, target, overwrite, followLinks)

  override suspend fun copyInto(target: PathList, overwrite: Boolean, followLinks: Boolean): File =
    copyTo(target.joinFile(this.name), overwrite, followLinks)

  override suspend fun <R : File> moveTo(target: R, overwrite: Boolean, followLinks: Boolean): R =
    copyOrMoveFile(isMove = true, source = this, target, overwrite, followLinks)

  override suspend fun moveInto(target: PathList, overwrite: Boolean, followLinks: Boolean): File =
    moveTo(target.joinFile(this.name), overwrite, followLinks)

  override fun delete(followLinks: Boolean): Boolean =
    delete(followLinks) { Files.deleteIfExists(nioPath) }

  override suspend fun text(charset: Charset): String = withIOContext {
    Files.readString(nioPath, charset)
  }

  override fun lines(charset: Charset): Flow<String> = streamFlow {
    Files.lines(nioPath, charset)
  }.flowOnIO()

  override suspend fun append(bytes: ByteArray): FileImpl = withIOContext {
    nioPath.appendBytes(bytes)
    this@FileImpl
  }

  override suspend fun append(channel: DataChannel): FileImpl = withIOContext {
    nioPath.appendBytes(channel.toBytes())
    this@FileImpl
  }

  override suspend fun append(text: CharSequence, charset: Charset): FileImpl = withIOContext {
    Files.writeString(nioPath, text, charset, StandardOpenOption.APPEND)
    this@FileImpl
  }

  override suspend fun append(
    lines: Iterable<CharSequence>,
    charset: Charset
  ): FileImpl = withIOContext {
    nioPath.appendLines(lines, charset)
    this@FileImpl
  }

  override suspend fun append(
    lines: Sequence<CharSequence>,
    charset: Charset
  ): FileImpl = withDefaultContext { append(lines.asIterable(), charset) }

  override suspend inline fun write(bytes: ByteArray): FileImpl = withIOContext {
    nioPath.writeBytes(bytes)
    this@FileImpl
  }

  override suspend fun write(channel: DataChannel): FileImpl = withIOContext {
    nioPath.writeBytes(channel.toBytes())
    this@FileImpl
  }

  override suspend fun write(text: CharSequence, charset: Charset): FileImpl = withIOContext {
    Files.writeString(nioPath, text, charset)
    this@FileImpl
  }

  override suspend fun write(
    lines: Iterable<CharSequence>,
    charset: Charset
  ): FileImpl = withIOContext {
    nioPath.writeLines(lines, charset)
    this@FileImpl
  }

  override suspend fun write(
    lines: Sequence<CharSequence>,
    charset: Charset
  ): FileImpl = withDefaultContext { write(lines.asIterable(), charset) }

  override fun NioPath.materialize(): FileImpl = FileImpl(this)

  private fun File.preCreate(overwrite: Boolean) {
    if (this.isDirectory && this.exists()) throw PathExistsAndIsNotFileException(this)
    if (overwrite) delete()
  }

  private inline fun File.delete(followLinks: Boolean, process: () -> Boolean): Boolean = when {
    isSymbolicLink && followLinks -> real.delete(followLinks)
    else -> {
      if (!isWritable) isWritable = true
      process()
    }
  }

  private suspend fun <R : File> copyOrMoveFile(
    isMove: Boolean,
    source: File,
    target: R,
    overwrite: Boolean,
    followLinks: Boolean,
  ): R = withIOContext {
    if (target.exists() && overwrite.not()) throw PathAlreadyExistsException(target)
    try {
      val options = listOfNotNull<CopyOption>(
        if (overwrite) StandardCopyOption.REPLACE_EXISTING else null,
        if (isMove.not()) StandardCopyOption.COPY_ATTRIBUTES else null,
        if (followLinks.not()) LinkOption.NOFOLLOW_LINKS else null,
      ).toTypedArray()

      target.createParentDirectories()

      when {
        isMove -> Files.move(source.toNioPath(), target.toNioPath(), *options)
        else -> Files.copy(source.toNioPath(), target.toNioPath(), *options)
      }
    } catch (e: Throwable) {
      when (e) {
        is NioFileAlreadyExistsException ->
          throw PathAlreadyExistsException(Path(e.file), e.otherFile?.asPath(), e.reason)

        is NioDirectoryNotEmptyException ->
          throw DirectoryNotEmptyException(Path(e.file).asDir())

        is NioNoSuchFileException ->
          throw NoSuchPathException(Path(e.file), e.otherFile?.asPath(), e.reason)
      }
    }
    target
  }
}