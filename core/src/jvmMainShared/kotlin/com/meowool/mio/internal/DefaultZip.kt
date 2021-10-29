@file:Suppress("NewApi", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.Directory
import com.meowool.mio.IFile
import com.meowool.mio.JavaZip
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
import com.meowool.mio.asFileOrNull
import com.meowool.mio.toMioZip
import kotlinx.coroutines.flow.Flow
import okio.Sink
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream

/**
 *
 *
 * @author 凛 (https://github.com/RinOrz)
 */
internal class DefaultZip(path: NioPath) : Zip, JavaZipBackend, DefaultFile(path) {

  override val javaZip: JavaZip by lazy { JavaZip(path.toString()) }

  private val rootEntry by lazy { NioPath("/") }

  private val entries by lazy {
    ConcurrentHashMap<String, DefaultZipEntry>(javaZip.size()).apply {
      val javaEntries = javaZip.entries()
      for (entry in javaEntries) put(entry.name, entry.toZipEntry())
    }
  }

  private val tempFiles by lazy { ConcurrentHashMap<String, IFile>() }

  // The real data of the cache entries
  private val data by lazy {
    createCache<String, Sink>(
      initialCapacity = entries.keys.size,
      weigher = { path, _ -> entries[path]?.size?.toInt() ?: -1 },
      evictedFetcher = { path -> null }
    )
  }

  override val absolute: Zip
    get() = absoluteImpl().toMioZip()
  override val real: Zip
    get() = realImpl().toMioZip()
  override val normalized: Zip
    get() = normalizedImpl().toMioZip()
  override val symbolicLink: Zip
    get() = symbolicLinkImpl().toMioZip()

  private fun createCondition(overwrite: Boolean) {
    if (this.isDirectory && this.exists()) throw PathExistsAndIsNotFileException(this)
    if (overwrite) delete()
  }

  override fun create(overwrite: Boolean): Zip = apply {
    createCondition(overwrite)
    createParentDirectories()
    if (this.exists().not()) ZipOutputStream(Files.newOutputStream(nioPath.normalize())).close()
  }

  override fun createStrictly(overwrite: Boolean): Zip = apply {
    createCondition(overwrite)
    if (this.exists()) throw PathAlreadyExistsException(
      this, reason = "The file already exists, it cannot be created again."
    )
    var parentPath = this.parent
    while (true) {
      if (parentPath?.exists() == false) throw ParentDirectoryNotExistsException(parentPath)
      parentPath = parentPath?.parent ?: break
    }
    ZipOutputStream(Files.newOutputStream(nioPath.normalize())).close()
  }

  override fun flow(depth: Int): Flow<ZipEntry> = kotlinx.coroutines.flow.flow {
    entries.values.forEach { entry ->
      entry.filterDepth(depth)?.let { emit(entry) }
    }
  }

  override fun list(depth: Int): List<ZipEntry> = entries.values.mapNotNull { entry ->
    entry.filterDepth(depth)
  }

  /**
   *
   * The [depth] argument -1 is the number of the entry separators, if the directory entry has one
   * more `/` at the end, needs to be removed:
   *
   * ```
   * depth = 1 ->
   *   foo.bar || dir/
   * depth = 2 ->
   *   dir/foo.bar || dir/subdirectory/
   * ```
   */
  @Suppress("GrazieInspection")
  private fun ZipEntry.filterDepth(depth: Int) = when {
    name.removeSuffix("/").count { it == '/' } == depth - 1 -> this
    else -> null
  }

  override fun addFile(entryPath: String, overwrite: Boolean): ZipFileEntry =
    this.joinFile(entryPath).create(overwrite)

  override fun addDirectory(entryPath: String, overwrite: Boolean): ZipDirectoryEntry =
    this.joinDir(entryPath).create(overwrite)

  override fun add(
    file: IFile,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
  ): ZipFileEntry {
    TODO("Not yet implemented")
  }

  override fun add(
    directory: Directory,
    recursively: Boolean,
    overwrite: Boolean,
    keepSources: Boolean,
    followLinks: Boolean,
    filter: (IPath) -> Boolean,
    onError: (path: IPath, throwable: Throwable) -> PathHandlingErrorSolution,
  ): ZipDirectoryEntry {
    TODO("Not yet implemented")
  }

  override fun replaceWith(zip: Zip, keepSources: Boolean, followLinks: Boolean): Zip {
    TODO("Not yet implemented")
  }

  override fun contains(entryPath: String): Boolean {
    if (entries.containsKey(entryPath.normalizeZipEntry(isDirectory = true))) return true
    return entries.containsKey(entryPath.normalizeZipEntry(isDirectory = false))
  }

  override fun join(vararg paths: IPath): ZipEntry =
    joinImpl(*paths, base = rootEntry).toZipEntry()

  override fun join(vararg paths: CharSequence): ZipEntry =
    joinImpl(*paths, base = rootEntry).toZipEntry()

  override fun joinFile(vararg paths: IPath): ZipFileEntry =
    joinImpl(*paths, base = rootEntry).toZipFileEntry()

  override fun joinFile(vararg paths: CharSequence): ZipFileEntry =
    joinImpl(*paths, base = rootEntry).toZipFileEntry()

  override fun joinDir(vararg paths: IPath): ZipDirectoryEntry =
    joinImpl(*paths, base = rootEntry).toZipDirEntry()

  override fun joinDir(vararg paths: CharSequence): ZipDirectoryEntry =
    joinImpl(*paths, base = rootEntry).toZipDirEntry()

  override inline fun div(path: IPath): ZipEntry = join(path)

  override inline fun div(path: CharSequence): ZipEntry = join(path)

  override fun close() = try {
    ZipOutputStream(nioPath.outputStream().buffered()).use { zos ->
      entries.values.forEach {
        it as JavaZipEntryBackend
        zos.putNextEntry(it.javaZipEntry)
        it.asFileOrNull()?.bufferedSink?.buffer?.writeTo(zos)
        zos.closeEntry()
      }
    }
  } finally {
    data.clear()
    entries.clear()
    javaZip.close()
  }

  fun <E> addEntry(entry: E) where E : ZipEntry, E : NioPathBackend {
    entries[entry.nioPath.normalizeZipEntry(entry.isDirectory)] = entry
  }

  fun <E> deleteEntry(entry: E): Boolean where E : ZipEntry, E : NioPathBackend {
    val path = entry.nioPath.normalizeZipEntry(entry.isDirectory)
    return entries.remove(path) != null
  }

  fun deleteDirEntry(path: String): Boolean =
    entries.remove(path.normalizeZipEntry(isDirectory = true)) != null

  fun moveEntry(from: String, to: String): NioPath {
    val deleted = entries.remove(from)!!
    entries[to] = deleted
    return deleted.nioPath
  }

  private fun JavaZipEntry.toZipEntry(): DefaultZipEntry = when {
    this.isDirectory -> TODO()
    else -> DefaultZipFileEntry(
      holder = this@ZipImpl,
      javaZipEntry = this
    )
  }

  private fun NioPath.toZipEntry(): ZipEntry = when {
    this.isRegularFile() || (!this.exists() && this.extension.isNotEmpty()) -> {
      entries.getOrElse(this.normalizeZipEntryPath(isDirectory = false).toString()) {
        this.toZipFileEntry()
      }
    }
    else -> entries.getOrElse(this.normalizeZipEntryPath(isDirectory = true).toString()) {
      this.toZipDirEntry()
    }
  }

  private fun NioPath.toZipFileEntry(): ZipFileEntry = DefaultZipFileEntry(
    holder = this@ZipImpl,
    javaZipEntry = JavaZipEntry(normalizeZipEntry(isDirectory = false))
  )

  private fun NioPath.toZipDirEntry(): ZipDirectoryEntry = TODO()

  class TempPath(
    path: IPath,
    deleteOnComplete: Boolean,
  ) : IPath by path

}