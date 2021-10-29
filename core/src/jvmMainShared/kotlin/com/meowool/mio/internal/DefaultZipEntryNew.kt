@file:Suppress("NewApi")

package com.meowool.mio.internal

import com.meowool.mio.NioPath
import com.meowool.mio.IPath
import com.meowool.mio.Zip
import com.meowool.mio.ZipEntry
import java.nio.file.Files

/**
 * The zip entry backend implement with [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class DefaultZipEntryNew(
  nio: NioPath,
  override val holder: Zip?,
) : ZipEntry, DefaultPath(nio) {
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

  override val absolute: ZipEntry
    get() = absoluteImpl().toZipEntry()
  override val real: ZipEntry
    get() = realImpl().toZipEntry()
  override val normalized: ZipEntry
    get() = normalizedImpl().toZipEntry()
  override val symbolicLink: ZipEntry
    get() = symbolicLinkImpl().toZipEntry()

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

  override fun createParentDirectories(): ZipEntry =
    apply { super.createParentDirectories() }

  override fun toReal(followLinks: Boolean): ZipEntry = toRealImpl(followLinks).toZipEntry()

  private fun NioPath.toZipEntry(): ZipEntry = when {
    Files.isRegularFile(this) -> DefaultZipFileEntryNew(this, holder)
    Files.isDirectory(this) -> DefaultZipDirectoryEntryNew(this, holder)
    else -> DefaultZipEntryNew(this, holder)
  }
}