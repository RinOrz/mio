@file:Suppress("NewApi", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.Charset
import com.meowool.mio.Directory
import com.meowool.mio.IFile
import com.meowool.mio.JavaZipEntry
import com.meowool.mio.NioPath
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotFileException
import com.meowool.mio.Zip
import com.meowool.mio.ZipDirectoryEntry
import com.meowool.mio.ZipFileEntry
import com.meowool.mio.write
import com.meowool.mio.writeLine
import kotlinx.coroutines.flow.Flow
import okio.BufferedSink
import okio.BufferedSource
import okio.Sink
import okio.Source
import okio.buffer
import okio.use

/**
 * The zip file entry backend implemented by default.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class DefaultZipFileEntry(
  override val holder: DefaultZip,
  override var javaZipEntry: JavaZipEntry,
  override var nioPath: NioPath = NioPath(javaZipEntry.name),
) : ZipFileEntry, JavaZipEntryBackend, NioPathBackend,
  DefaultZipEntry(holder, javaZipEntry, nioPath) {


  override val appendingBufferedSink: BufferedSink
    get() = appendingSink.buffer()

  override var bytes: ByteArray
    get() = bufferedSource.use { it.readByteArray() }
    set(value) {
      bufferedSink.use { it.write(value) }
    }

  override fun isEmpty(): Boolean {
    TODO("Not yet implemented")
  }

  override fun isNotEmpty(): Boolean {
    TODO("Not yet implemented")
  }

  override fun copyTo(target: IFile, overwrite: Boolean, followLinks: Boolean): IFile {
    TODO("Not yet implemented")
  }

  override fun copyInto(target: Directory, overwrite: Boolean, followLinks: Boolean): IFile {
    TODO("Not yet implemented")
  }

  override fun copyInto(target: Zip, overwrite: Boolean, followLinks: Boolean): IFile {
    TODO("Not yet implemented")
  }

  override fun moveTo(target: IFile, overwrite: Boolean, followLinks: Boolean): IFile {
    TODO("Not yet implemented")
  }

  override fun moveInto(target: Directory, overwrite: Boolean, followLinks: Boolean): IFile {
    TODO("Not yet implemented")
  }

  override fun moveInto(target: Zip, overwrite: Boolean, followLinks: Boolean): IFile {
    TODO("Not yet implemented")
  }

  private fun createCondition(overwrite: Boolean) {
    if (this.isDirectory && this.exists()) throw PathExistsAndIsNotFileException(this)
    if (overwrite) delete()
  }

  override val absolute: ZipFileEntry
    get() = TODO("Not yet implemented")
  override val real: ZipFileEntry
    get() = TODO("Not yet implemented")
  override val normalized: ZipFileEntry
    get() = TODO("Not yet implemented")
  override val symbolicLink: ZipFileEntry
    get() = TODO("Not yet implemented")

  override fun create(overwrite: Boolean): ZipFileEntry = apply {
    createCondition(overwrite)
    createParentDirectories()
    if (this.exists().not()) holder.addEntry(this)
  }

  override fun createStrictly(overwrite: Boolean): ZipFileEntry = apply {
    createCondition(overwrite)
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The file entry already exists, it cannot be created again."
    )
    var parentPath = this.parent
    while (true) {
      if (parentPath?.exists() == false) throw ParentDirectoryNotExistsException(parentPath)
      parentPath = parentPath?.parent ?: break
    }
    if (this.exists().not()) holder.addEntry(this)
  }

  override fun replaceWith(file: IFile, keepSources: Boolean, followLinks: Boolean): ZipFileEntry {
    TODO("Not yet implemented")
  }

  override fun append(bytes: ByteArray): ZipFileEntry = apply {
    appendingBufferedSink.use { it.write(bytes) }
  }

  override fun append(source: Source): ZipFileEntry = apply {
    appendingBufferedSink.use { it.writeAll(source) }
  }

  override fun append(text: CharSequence, charset: Charset): ZipFileEntry = apply {
    appendingBufferedSink.use { it.write(text, charset) }
  }

  override fun append(lines: Iterable<CharSequence>, charset: Charset): ZipFileEntry = apply {
    appendingBufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun append(lines: Sequence<CharSequence>, charset: Charset): ZipFileEntry = apply {
    appendingBufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun write(bytes: ByteArray): ZipFileEntry = apply {
    bufferedSink.use { it.write(bytes) }
  }

  override fun write(source: Source): ZipFileEntry = apply {
    bufferedSink.use { it.writeAll(source) }
  }

  override fun write(text: CharSequence, charset: Charset): ZipFileEntry = apply {
    bufferedSink.use { it.write(text, charset) }
  }

  override fun write(lines: Iterable<CharSequence>, charset: Charset): ZipFileEntry = apply {
    bufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun write(lines: Sequence<CharSequence>, charset: Charset): ZipFileEntry = apply {
    bufferedSink.use { sink ->
      lines.forEach { sink.writeLine(it, charset) }
    }
  }

  override fun createParentDirectories(): ZipFileEntry {
    nioPath.parent?.forEach {
      holder.getOrAddEntry(ZipDirectoryEntry())
      it.toString()
    }
    val parent = nioPath.parent ?: return@apply
    TODO("Not yet implemented")
  }

  override fun toReal(followLinks: Boolean): ZipFileEntry {
    TODO("Not yet implemented")
  }

  override var extension: String
    get() = TODO("Not yet implemented")
    set(value) {}
  override val extensionWithDot: String
    get() = TODO("Not yet implemented")
  override var nameWithoutExtension: String
    get() = TODO("Not yet implemented")
    set(value) {}
  override val source: Source
    get() = TODO("Not yet implemented")
  override val bufferedSource: BufferedSource
    get() = TODO("Not yet implemented")
  override val sink: Sink
    get() = TODO("Not yet implemented")
  override val bufferedSink: BufferedSink
    get() = TODO("Not yet implemented")
  override val appendingSink: Sink
    get() = TODO("Not yet implemented")

  override fun delete(followLinks: Boolean): Boolean = holder.deleteEntry(this)
  override fun deleteStrictly(followLinks: Boolean): Boolean {
    TODO("Not yet implemented")
  }

  override fun text(charset: Charset): String {
    TODO("Not yet implemented")
  }

  override fun lines(charset: Charset): Flow<String> {
    TODO("Not yet implemented")
  }
}