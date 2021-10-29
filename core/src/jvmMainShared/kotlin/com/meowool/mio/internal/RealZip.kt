@file:Suppress("NewApi")

package com.meowool.mio.internal

import com.meowool.mio.Directory
import com.meowool.mio.IFile
import com.meowool.mio.NioPath
import com.meowool.mio.ParentDirectoryNotExistsException
import com.meowool.mio.IPath
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotFileException
import com.meowool.mio.PathHandlingErrorSolution
import com.meowool.mio.Zip
import com.meowool.mio.ZipDirectoryEntry
import com.meowool.mio.ZipEntry
import com.meowool.mio.ZipFileEntry
import com.meowool.mio.toMioDirectory
import com.meowool.mio.toNioPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.streams.toList

/**
 * The zip backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class DefaultZipNew(zipPath: NioPath) : Zip, DefaultFile(zipPath) {
  private val url = URI.create("jar:file:$nioPath")
  private var fs: FileSystem = createFileSystem()

  /** The root path of this zip archive */
  private val root = fs.getPath(fs.separator)
  override val absolute: Zip
    get() = TODO("Not yet implemented")
  override val real: Zip
    get() = TODO("Not yet implemented")
  override val normalized: Zip
    get() = TODO("Not yet implemented")
  override val symbolicLink: Zip
    get() = TODO("Not yet implemented")

  override fun create(overwrite: Boolean): Zip = apply {
    if (this.exists() && this.isDirectory) throw PathExistsAndIsNotFileException(this)
    if (this.exists() && overwrite) delete()
    createParentDirectories()
    if (this.exists().not()) create()
  }

  override fun createStrictly(overwrite: Boolean): Zip = apply {
    if (this.exists() && this.isDirectory) throw PathExistsAndIsNotFileException(this)
    if (this.exists() && overwrite) delete()
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The zip archive already exists, it cannot be created again."
    )
    var parentPath = this.parent
    while (true) {
      if (parentPath?.exists() == false) throw ParentDirectoryNotExistsException(parentPath)
      parentPath = parentPath?.parent ?: break
    }
    create()
  }

  override fun flow(depth: Int): Flow<ZipEntry> = streamFlow {
    when (depth) {
      1 -> Files.list(root)
      else -> Files.walk(root, depth).filter { it != root }
    }
  }.map(::getZipEntry)

  override fun list(depth: Int): List<ZipEntry> = when (depth) {
    1 -> Files.list(root)
    else -> Files.walk(root, depth).filter { it != root }
  }.map(::getZipEntry).toList()

  override fun addFile(entryPath: String, overwrite: Boolean): ZipFileEntry {
    val nioPath = fs.getPath(entryPath).apply {
      if (exists() && overwrite) deleteIfExists()
      parent?.createDirectories()
      createFile()
    }
    return DefaultZipFileEntryNew(nioPath, this)
  }

  override fun addDirectory(entryPath: String, overwrite: Boolean): ZipDirectoryEntry {
    val nioPath = fs.getPath(entryPath).toMioDirectory().create(overwrite).toNioPath()
    return DefaultZipDirectoryEntryNew(nioPath, this)
  }

  override fun add(
    file: IFile,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
  ): ZipFileEntry = DefaultZipFileEntryNew(
    nio = when {
      keepSources -> file.copyInto(
        target = root.toMioDirectory(), overwrite, followLinks
      )
      else -> file.moveInto(
        target = root.toMioDirectory(), overwrite, followLinks
      )
    }.toNioPath(),
    holder = this
  )

  override fun add(
    directory: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> PathHandlingErrorSolution,
  ): ZipDirectoryEntry = DefaultZipDirectoryEntryNew(
    nio = when {
      keepSources -> directory.copyInto(
        target = root.toMioDirectory(), recursively, overwrite, followLinks, filter, onError
      )
      else -> directory.moveInto(
        target = root.toMioDirectory(), recursively, overwrite, followLinks, filter, onError
      )
    }.toNioPath(),
    holder = this
  )

  override fun replaceWith(zip: Zip, keepSources: Boolean, followLinks: Boolean): Zip {
    TODO("Not yet implemented")
  }

  override fun contains(entryPath: String): Boolean {
    TODO("Not yet implemented")
  }

  override fun copyTo(target: IFile, overwrite: Boolean, followLinks: Boolean): Zip {
    TODO("Not yet implemented")
  }

  override fun copyInto(target: Directory, overwrite: Boolean, followLinks: Boolean): Zip {
    TODO("Not yet implemented")
  }

  override fun moveTo(target: IFile, overwrite: Boolean, followLinks: Boolean): Zip {
    TODO("Not yet implemented")
  }

  override fun moveInto(target: Directory, overwrite: Boolean, followLinks: Boolean): Zip {
    TODO("Not yet implemented")
  }

  override fun join(vararg paths: IPath): ZipEntry =
    getZipEntry(paths.fold(root) { acc, path ->
      acc.resolve(path.toNioPath())
    })

  override fun join(vararg paths: CharSequence): ZipEntry =
    getZipEntry(paths.fold(root) { acc, path ->
      acc.resolve(path.toString())
    })

  override fun joinFile(vararg paths: IPath): ZipFileEntry {
    TODO("Not yet implemented")
  }

  override fun joinFile(vararg paths: CharSequence): ZipFileEntry {
    TODO("Not yet implemented")
  }

  override fun joinDir(vararg paths: IPath): ZipDirectoryEntry {
    TODO("Not yet implemented")
  }

  override fun joinDir(vararg paths: CharSequence): ZipDirectoryEntry {
    TODO("Not yet implemented")
  }

  override fun div(path: IPath): ZipEntry = join(path)

  override fun div(path: CharSequence): ZipEntry = join(path)

  override fun close() = fs.close()

  private fun createFileSystem(): FileSystem {
    runCatching {
      val fs = FileSystems.getFileSystem(url)
      if (fs.isOpen) return fs
      fs.close()
    }
    return FileSystems.newFileSystem(url, mapOf("create" to true))
  }

  private fun create() {
    fs.close()
    fs = createFileSystem()
  }

  private fun getZipEntry(nio: NioPath): ZipEntry = when {
    Files.isDirectory(nio) -> DefaultZipDirectoryEntryNew(nio, this)
    Files.isRegularFile(nio) -> DefaultZipFileEntryNew(nio, this)
    else -> DefaultZipEntryNew(nio, this)
  }
}