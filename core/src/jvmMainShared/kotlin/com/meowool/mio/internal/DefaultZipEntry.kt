@file:Suppress("NewApi", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.JavaZipEntry
import com.meowool.mio.MediaType
import com.meowool.mio.NioPath
import com.meowool.mio.IPath
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.SystemSeparator
import com.meowool.mio.ZipDirectoryEntry
import com.meowool.mio.ZipEntry
import com.meowool.mio.asFile
import com.meowool.mio.createTempFile
import com.meowool.mio.toMioPath
import com.meowool.mio.toNioPath
import com.meowool.sweekt.isAndroidSystem
import com.meowool.sweekt.isLinuxSystem
import com.meowool.sweekt.substringAfter
import com.meowool.sweekt.toReadableSize
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import kotlin.io.path.exists

/**
 * @author 凛 (https://github.com/RinOrz)
 */
internal open class DefaultZipEntry(
  override val holder: DefaultZip,
  override var javaZipEntry: JavaZipEntry,
  override var nioPath: NioPath = NioPath(javaZipEntry.name),
) : ZipEntry, JavaZipEntryBackend, NioPathBackend {

  override val compressedSize: Long
    get() = javaZipEntry.compressedSize

  override val crc: Long
    get() = javaZipEntry.crc

  override var extra: ByteArray?
    get() = javaZipEntry.extra
    set(value) {
      javaZipEntry.extra = value
    }

  override var comment: String?
    get() = javaZipEntry.comment
    set(value) {
      javaZipEntry.comment = value
    }

  override var size: Long
    get() = javaZipEntry.size
    set(value) {
      javaZipEntry.size = value
    }

  override val absolute: ZipEntry
    get() = if (isAbsolute) this else nioPath.remake()

  override val real: ZipEntry
    get() = absolute

  override val normalized: ZipEntry
    get() = nioPath.remake()

  override val symbolicLink: ZipEntry
    get() = this

  override val isAbsolute: Boolean
    get() = nioPath.isAbsolute

  override var name: String
    get() = nioPath.fileName?.toString().orEmpty()
    set(value) {
      val target = nioPath.resolveSibling(value)
      if (target.exists()) throw PathAlreadyExistsException(
        path = target.toMioPath(),
        reason = "Cannot rename because the target path already exists!"
      )
      update(
        holder.moveEntry(
          nioPath.normalizeZipEntry(isDirectory),
          target.normalizeZipEntry(isDirectory)
        )
      )
    }

  override val parent: ZipDirectoryEntry?
    get() = nioPath.parent?.toString()?.let { p ->
      holder.joinDir(p).takeIf { it.exists() }
    }

  override var lastModifiedTime: Long
    get() = javaZipEntry.lastModifiedTime.toMillis()
    set(value) {
      javaZipEntry.lastModifiedTime = FileTime.fromMillis(value)
    }

  override var lastAccessTime: Long
    get() = javaZipEntry.lastAccessTime.toMillis()
    set(value) {
      javaZipEntry.lastAccessTime = FileTime.fromMillis(value)
    }

  override var creationTime: Long
    get() = javaZipEntry.creationTime.toMillis()
    set(value) {
      javaZipEntry.creationTime = FileTime.fromMillis(value)
    }

  override val isRoot: Boolean
    get() = toString() == SystemSeparator

  override var isReadable: Boolean = true
  override var isWritable: Boolean = true
  override var isExecutable: Boolean = true
  override var isHidden: Boolean
    get() = name[0] == '.'
    set(value) {
      when {
        value -> if (isHidden.not()) name = ".$name"
        name.first() == '.' -> name = name.substringAfter(1)
      }
    }

  override val isRegularFile: Boolean
    get() = javaZipEntry.isDirectory.not()

  override val isDirectory: Boolean
    get() = javaZipEntry.isDirectory

  override val isSymbolicLink: Boolean
    get() = false

  override val isOther: Boolean
    get() = false

  override val readableSize: String
    get() = size.toReadableSize()

  override val contentType: String
    get() = when {
      isDirectory && isLinuxSystem -> MediaType.Directory.value[0]
      isDirectory && isAndroidSystem -> MediaType.Directory.value[1]
      else -> runCatching {
        Files.probeContentType(this.asFile().copyTo(createTempFile()).toNioPath())
      }.getOrNull().orEmpty()
    }

  override val key: Any?
    get() = TODO("Not yet implemented")

  override fun createParentDirectories(): ZipEntry = apply {
    nioPath.parent?.forEach {
      val dir = it.toString()
      if (holder.contains(dir).not()) holder.addDirectory(dir)
    }
  }

  override fun toReal(followLinks: Boolean): ZipEntry = this

  override fun exists(followLinks: Boolean): Boolean {
    TODO("Not yet implemented")
  }

  override fun notExists(followLinks: Boolean): Boolean {
    TODO("Not yet implemented")
  }

  override fun join(vararg paths: IPath): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun join(vararg paths: CharSequence): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun div(path: IPath): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun div(path: CharSequence): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun joinToParent(vararg paths: CharSequence): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun joinToParent(vararg paths: IPath): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun relativeTo(target: CharSequence): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun relativeTo(target: IPath): ZipEntry {
    TODO("Not yet implemented")
  }

  override fun split(): List<String> = nioPath.normalize().map { it.toString() }

  override fun startsWith(path: IPath): Boolean {
    TODO("Not yet implemented")
  }

  override fun endsWith(path: IPath): Boolean {
    TODO("Not yet implemented")
  }

  override fun <T : IPath> linkTo(target: T): T {
    TODO("Not yet implemented")
  }

  override fun <T : IPath> linkSymbolTo(target: T): T {
    TODO("Not yet implemented")
  }

  override fun compareTo(otherPath: String): Int {
    TODO("Not yet implemented")
  }

  override fun compareTo(other: IPath): Int {
    TODO("Not yet implemented")
  }

  override fun equals(other: Any?): Boolean {
    TODO("Not yet implemented")
  }

  override fun hashCode(): Int {
    TODO("Not yet implemented")
  }

  override fun toString(): String {
    TODO("Not yet implemented")
  }

  private fun NioPath.remake() = DefaultZipFileEntry(
    holder = holder,
    javaZipEntry = javaZipEntry,
    nioPath = normalizeZipEntryPath(isDirectory = false)
  )

  private fun update(path: NioPath) {
    nioPath = path
    javaZipEntry = JavaZipEntry(path.toString()).also {
      it.compressedSize = compressedSize
      it.crc = crc
      it.extra = extra
      it.comment = comment
      it.size = size
      it.time = lastModifiedTime

      @Suppress("NewApi")
      runCatching {
        it.creationTime = FileTime.fromMillis(creationTime)
        it.lastAccessTime = FileTime.fromMillis(lastAccessTime)
        it.lastModifiedTime = FileTime.fromMillis(lastModifiedTime)
      }
    }
  }
}