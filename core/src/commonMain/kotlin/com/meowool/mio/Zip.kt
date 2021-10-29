@file:Suppress("NOTHING_TO_INLINE", "SpellCheckingInspection")

package com.meowool.mio

/**
 * An object representing the file system and its path.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface Zip : IFile<Zip>, IPathGroup<Zip, Path, File, Directory>, Closeable {

  /**
   * Joins a sequence of [paths] into this zip archive and as a zip entry.
   *
   * If there has a root on the given sequence of [paths], the path where the root appears last is
   * taken as the beginning of the entry path.
   *
   * This is different from [IPath.join], [Zip.join] will add the path to the root entry of the
   * zip archive. In other words, the added path is the zip entry in the zip archive.
   *
   * For example:
   * ```
   * Zip("foo.zip").join("baz", "file.txt")  -> entry `/baz/file.txt` in `foo.zip`
   * Zip("foo.zip").join("baz", "/gav")      -> entry `/gav`          in `foo.zip`
   * Zip("/foo").join("/")                   -> entry `/`             in `foo.zip`
   * ```
   *
   * @return the zip entry after joined
   */
  override fun join(vararg paths: Path): ZipEntry

  /**
   * Joins a sequence of [paths] into this zip archive and as a zip entry.
   *
   * If there has a root on the given sequence of [paths], the path where the root appears last is
   * taken as the beginning of the entry path.
   *
   * This is different from [IPath.join], [Zip.join] will add the path to the root entry of the
   * zip archive. In other words, the added path is the zip entry in the zip archive.
   *
   * For example:
   * ```
   * Zip("foo.zip").join("baz", "file.txt")  -> entry `/baz/file.txt` in `foo.zip`
   * Zip("foo.zip").join("baz", "/gav")      -> entry `/gav`          in `foo.zip`
   * Zip("/foo").join("/")                   -> entry `/`             in `foo.zip`
   * ```
   *
   * @return the zip entry after joined
   */
  override fun join(vararg paths: CharSequence): ZipEntry

  /**
   * Joins a sequence of [paths] into this zip archive and as a zip directory entry.
   *
   * @return the zip entry of directory after joined
   * @see join for more details.
   */
  override fun joinDir(vararg paths: Path): ZipDirectoryEntry = this.join(*paths).asDir()

  /**
   * Joins a sequence of [paths] into this zip archive and as a zip directory entry.
   *
   * @return the zip entry of directory after joined
   * @see join for more details.
   */
  override fun joinDir(vararg paths: CharSequence): ZipDirectoryEntry = this.join(*paths).asDir()

  /**
   * Joins a sequence of [paths] into this zip archive and as a zip file entry.
   *
   * @return the zip entry of file after joined
   * @see join for more details.
   */
  override fun joinFile(vararg paths: Path): ZipFileEntry = this.join(*paths).asFile()

  /**
   * Joins a sequence of [paths] into this zip archive and as a zip file entry.
   *
   * @return the zip entry of file after joined
   * @see join for more details.
   */
  override fun joinFile(vararg paths: CharSequence): ZipFileEntry = this.join(*paths).asFile()

  /**
   * Closes this zip archive object, this will synchronize all the changes of the entries in this
   * object to the real file.
   *
   * @see flow
   * @see list
   */
  override fun close()
}

/**
 * Opens and returns the zip archive based on the path.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
expect fun Zip(first: CharSequence, vararg more: CharSequence): Zip

/**
 * Opens and returns the zip archive based on the given [path].
 */
expect fun Zip(path: Path): Zip

/**
 * Convert [CharSequence] to [Zip].
 *
 * @param more additional char sequence to be joined to form the path
 */
inline fun CharSequence.asZip(vararg more: CharSequence): Zip = Zip(this, *more)

/**
 * Convert [Path] to [Zip].
 */
inline fun Path.asZip(): Zip = Zip(this)

/**
 * Try to use the [IPath] as a [Zip], and return `null` if it already exists and is not a zip file.
 */
expect fun Path.asZipOrNull(): Zip?