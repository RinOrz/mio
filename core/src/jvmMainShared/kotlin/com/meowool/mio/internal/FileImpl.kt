@file:Suppress("NewApi", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.Charset
import com.meowool.mio.File
import com.meowool.mio.FileChannel
import com.meowool.mio.IFile
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotFileException
import com.meowool.mio.PathGroup
import com.meowool.mio.toNioPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import kotlin.io.path.readBytes
import kotlin.io.path.readText
import kotlin.io.path.writeBytes



/**
 * The file backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal abstract class TypedFileImpl<Self : IFile<Self>>(chars: CharSequence) :
  IFile<Self>, PathImpl<Self>(chars) {
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
    get() = nioPath.readBytes()
    set(value) {
      nioPath.writeBytes(value)
    }

  override fun create(overwrite: Boolean): Self = self {
    preCreate(overwrite)
    createParentDirectories()
    if (this.exists().not()) Files.createFile(nioPath)
  }

  override fun createStrictly(overwrite: Boolean): Self = self {
    preCreate(overwrite)
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The file already exists, it cannot be created again."
    )
    var parentPath = this.parent
    while (true) {
      if (parentPath?.exists() == false) throw ParentDirectoryNotExistsException(parentPath)
      parentPath = parentPath?.parent ?: break
    }
    Files.createFile(nioPath)
  }

  override fun replaceWith(file: Self, keepSources: Boolean, followLinks: Boolean): Self = self {
    val target = copyOrMoveFile(
      isMove = !keepSources,
      source = file,
      target = this,
      overwrite = true,
      followLinks
    )
    repath(target.toString())
  }

  override fun <R : File> copyTo(target: R, overwrite: Boolean, followLinks: Boolean): R =
    copyOrMoveFile(isMove = false, source = this, target, overwrite, followLinks)

  override fun copyInto(target: PathGroup, overwrite: Boolean, followLinks: Boolean): Self =
    self { copyTo(target.joinFile(this.name), overwrite, followLinks) }

  override fun <R : File> moveTo(target: R, overwrite: Boolean, followLinks: Boolean): R =
    copyOrMoveFile(isMove = true, source = this, target, overwrite, followLinks)

  override fun moveInto(target: PathGroup, overwrite: Boolean, followLinks: Boolean): Self =
    self { moveTo(target.joinFile(this.name), overwrite, followLinks) }

  override fun delete(followLinks: Boolean): Boolean =
    delete(followLinks) { Files.deleteIfExists(nioPath) }

  override fun deleteStrictly(followLinks: Boolean): Boolean =
    delete(followLinks) { Files.delete(nioPath); true }

  override fun text(charset: Charset): String = nioPath.readText(charset)

  override fun lines(charset: Charset): Flow<String> = open {
    flow { readLineOrNull()?.also { emit(it) } }
  }

  override fun append(bytes: ByteArray): Self {
    TODO("Not yet implemented")
  }

  override fun append(channel: FileChannel<*>): Self {
    TODO("Not yet implemented")
  }

  override fun append(text: CharSequence, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun append(lines: Iterable<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun append(lines: Sequence<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun write(bytes: ByteArray): Self {
    TODO("Not yet implemented")
  }

  override fun write(channel: FileChannel<*>): Self {
    TODO("Not yet implemented")
  }

  override fun write(text: CharSequence, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun write(lines: Iterable<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }

  override fun write(lines: Sequence<CharSequence>, charset: Charset): Self {
    TODO("Not yet implemented")
  }


  /**
   * ===============================================================
   * =                   Internal implementation                   =
   * ===============================================================
   */

  private fun preCreate(overwrite: Boolean) {
    if (this.isDirectory && this.exists()) throw PathExistsAndIsNotFileException(this)
    if (overwrite) delete()
  }

  private fun <R : File> copyOrMoveFile(
    isMove: Boolean,
    source: File,
    target: R,
    overwrite: Boolean,
    followLinks: Boolean,
  ): R = copyOrMoveFile(source, target, overwrite) { sourceFile, targetFile ->
    val options = listOfNotNull<CopyOption>(
      if (overwrite) StandardCopyOption.REPLACE_EXISTING else null,
      if (isMove.not()) StandardCopyOption.COPY_ATTRIBUTES else null,
      if (followLinks.not()) LinkOption.NOFOLLOW_LINKS else null,
    ).toTypedArray()
    when {
      isMove -> Files.move(sourceFile.toNioPath(), targetFile.toNioPath(), *options)
      else -> Files.copy(sourceFile.toNioPath(), targetFile.toNioPath(), *options)
    }
  }
}

internal interface FileBounded : IFile<FileBounded>
internal class FileImpl(chars: CharSequence) : FileBounded, TypedFileImpl<FileBounded>(chars) {
  override fun CharSequence.produce(): FileBounded {
    TODO("Not yet implemented")
  }

  override fun open(): FileChannel<FileBounded> =
    FileChannelImpl(this, nioPath)

  override fun create(overwrite: Boolean): FileBounded =
    apply { nioCreate(overwrite) }

  override fun createStrictly(overwrite: Boolean): FileBounded =
    apply { nioCreateStrictly(overwrite) }
}