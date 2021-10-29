@file:Suppress("NewApi")

package com.meowool.mio.internal

import com.meowool.mio.NioPath
import com.meowool.mio.IPath
import com.meowool.mio.Zip
import com.meowool.mio.ZipDirectoryEntry
import com.meowool.mio.ZipEntry
import java.nio.file.Files

/**
 * The zip file entry backend implement with [IoFile].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class DefaultZipDirectoryEntryNew(
  nio: NioPath,
  override val holder: Zip?,
) : ZipDirectoryEntry, DefaultDirectory(nio) {
  private val attrs get() = Files.readAttributes(nioPath, "zip:*")

  override val compressedSize: Long
    get() = attrs["compressedSize"] as? Long ?: -1
  override val crc: Long
    get() = attrs["crc"] as? Long ?: -1
  override var extra: ByteArray?
    get() = attrs["extra"] as? ByteArray
    set(value) {
      Files.setAttribute(nioPath, "zip:extra", value)
    }
  override var comment: String?
    get() = (attrs["comment"] as? ByteArray)?.decodeToString()
    set(value) {
      Files.setAttribute(nioPath, "zip:comment", value?.toByteArray())
    }

  override val absolute: ZipDirectoryEntry
    get() = absoluteImpl().toZipDirEntry()
  override val real: ZipDirectoryEntry
    get() = realImpl().toZipDirEntry()
  override val normalized: ZipDirectoryEntry
    get() = normalizedImpl().toZipDirEntry()
  override val symbolicLink: ZipDirectoryEntry
    get() = symbolicLinkImpl().toZipDirEntry()

  override fun create(overwrite: Boolean): ZipDirectoryEntry =
    apply { super.create(overwrite) }

  override fun createStrictly(overwrite: Boolean): ZipDirectoryEntry =
    apply { super.createStrictly(overwrite) }

  override fun createParentDirectories(): ZipDirectoryEntry =
    apply { super.createParentDirectories() }

  override fun join(vararg paths: IPath): ZipEntry = joinImpl(*paths).toZipEntry()

  override fun join(vararg paths: CharSequence): ZipEntry = joinImpl(*paths).toZipEntry()

  override fun div(path: IPath): ZipEntry = join(path)

  override fun div(path: CharSequence): ZipEntry = join(path)

  override fun joinToParent(vararg paths: CharSequence): ZipEntry =
    joinToParentImpl(*paths).toZipEntry()

  override fun joinToParent(vararg paths: IPath): ZipEntry =
    joinToParentImpl(*paths).toZipEntry()

  override fun relativeTo(target: CharSequence): ZipEntry =
    relativeToImpl(target).toZipEntry()

  override fun relativeTo(target: IPath): ZipEntry =
    relativeToImpl(target).toZipEntry()

  override fun toReal(followLinks: Boolean): ZipDirectoryEntry =
    toRealImpl(followLinks).toZipDirEntry()

  private fun NioPath.toZipEntry(): ZipEntry = DefaultZipEntryNew(this, holder)

  private fun NioPath.toZipDirEntry(): ZipDirectoryEntry = DefaultZipDirectoryEntryNew(this, holder)
}