@file:Suppress("FunctionName", "NOTHING_TO_INLINE", "NO_ACTUAL_FOR_EXPECT")

package com.meowool.mio

import com.meowool.sweekt.toReadableSize

/**
 * An object representing the path.
 *
 * [Implement reference java new io](https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/nio/file/Path.java)
 * [Implement reference multiplatform okio](https://github.com/square/okio/blob/master/okio/src/commonMain/kotlin/okio/Path.kt)
 * [Implement reference python os.path](https://github.com/python/cpython/blob/3.9/Lib/posixpath.py)
 *
 * @param Self represents the return type of members (file, directory, etc...)
 * @author 凛 (https://github.com/RinOrz)
 */
interface IPath<Self: IPath<Self>> : Comparable<Self> {

  /**
   * Returns the absolute path of this file or directory.
   */
  val absolute: Self

  /**
   * Returns the string of absolute path of this file or directory.
   *
   * @see absolute
   */
  val absoluteString: String

  /**
   * Returns an absolute path represent the real path of this file or directory located. If this
   * is a symbolic link, it will be resolved to the final target.
   */
  val real: Self

  /**
   * Returns the string of an absolute path represent the real path of this file or directory
   * located. If this is a symbolic link, it will be resolved to the final target.
   *
   * @see real
   */
  val realString: String get() = real.toString()

  /**
   * Returns the normalized path of this path.
   *
   * The following steps may occur:
   *   1. Converts all separators to system flavor
   *   2. Converts user home symbol (`~`) to real path of user home directory
   *   3. Removes the duplicate slash like `//` but will protect UNC path
   *   4. Removes the all useless single dot like `./`
   *   5. Resolves the all parent paths symbols like `..`
   *
   * For example:
   * ```
   * `/foo///.`              ->   `/foo`
   * `/foo/./`               ->   `/foo`
   * `/foo/../bar`           ->   `/bar`
   * `/foo/../bar/`          ->   `/bar`
   * `/foo/../bar/../baz`    ->   `/baz`
   * `//foo//./bar`          ->   `/foo/bar`
   * `/../..`                ->   `/`
   * `foo/bar/..`            ->   `foo`
   * `../foo/`               ->   `../foo/`
   * `foo/../../bar/`        ->   `../bar/`
   * `foo/../bar`            ->   `bar`
   * `//server/foo/..//bar`  ->   `//server/bar`
   * `C:\..\..\bar`          ->   `C:\bar`
   * `C:..\bar`              ->   `C:..\bar`
   * `~`                     ->   `home/user/`
   * `~/foo/../bar/`         ->   `home/user/bar`
   * `~/../bar`              ->   `home/user/bar`
   * ```
   *
   * @see normalizedString
   */
  val normalized: Self

  /**
   * Returns the string of normalized path of this path.
   *
   * The following steps may occur:
   *   1. Converts all separators to system flavor
   *   2. Converts user home symbol (`~`) to real path of user home directory
   *   3. Removes the duplicate slash like `//` but will protect UNC path
   *   4. Removes the all useless single dot like `./`
   *   5. Resolves the all parent paths symbols like `..`
   *
   * For example:
   * ```
   * `/foo///.`              ->   `/foo`
   * `/foo/./`               ->   `/foo`
   * `/foo/../bar`           ->   `/bar`
   * `/foo/../bar/`          ->   `/bar`
   * `/foo/../bar/../baz`    ->   `/baz`
   * `//foo//./bar`          ->   `/foo/bar`
   * `/../..`                ->   `/`
   * `foo/bar/..`            ->   `foo`
   * `../foo/`               ->   `../foo/`
   * `foo/../../bar/`        ->   `../bar/`
   * `foo/../bar`            ->   `bar`
   * `//server/foo/..//bar`  ->   `//server/bar`
   * `C:\..\..\bar`          ->   `C:\bar`
   * `C:..\bar`              ->   `C:..\bar`
   * `~`                     ->   `home/user/`
   * `~/foo/../bar/`         ->   `home/user/bar`
   * `~/../bar`              ->   `home/user/bar`
   * ```
   *
   * @see normalized
   */
  val normalizedString: String

  /**
   * If this path is a symbolic link, returns the linked target, otherwise return itself.
   */
  val symbolicLink: Self

  /**
   * The name of the file or directory of this path, which is the last element of [IPath.split].
   */
  var name: String

  /**
   * Usually represents the volume label of the Windows path, for example, the volume label on the
   * `C:\\Windows` path is `C:`, if this path is not a Windows path, or it does not have a volume
   * label, null will be returned.
   *
   * Note that paths that start with a volume label are not necessarily absolute paths. For
   * example, the path `C:notepad.exe` is relative to whatever the current working directory is on
   * the `C:` drive.
   */
  val volumeLabel: String?

  /**
   * The parent directory of this path, if the path has no parent directory then it is `null`.
   *
   * For example:
   * ```
   * "foo/bar"        -> `foo`
   * "/foo"           -> `/`
   * "/", "../", "."  -> `null`
   *
   * ## Windows path.
   * "C:"             -> `null`
   * "C:\\"           -> `null`
   * "C:\\Windows"    -> `C:\\`
   *
   * ## UNC path has no parent.
   * "\\\\softer"     -> `null`
   * "//softer"       -> `null`
   *
   * ## Relative path without parent, need to get `absolute` path first if to get the real parent.
   * "file.txt"       -> `null`
   *
   * ## The parent property will not resolve any `..` in relative paths.
   * "foo/../../"     -> "foo/../"
   * ```
   *
   * @see parentString
   */
  val parent: Directory?

  /**
   * The parent path string of this path, if the path has no parent directory then it is `null`.
   *
   * @see parent
   */
  val parentString: String?

  /**
   * The time in milliseconds of last modification.
   *
   * If the file system implementation does not support a time stamp to indicate the time of
   * last modification then this property returns an implementation specific default value,
   * typically milliseconds representing the epoch (1970-01-01T00:00:00Z).
   */
  var lastModifiedTime: Long

  /**
   * The time in milliseconds of last access.
   *
   * If the file system implementation does not support a time stamp to indicate the time of
   * last access then this property returns an implementation specific default value, typically
   * the [lastModifiedTime] or milliseconds representing the epoch (1970-01-01T00:00:00Z).
   */
  var lastAccessTime: Long

  /**
   * The creation time in milliseconds is the time that the file was created.
   *
   * If the file system implementation does not support a time stamp to indicate the time when
   * the file was created then this property returns an implementation specific default value,
   * typically the [lastModifiedTime] or milliseconds representing the epoch (1970-01-01T00:00:00Z).
   */
  var creationTime: Long

  /**
   * Returns `true` if there is a root separator on this path.
   */
  val hasRoot: Boolean

  /**
   * Returns `true` if this path has no parent path, represents that this is a root path.
   */
  val isRoot: Boolean

  /**
   * Returns `true` if the path is absolute.
   *
   * An absolute path is complete in that it doesn't need to be combined with other path
   * information in order to locate a file or directory.
   */
  val isAbsolute: Boolean get() = hasRoot

  /**
   * Returns `true` if the path is relative.
   */
  val isRelative: Boolean get() = hasRoot.not()

  /**
   * This path whether is readable.
   */
  var isReadable: Boolean

  /**
   * This path whether is writable.
   */
  var isWritable: Boolean

  /**
   * This path whether is executable.
   */
  var isExecutable: Boolean

  /**
   * This is whether a hidden path.
   *
   * The exact definition of hidden is platform or provider dependent.
   * On UNIX for example is considered to be hidden if its name begins with a dot.
   * On Windows is considered hidden if it isn't a directory and the [isHidden] attribute is set.
   */
  var isHidden: Boolean

  /**
   * Returns `true` if this path exists and is a regular file with opaque content.
   */
  val isRegularFile: Boolean

  /**
   * Returns `true` if this path exists and is a directory.
   */
  val isDirectory: Boolean

  /**
   * Returns `true` if this path exists and is a symbolic link.
   */
  val isSymbolicLink: Boolean

  /**
   * Returns `true` if this path exists and something other than a regular file, directory or
   * symbolic link.
   */
  val isOther: Boolean

  /**
   * The size of this path (in bytes).
   *
   * If changes the number of bytes in the file to [size] and the new size is smaller. This will
   * remove bytes from the end. It will add empty bytes to the end if it is larger. If the path is
   * not [isRegularFile], the changes will fail and nothing will happen, because the size of
   * non-file path is system-specific implemented, so cannot change it.
   *
   * Note that the size may differ from the actual size on the file system due to compression,
   * support for sparse files, or other reasons.
   */
  var size: Long

  /**
   * Returns a readable size string.
   *
   * @see size
   * @see com.meowool.sweekt.toReadableSize for more details
   */
  val readableSize: String get() = size.toReadableSize()

  /**
   * Probes the content type (MIME type) of this path.
   *
   * Note that this property is not necessarily accurate, even empty. If you want to get very
   * accurate results, you can use other content detection libraries such as
   * [Apache-Tika](https://tika.apache.org/).
   */
  val contentType: String

  /**
   * Returns an object that uniquely identifies the given path.
   */
  val key: Any

  /**
   * Returns `true` if the file or directory of this path is exists.
   *
   * @param followLinks if this is a symbolic link, whether to ensure the final real target of link
   *   is exists.
   *
   * @see notExists
   */
  fun exists(followLinks: Boolean = true): Boolean

  /**
   * Returns `true` if the file or directory of this path does not exist.
   *
   * @param followLinks if this is a symbolic link, whether to ensure the final real target of link
   *   is not exists.
   *
   * @see exists
   */
  fun notExists(followLinks: Boolean = true): Boolean

  /**
   * Joins a [path] to this path.
   *
   * If the given [path] has a root, returns it's directly.
   *
   * This is best explained by analogy. Imagine you're in a command prompt and this path is your
   * current command line path. You type "cd [path]". The path after joined is the command line
   * path you'd end up in.
   *
   * For example:
   * ```
   * Path("foo/bar") / "gav"           ->  `foo/bar/gav`
   * Path("foo/bar") / "/baz"          -> `/baz`
   * Path("/foo") / "/"                -> `/`
   *
   * ## Windows path.
   * Path("C:") / "/foo"               -> `/foo`
   * Path("C:\\") / "Windows"          -> `D:\\Windows`
   *
   * ## UNC path.
   * Path("//softer") / "foo"          -> `\\\\`
   *
   * ## User home path symbol.
   * Path("//softer") / "~"            -> `~/foo`
   * ```
   *
   * @return the path after joined
   */
  operator fun div(path: Path): Path

  /**
   * Joins a [path] to this path.
   *
   * If the given [path] has a root, returns it's directly.
   *
   * This is best explained by analogy. Imagine you're in a command prompt and this path is your
   * current command line path. You type "cd [path]". The path after joined is the command line
   * path you'd end up in.
   *
   * For example:
   * ```
   * Path("foo/bar") / "gav"           ->  `foo/bar/gav`
   * Path("foo/bar") / "/baz"          -> `/baz`
   * Path("/foo") / "/"                -> `/`
   *
   * ## Windows path.
   * Path("C:") / "/foo"               -> `/foo`
   * Path("C:\\") / "Windows"          -> `D:\\Windows`
   *
   * ## UNC path.
   * Path("//softer") / "foo"          -> `\\\\`
   *
   * ## User home path symbol.
   * Path("//softer") / "~"            -> `~/foo`
   * ```
   *
   * @return the path after joined
   */
  operator fun div(path: CharSequence): Path

  /**
   * Joins a [path] to this path.
   *
   * @return the path after joined
   * @see div for more details.
   */
  fun join(path: Path): Path = div(path)

  /**
   * Joins a [path] to this path.
   *
   * @return the path after joined
   * @see div for more details.
   */
  fun join(path: CharSequence): Path = div(path)

  /**
   * Joins a sequence of [paths] to this path.
   *
   * If there has a root on the given sequence of [paths], the path where the root appears last is
   * taken as the beginning of the path.
   *
   * This is best explained by analogy. Imagine you're in a command prompt and this path is your
   * current command line path. You type "cd [paths]". The path after joined is the command line
   * path you'd end up in.
   *
   * For example:
   * ```
   * Path("foo/bar").join("gav")           ->  `foo/bar/gav`
   * Path("foo/bar").join("baz", "/gav")   -> `/gav`
   * Path("/foo").join("/")                -> `/`
   *
   * ## Windows path.
   * Path("C:").join("foo", "bar")         -> `C:foo/bar`
   * Path("C:\\").join("Windows", "D:")    -> `D:`
   *
   * ## UNC path.
   * Path("//softer").join("foo", "\\\\")  -> `\\\\`
   *
   * ## User home path symbol.
   * Path("//softer").join("~", "foo")     -> `~/foo`
   * ```
   *
   * @return the path after joined
   */
  fun join(vararg paths: Path): Path

  /**
   * Joins a sequence of [paths] to this path.
   *
   * If there has a root on the given sequence of [paths], the path where the root appears last is
   * taken as the beginning of the path.
   *
   * This is best explained by analogy. Imagine you're in a command prompt and this path is your
   * current command line path. You type "cd [paths]". The path after joined is the command line
   * path you'd end up in.
   *
   * For example:
   * ```
   * Path("foo/bar").join("gav")           ->  `foo/bar/gav`
   * Path("foo/bar").join("baz", "/gav")   -> `/gav`
   * Path("/foo").join("/")                -> `/`
   *
   * ## Windows path.
   * Path("C:").join("foo", "bar")         -> `C:foo/bar`
   * Path("C:\\").join("Windows", "D:")    -> `D:`
   *
   * ## UNC path.
   * Path("//softer").join("foo", "\\\\")  -> `\\\\`
   *
   * ## User home path symbol.
   * Path("//softer").join("~", "foo")     -> `~/foo`
   * ```
   *
   * @return the path after joined
   */
  fun join(vararg paths: CharSequence): Path

  /**
   * Joins a sequence of [paths] of the directory to this path.
   *
   * @return the path of directory after joined
   * @see join for more details.
   */
  fun joinDir(vararg paths: Path): Directory = this.join(*paths).asDir()

  /**
   * Joins a sequence of [paths] of the directory to this path.
   *
   * @return the path of directory after joined
   * @see join for more details.
   */
  fun joinDir(vararg paths: CharSequence): Directory = this.join(*paths).asDir()

  /**
   * Joins a sequence of [paths] of the file to this path.
   *
   * @return the path of file after joined
   * @see join for more details.
   */
  fun joinFile(vararg paths: Path): File = this.join(*paths).asFile()

  /**
   * Joins a sequence of [paths] of the file to this path.
   *
   * @return the path of file after joined
   * @see join for more details.
   */
  fun joinFile(vararg paths: CharSequence): File = this.join(*paths).asFile()

  /**
   * Joins a sequence of [paths] to the parent path of this path, if the parent path is `null`,
   * joined to root path.
   *
   * If there has a root on the given sequence of [paths], the path where the root appears last is
   * taken as the beginning of the path.
   *
   * For example:
   * ```
   * Path("foo/bar").joinToParent("gav")  -> `foo/gav`
   * Path("foo/bar").joinToParent("/gav") -> `/gav`
   * Path("foo/bar").joinToParent("~/")   -> `~/`
   * ```
   *
   * @return the path after joined
   * @see join
   */
  fun joinToParent(vararg paths: Path): Path =
    parent?.join(*paths) ?: EmptyRootPath.join(*paths)

  /**
   * Joins a sequence of [paths] to the parent path of this path, if the parent path is `null`,
   * joined to root path.
   *
   * If there has a root on the given sequence of [paths], the path where the root appears last is
   * taken as the beginning of the path.
   *
   * For example:
   * ```
   * Path("foo/bar").joinToParent("gav")  -> `foo/gav`
   * Path("foo/bar").joinToParent("/gav") -> `/gav`
   * Path("foo/bar").joinToParent("~/")   -> `~/`
   * ```
   *
   * @return the path after joined
   * @see join
   */
  fun joinToParent(vararg paths: CharSequence): Path =
    parent?.join(*paths) ?: EmptyRootPath.join(*paths)

  /**
   * Joins a sequence of [paths] of the directory to the parent path of this path, if the parent
   * path is `null`, joined to root path.
   *
   * @return the path of directory after joined
   * @see joinToParent for more details.
   */
  fun joinDirToParent(vararg paths: Path): Directory = this.joinToParent(*paths).asDir()

  /**
   * Joins a sequence of [paths] of the directory to the parent path of this path, if the parent
   * path is `null`, joined to root path.
   *
   * @return the path of directory after joined
   * @see joinToParent for more details.
   */
  fun joinDirToParent(vararg paths: CharSequence): Directory = this.joinToParent(*paths).asDir()

  /**
   * Joins a sequence of [paths] of the file to the parent path of this path, if the parent path
   * is `null`, joined to root path.
   *
   * @return the path of file after joined
   * @see joinToParent for more details.
   */
  fun joinFileToParent(vararg paths: Path): File = this.joinToParent(*paths).asFile()

  /**
   * Joins a sequence of [paths] of the file to the parent path of this path, if the parent path
   * is `null`, joined to root path.
   *
   * @return the path of file after joined
   * @see joinToParent for more details.
   */
  fun joinFileToParent(vararg paths: CharSequence): File = this.joinToParent(*paths).asFile()

  /**
   * Returns a relative path from this path to the given [target] path, or an empty path if both
   * paths are equals.
   *
   * For example:
   * ```
   * Path("/data/system/bin").relativeTo("/home")  -> `../../../home`
   * Path("/data").relativeTo("/data/system/bin")  -> `system/bin`
   * Path("/data").relativeTo("/data")             -> `.`
   * Path("C:/Windows").relativeTo("/home")        -> `/home`
   * ```
   *
   * @param target the target path to be reached by this path.
   */
  infix fun relativeTo(target: CharSequence): Path

  /**
   * Returns a relative path from this path to the given [target] path, or an empty path if both
   * paths are equals.
   *
   * For example:
   * ```
   * Path("/data/system/bin").relativeTo("/home")  -> `../../../home`
   * Path("/data").relativeTo("/data/system/bin")  -> `system/bin`
   * Path("/data").relativeTo("/data")             -> `.`
   * Path("C:/Windows").relativeTo("/home")        -> `/home`
   * ```
   *
   * @param target the target path to be reached by this path.
   */
  infix fun relativeTo(target: Path): Path

  /**
   * Returns a relative path string from this path to the given [target] path, or an empty path if
   * both paths are equals.
   *
   * For example:
   * ```
   * Path("/data/system/bin").relativeTo("/home")  -> `../../../home`
   * Path("/data").relativeTo("/data/system/bin")  -> `system/bin`
   * Path("/data").relativeTo("/data")             -> `.`
   * Path("C:/Windows").relativeTo("/home")        -> `/home`
   * ```
   *
   * @param target the target path to be reached by this path.
   */
  infix fun relativeStrTo(target: CharSequence): String = relativeTo(target).toString()

  /**
   * Returns a relative path string from this path to the given [target] path, or an empty path if
   * both paths are equals.
   *
   * For example:
   * ```
   * Path("/data/system/bin").relativeTo("/home")  -> `../../../home`
   * Path("/data").relativeTo("/data/system/bin")  -> `system/bin`
   * Path("/data").relativeTo("/data")             -> `.`
   * Path("C:/Windows").relativeTo("/home")        -> `/home`
   * ```
   *
   * @param target the target path to be reached by this path.
   */
  infix fun relativeStrTo(target: Path): String = relativeTo(target).toString()

  /**
   * Creates all non-existent parent directories of this file, including any necessary but
   * nonexistent parent directories. If some path on the parent directories paths already exists
   * and is a file, throw an [PathExistsAndIsNotDirectoryException], otherwise skip them.
   *
   * For example, the path is: `foo/bar/baz.file`, the function will be created directories:
   * 'foo' and 'bar'.
   *
   * @return this file
   */
  @Throws(PathExistsAndIsNotDirectoryException::class)
  fun createParentDirectories(): Self

  /**
   * Returns the real path of an existing file.
   *
   * @param followLinks if this is a symbolic link, whether to handle the final target of link.
   *
   * @return this path
   */
  fun toReal(followLinks: Boolean = true): Self

  /**
   * Returns the list of this path split.
   *
   * The first element of the list is the name of this parent path that this path can touch last,
   * and the last element of the list is the name of this path, and the list will be normalized,
   * e.g, if the path is
   *   `foo/bar/gav/../file.txt`,
   * the list will be
   *   `[foo, bar, file.txt]`.
   *
   * For more example:
   * ```
   * Path("C:/foo/bar").split() == `[C:, foo, bar]`
   *
   * // If user home path is `/home/kitty`
   * Path("~/foo/bar").split() == `[home, kitty, foo, bar]`
   *
   * // If current work path is `home/working/foo/bar`
   * Path("../../baz").split() == `[home, working, baz]`
   * ```
   */
  fun split(): List<String>

  /**
   * Returns `true` if this path starts with the given [path].
   */
  fun startsWith(path: Path): Boolean

  /**
   * Returns `true` if this path ends with the given [path].
   */
  fun endsWith(path: Path): Boolean

  /**
   * Creates a "hard link" for the [target] path (the [target] must exists) so that it can be
   * accessed using this path.
   *
   * @return the [target] path to the link
   *
   * @throws UnsupportedOperationException Only allow to create link for file, if the target is a
   *   directory, it cannot be created.
   * @throws LinkAlreadyExistsException if the [target] path already linked, it cannot be created.
   */
  fun <R : Path> linkTo(target: R): R

  /**
   * Creates a symbolic link to the [target] path.
   *
   * @return the [target] path to the symbolic link
   *
   * @throws PathAlreadyExistsException if the target already exists, it cannot be overwritten
   *   with a symbolic link.
   *
   * @see symbolicLink
   */
  fun <R : Path> linkSymbolTo(target: R): R

  /**
   * Returns true if the object of this path is exactly equal to the given [other] object.
   */
  fun isSameAs(other: Path?): Boolean

  /**
   * Returns true if this path is equals to the path of given [other] object.
   *
   * @see isSameAs
   */
  override fun equals(other: Any?): Boolean

  /**
   * Compares two abstract paths lexicographically. This function does not access the file system
   * and neither file is required exists.
   *
   * By default, it is processed by the expression `path.toString().compareTo(other.toString())`.
   */
  override fun compareTo(other: Self): Int

  /**
   * Compares two abstract paths lexicographically. This function does not access the file system
   * and neither file is required exists.
   *
   * By default, it is processed by the expression `path.toString().compareTo(otherPath)`.
   */
  fun compareTo(otherPath: String): Int

  /**
   * Computes a hash code for this path.
   *
   * By default, it is processed by the expression `path.toString().hashCode()`.
   * ```
   */
  override fun hashCode(): Int

  /**
   * Returns the string representation of this path.
   */
  override fun toString(): String
}

/**
 * An object representing the any type of path.
 *
 * @see File
 * @see Directory
 * @see Zip
 * @see ZipEntry
 * @see ZipFileEntry
 * @see ZipDirectoryEntry
 */
typealias Path = IPath<*>

/**
 * Returns the path based on the path char sequence.
 *
 * @param first the path char sequence or initial part of the path
 * @param more additional char sequence to be joined to form the path
 */
expect fun Path(first: CharSequence, vararg more: CharSequence): Path

/**
 * Convert [CharSequence] to [Path].
 *
 * @param more additional char sequence to be joined to form the path
 */
inline fun CharSequence.asPath(vararg more: CharSequence): Path = Path(this, *more)

/**
 * Returns an empty root path.
 */
val EmptyRootPath = Path(SystemSeparator)

/**
 * Returns an empty relative path.
 */
val EmptyRelativePath = Path("")

/**
 * Returns a relative path represents to the current work.
 */
val CurrentRelativePath = Path(".")