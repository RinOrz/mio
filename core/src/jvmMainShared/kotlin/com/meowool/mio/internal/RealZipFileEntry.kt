@file:Suppress("NewApi")

package com.meowool.mio.internal

import com.meowool.mio.Charset
import com.meowool.mio.IFile
import com.meowool.mio.NioPath
import com.meowool.mio.IPath
import com.meowool.mio.Zip
import com.meowool.mio.ZipEntry
import com.meowool.mio.ZipFileEntry
import okio.Source
import java.nio.file.Files

/**
 * The zip file entry backend implement with [IoFile].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class DefaultZipFileEntryNew(
  nio: NioPath,
  override val holder: Zip?,
) : ZipFileEntry, DefaultFile(nio) {
  private val attrs by lazy { Files.readAttributes(nio, "zip:*") }

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

  override val absolute: ZipFileEntry
    get() = absoluteImpl().toZipFileEntry()
  override val real: ZipFileEntry
    get() = realImpl().toZipFileEntry()
  override val normalized: ZipFileEntry
    get() = normalizedImpl().toZipFileEntry()
  override val symbolicLink: ZipFileEntry
    get() = symbolicLinkImpl().toZipFileEntry()

  override fun create(overwrite: Boolean): ZipFileEntry = apply {
    super.create(overwrite)
  }

  override fun createStrictly(overwrite: Boolean): ZipFileEntry = apply {
    super.createStrictly(overwrite)
  }

  override fun replaceWith(file: IFile, keepSources: Boolean, followLinks: Boolean): ZipFileEntry =
    apply { super.replaceWith(file, keepSources, followLinks) }

  override fun append(bytes: ByteArray): ZipFileEntry = apply { super.append(bytes) }

  override fun append(source: Source): ZipFileEntry = apply { super.append(source) }

  override fun append(text: CharSequence, charset: Charset): ZipFileEntry =
    apply { super.append(text, charset) }

  override fun append(lines: Iterable<CharSequence>, charset: Charset): ZipFileEntry =
    apply { super.append(lines, charset) }

  override fun append(lines: Sequence<CharSequence>, charset: Charset): ZipFileEntry =
    apply { super.append(lines, charset) }

  override fun write(bytes: ByteArray): ZipFileEntry = apply { super.write(bytes) }

  override fun write(source: Source): ZipFileEntry = apply { super.write(source) }

  override fun write(text: CharSequence, charset: Charset): ZipFileEntry =
    apply { super.write(text, charset) }

  override fun write(lines: Iterable<CharSequence>, charset: Charset): ZipFileEntry =
    apply { super.write(lines, charset) }

  override fun write(lines: Sequence<CharSequence>, charset: Charset): ZipFileEntry =
    apply { super.write(lines, charset) }

  override fun createParentDirectories(): ZipFileEntry = apply { super.createParentDirectories() }

  override fun toReal(followLinks: Boolean): ZipFileEntry = toRealImpl(followLinks).toZipFileEntry()

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

  private fun NioPath.toZipEntry(): ZipEntry = DefaultZipEntryNew(this, holder)

  private fun NioPath.toZipFileEntry() = DefaultZipFileEntryNew(this, holder)
}