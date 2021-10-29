package com.meowool.mio

/**
 * Any type of entry in the zip archive.
 *
 * @see ZipFileEntry
 * @see ZipDirectoryEntry
 */
typealias ZipEntry = IZipEntry<*>

/**
 * The interface represents to the entry in the zip archive.
 *
 * @param Self represents the return type of members (file entry, directory entry)
 * @author 凛 (https://github.com/RinOrz)
 */
interface IZipEntry<Self: IZipEntry<Self>> : IPath<Self> {

  /**
   * The zip archive that this entry belongs to, or `null` if this entry has been deleted.
   */
  val holder: Zip?

  /**
   * Returns the compressed size of this entry.
   */
  val compressedSize: Long

  /**
   * Returns the crc (Cyclic Redundancy Check) value of uncompressed data of this entry.
   * [Related Information](https://en.wikipedia.org/wiki/Cyclic_redundancy_check)
   */
  val crc: Long

  /**
   * The optional extra field data of this entry.
   * [Related Information](https://en.wikipedia.org/wiki/ZIP_(file_format)#Extra_field)
   */
  var extra: ByteArray?

  /**
   * The optional comment string of this entry.
   */
  var comment: String?

  /**
   * The parent directory of this path, if the path does not have a parent directory then it
   * is `null` (e.g. this path is on the root path).
   */
  override val parent: ZipDirectoryEntry?

  /**
   * Joins a sequence of [paths] to this entry.
   *
   * @see IPath.join for more details.
   */
  override fun join(vararg paths: Path): ZipEntry

  /**
   * Joins a sequence of [paths] to this entry.
   *
   * @see IPath.join for more details.
   */
  override fun join(vararg paths: CharSequence): ZipEntry

  /**
   * Joins a [path] to this entry.
   *
   * @see IPath.div for more details.
   */
  override operator fun div(path: Path): ZipEntry

  /**
   * Joins a [path] to this entry.
   *
   * @see IPath.div for more details.
   */
  override fun join(path: Path): ZipEntry = div(path)

  /**
   * Joins a [path] to this entry.
   *
   * @see IPath.div for more details.
   */
  override operator fun div(path: CharSequence): ZipEntry

  /**
   * Joins a [path] to this entry.
   *
   * @see IPath.div for more details.
   */
  override fun join(path: CharSequence): ZipEntry = div(path)

  /**
   * Joins a sequence of [paths] to the parent entry of this entry, if the parent entry is `null`,
   * joined to root entry.
   *
   * @see IPath.div for more details.
   */
  override fun joinToParent(vararg paths: Path): ZipEntry
}