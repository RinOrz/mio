@file:Suppress(
  "RESERVED_MEMBER_INSIDE_INLINE_CLASS",
  "BlockingMethodInNonBlockingContext",
  "OVERRIDE_BY_INLINE",
  "NOTHING_TO_INLINE",
)

package com.meowool.mio.internal

import com.meowool.mio.Charset
import com.meowool.mio.Directory
import com.meowool.mio.IFile
import com.meowool.mio.IoFile
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotFileException
import com.meowool.mio.Zip
import com.meowool.mio.readLine
import com.meowool.mio.readText
import com.meowool.mio.resolveFileExtension
import com.meowool.mio.toIoFile
import com.meowool.mio.toMioFile
import com.meowool.mio.write
import com.meowool.mio.writeLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.BufferedSink
import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import okio.use

/**
 * The file backend implement with [IoFile].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal open class DefaultFileLegacy(path: IoFile) : IFile, DefaultPathLegacy(path) {
  override val absolute: IFile
    get() = absoluteImpl().toMioFile()

  override val real: IFile
    get() = realImpl().toMioFile()

  override val normalized: IFile
    get() = normalizedImpl().toMioFile()

  override val symbolicLink: IFile
    get() = real

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
    get() = ioFile.source()

  override val bufferedSource: BufferedSource
    get() = ioFile.source().buffer()

  override val sink: Sink
    get() = ioFile.sink()

  override val bufferedSink: BufferedSink
    get() = ioFile.sink().buffer()

  override val appendingSink: Sink
    get() = ioFile.sink(append = true)

  override val appendingBufferedSink: BufferedSink
    get() = ioFile.sink(append = true).buffer()

  override var bytes: ByteArray
    get() = bufferedSource.use { it.readByteArray() }
    set(value) {
      bufferedSink.use { it.write(value) }
    }

  override fun isEmpty(): Boolean = size < 0

  override fun isNotEmpty(): Boolean = size > 0

  private fun createCondition(overwrite: Boolean) {
    if (this.exists() && this.isDirectory) throw PathExistsAndIsNotFileException(this)
    if (this.exists() && overwrite) delete()
  }

  override fun create(overwrite: Boolean): IFile {
    createCondition(overwrite)
    createParentDirectories()
    if (this.exists().not()) ioFile.normalize().createNewFile()
    return this
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
    ioFile.normalize().createNewFile()
  }

  override fun replaceWith(
    file: IFile,
    keepSources: Boolean,
    followLinks: Boolean,
  ): IFile {
    val source = file.toIoFile().normalize()
    val target = (if (followLinks) real else this).createParentDirectories().toIoFile().normalize()
    val result = source.copyTo(target, overwrite = true)
    if (keepSources) target.apply {
      setWritable(source.canWrite())
      setExecutable(source.canExecute())
      setLastModified(source.lastModified())
    }
    if (keepSources.not()) source.delete()
    return result.toMioFile()
  }

  override fun copyTo(
    target: IFile,
    overwrite: Boolean,
    followLinks: Boolean,
  ): IFile {
    if (overwrite.not() && target.exists()) throw PathAlreadyExistsException(target)
    // Do not call the system's copy api on this file, but hand it over to the target's
    // `replaceWith` implementation, because under certain circumstances the
    // system's `IoFile.copyTo` cannot expect to copy the file, for example, the target file is an
    // entry in the zip archive.
    return target.replaceWith(file = this, keepSources = true, followLinks)
  }

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
  ): IFile {
    if (overwrite.not() && target.exists()) throw PathAlreadyExistsException(target)
    // Do not call the system's move api on this file, but hand it over to the target's
    // `replaceWith` implementation, because under certain circumstances the
    // system's `IoFile.moveTo` cannot expect to copy the file, for example, the target file is an
    // entry in the zip archive.
    return target.replaceWith(file = this, keepSources = false, followLinks)
  }

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
      ioFile.delete()
    }
  }

  override fun deleteStrictly(followLinks: Boolean): Boolean = when {
    isSymbolicLink && followLinks -> real.deleteStrictly(followLinks)
    else -> ioFile.delete()
  }

  override fun text(charset: Charset): String = bufferedSource.readText(charset)

  override fun lines(charset: Charset): Flow<String> = bufferedSource.use {
    flow { it.readLine(charset)?.apply { emit(this) } }
  }

  override fun append(bytes: ByteArray): IFile = apply {
    appendingBufferedSink.use { it.write(bytes) }
  }

  override fun append(source: Source): IFile = apply {
    appendingBufferedSink.use { it.writeAll(source) }
  }

  override fun append(text: CharSequence, charset: Charset): IFile = apply {
    appendingBufferedSink.use { it.write(text, charset) }
  }

  override fun append(lines: Iterable<CharSequence>, charset: Charset): IFile = apply {
    appendingBufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun append(lines: Sequence<CharSequence>, charset: Charset): IFile = apply {
    appendingBufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun write(bytes: ByteArray): IFile = apply {
    bufferedSink.use { it.write(bytes) }
  }

  override fun write(source: Source): IFile = apply {
    bufferedSink.use { it.writeAll(source) }
  }

  override fun write(text: CharSequence, charset: Charset): IFile = apply {
    bufferedSink.use { it.write(text, charset) }
  }

  override fun write(lines: Iterable<CharSequence>, charset: Charset): IFile = apply {
    bufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun write(lines: Sequence<CharSequence>, charset: Charset): IFile = apply {
    bufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun createParentDirectories(): IFile = apply { super.createParentDirectories() }

  override fun toReal(followLinks: Boolean): IFile = toRealImpl(followLinks).toMioFile()

}