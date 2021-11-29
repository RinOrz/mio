package com.meowool.io

import kotlinx.coroutines.flow.Flow
import com.meowool.io.PathHandlingErrorSolution.Companion.PathListDefault as DefaultPathListHandlingErrorSolution
import com.meowool.io.PathHandlingErrorSolution.Companion.PathListSuspendDefault as DefaultPathListSuspendHandlingErrorSolution

/**
 * An object that represents a path list. This object represents the list holds path's children
 * of type [SubPath]. For example, directory, archive, etc., these are regarded as one path list.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
interface BasePathList<
  SubPath : Path,
  SubFile : File,
  SubDirectory : Directory,
  Actual : BasePathList<SubPath, SubFile, SubDirectory, Actual>> : BasePath<Actual> {

  /**
   * Returns `true` if there are no children in this path list.
   */
  fun isEmpty(): Boolean = list(depth = 1).isEmpty()

  /**
   * Returns `true` if there are children in this path list.
   */
  fun isNotEmpty(): Boolean = isEmpty().not()

  /**
   * Returns a flow that lazily emits children in this path list.
   *
   * @param depth the maximum number of directory levels to traverse. when the value
   *   is [Int.MAX_VALUE], recursively emits all children and their children, when the value is `1`,
   *   only the sub-files or sub-directories in direct contact with this path list are emitted.
   *
   * @see flowRecursively
   */
  fun flow(depth: Int = 1): Flow<SubPath>

  /**
   * Returns a flow that lazily emits children in this path list recursively.
   *
   * @see flow
   */
  fun flowRecursively(): Flow<SubPath> = flow(depth = Int.MAX_VALUE)

  /**
   * Returns a list that directly adds children in this path list.
   *
   * @param depth the maximum number of directory levels to traverse. when the value
   *   is [Int.MAX_VALUE], recursively adds all children and their children, when the value is `1`,
   *   only the sub-files or sub-directories in direct contact with this path list are added.
   *
   * @see listRecursively
   */
  fun list(depth: Int = 1): List<SubPath>

  /**
   * Returns a list that directly adds children in this path list recursively.
   *
   * @see list
   */
  fun listRecursively(): List<SubPath> = list(depth = Int.MAX_VALUE)

  /**
   * Start walking this path list and its children.
   *
   * When walking to the directory, call the [filterDirs], if the filter returns `true`, call the
   * [onEnterDirectory] callback then continue to walk its content, otherwise skip it's and its
   * children, when walking to the file and the [filterFiles] returns `true`, call the
   * [onVisitFile] callback, until all the contents of the directory have been walked, call the
   * [onLeaveDirectory] callback to exit the directory, and so on.
   *
   * For example:
   * ```
   * 1. Suppose a directory:
   *
   *   - Dir
   *     - SubDir1
   *       - SubFile1
   *       - SubFile2
   *     - SubDir2
   *       - SubFile3
   *     - File
   *
   *
   * 2. Call:
   *
   *   Dir.walk(
   *     filterDirs = {
   *       // When the directory name is `SubDir1`, return `false`
   *       it.name != "SubDir1"
   *     },
   *     onEnterDirectory = {
   *       println("enter: ${it.name}")
   *     },
   *     onLeaveDirectory = {
   *       println("leave: ${it.name}")
   *     },
   *     onVisitFile = {
   *       println("visit: ${it.name}")
   *     }
   *   )
   *
   *
   * 3. Output result
   *
   *   enter: Dir
   *   enter: SubDir1
   *   leave: SubDir1
   *   enter: SubDir2
   *   visit: SubFile3
   *   leave: SubDir2
   *   visit: File
   *   leave: Dir
   * ```
   *
   * If the path list is large, the walks can be time-consuming, it is recommended to call it in a
   * background thread.
   *
   * @return the list of each path walked
   *
   * @param depth the maximum number of directory levels to visiting. when the value
   *   is [Int.MAX_VALUE], recursively visit all subdirectories, when the value is `1`, only
   *   the children in direct contact with this path list are visited.
   * @param walkDirs if the value is `true`, all sub-directories in this path list will be walked,
   *   if `false`, directories themselves will not be walked
   *   (whether the files in them are walked depends on [walkFiles]).
   * @param walkFiles if the value is `true`, all sub-files in this path list will be walked,
   *   if `false`, Skip them when walking.
   * @param followLinks when walking to a symbolic link path, whether to walk to the final target
   *   of the link instead of the symbolic link itself.
   * @param filterDirs the callback will be called before each directory is walk, if the returns
   *   value is `true`, then continue walking, otherwise skip it's and its children.
   * @param filterFiles the callback will be called before each file is walk, if the returns value
   *   is `true`, then walking the file, otherwise skip its.
   * @param onError the solution after an error occurs when walking to a certain file or directory.
   * @param onEnterDirectory the callback that will be called when entering each directory.
   * @param onLeaveDirectory the callback called when the children of a certain directory are
   *   all walked.
   * @param onVisitFile the callback called when the visiting file.
   *
   * @see SubFile
   * @see SubDirectory
   */
  suspend fun walk(
    depth: Int = Int.MAX_VALUE,
    walkDirs: Boolean = true,
    walkFiles: Boolean = true,
    followLinks: Boolean = false,
    filterDirs: suspend (SubDirectory) -> Boolean = { true },
    filterFiles: suspend (SubFile) -> Boolean = { true },
    onError: suspend (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = DefaultPathListSuspendHandlingErrorSolution,
    onEnterDirectory: suspend (SubDirectory) -> Unit = {},
    onLeaveDirectory: suspend (SubDirectory) -> Unit = {},
    onVisitFile: suspend (SubFile) -> Unit = {},
  ): List<SubPath>

  /**
   * Start walking this path list and its children.
   * This may block the thread, please consider using [walk].
   *
   * When walking to the directory, call the [filterDirs], if the filter returns `true`, call the
   * [onEnterDirectory] callback then continue to walk its content, otherwise skip it's and its
   * children, when walking to the file and the [filterFiles] returns `true`, call the
   * [onVisitFile] callback, until all the contents of the directory have been walked, call the
   * [onLeaveDirectory] callback to exit the directory, and so on.
   *
   * For example:
   * ```
   * 1. Suppose a directory:
   *
   *   - Dir
   *     - SubDir1
   *       - SubFile1
   *       - SubFile2
   *     - SubDir2
   *       - SubFile3
   *     - File
   *
   *
   * 2. Call:
   *
   *   Dir.walk(
   *     filterDirs = {
   *       // When the directory name is `SubDir1`, return `false`
   *       it.name != "SubDir1"
   *     },
   *     onEnterDirectory = {
   *       println("enter: ${it.name}")
   *     },
   *     onLeaveDirectory = {
   *       println("leave: ${it.name}")
   *     },
   *     onVisitFile = {
   *       println("visit: ${it.name}")
   *     }
   *   )
   *
   *
   * 3. Output result
   *
   *   enter: Dir
   *   enter: SubDir1
   *   leave: SubDir1
   *   enter: SubDir2
   *   visit: SubFile3
   *   leave: SubDir2
   *   visit: File
   *   leave: Dir
   * ```
   *
   * If the path list is large, the walks can be time-consuming, it is recommended to call it in a
   * background thread.
   *
   * @return the list of each path walked
   *
   * @param depth the maximum number of directory levels to visiting. when the value
   *   is [Int.MAX_VALUE], recursively visit all subdirectories, when the value is `1`, only
   *   the children in direct contact with this path list are visited.
   * @param walkDirs if the value is `true`, all sub-directories in this path list will be walked,
   *   if `false`, directories themselves will not be walked
   *   (whether the files in them are walked depends on [walkFiles]).
   * @param walkFiles if the value is `true`, all sub-files in this path list will be walked,
   *   if `false`, Skip them when walking.
   * @param followLinks when walking to a symbolic link path, whether to walk to the final target
   *   of the link instead of the symbolic link itself.
   * @param filterDirs the callback will be called before each directory is walk, if the returns
   *   value is `true`, then continue walking, otherwise skip it's and its children.
   * @param filterFiles the callback will be called before each file is walk, if the returns value
   *   is `true`, then walking the file, otherwise skip its.
   * @param onError the solution after an error occurs when walking to a certain file or directory.
   * @param onEnterDirectory the callback that will be called when entering each directory.
   * @param onLeaveDirectory the callback called when the children of a certain directory are
   *   all walked.
   * @param onVisitFile the callback called when the visiting file.
   *
   * @see SubFile
   * @see SubDirectory
   */
  fun walkBlocking(
    depth: Int = Int.MAX_VALUE,
    walkDirs: Boolean = true,
    walkFiles: Boolean = true,
    followLinks: Boolean = false,
    filterDirs: (SubDirectory) -> Boolean = { true },
    filterFiles: (SubFile) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = DefaultPathListHandlingErrorSolution,
    onEnterDirectory: (SubDirectory) -> Unit = {},
    onLeaveDirectory: (SubDirectory) -> Unit = {},
    onVisitFile: (SubFile) -> Unit = {},
  ): List<SubPath>

  /**
   * Creates sub-file in this path list according to the given [subpath].
   *
   * For example:
   * ```
   * 1. Suppose a directory:
   *
   *   - Group
   *     - Test.txt
   *
   * 2. Call:
   *
   *   Group.addFile("sub/file.txt")
   *
   * 3. Result:
   *
   *   - Group
   *     - sub
   *       - file.txt
   *     - Test.txt
   * ```
   *
   * @return the sub-file that has been added
   *
   * @param subpath the path of the sub-file to be created in this path list.
   * @param overwrite if the value is `true`, when a file with the same path already exists in this
   *   path list, it will be overwritten with a new file, otherwise nothing will happen.
   *
   * @see SubFile
   * @see BaseFile.create for more details
   */
  suspend fun addFile(subpath: String, overwrite: Boolean = false): SubFile

  /**
   * Creates sub-directory in this path list according to the given [subpath].
   *
   * For example:
   * ```
   * 1. Suppose a directory:
   *
   *   - Group
   *     - Test.txt
   *
   * 2. Call:
   *
   *   Group.addDirectory("sub/nestedDir")
   *
   * 3. Result:
   *
   *   - Group
   *     - sub
   *       - nestedDir
   *     - Test.txt
   * ```
   *
   * @return the sub-directory that has been added
   *
   * @param subpath the path of the sub-directory to be created in this path list.
   * @param overwrite if the value is `true`, when a directory with the same path already exists
   *   in this path list, it will be overwritten with an empty directory, otherwise nothing
   *   will happen.
   *
   * @see SubDirectory
   * @see BaseDirectory.create for more details
   */
  suspend fun addDirectory(subpath: String, overwrite: Boolean = false): SubDirectory

  /**
   * Creates sub-directory in this path list according to the given [subpath].
   *
   * For example:
   * ```
   * 1. Suppose a directory:
   *
   *   - Group
   *     - Test.txt
   *
   * 2. Call:
   *
   *   Group.addDirectory("sub/nestedDir")
   *
   * 3. Result:
   *
   *   - Group
   *     - sub
   *       - nestedDir
   *     - Test.txt
   * ```
   *
   * @return the sub-directory that has been added
   *
   * @param subpath the path of the sub-directory to be created in this path list.
   * @param overwrite if the value is `true`, when a directory with the same path already exists
   *   in this path list, it will be overwritten with an empty directory, otherwise nothing
   *   will happen.
   *
   * @see SubDirectory
   * @see BaseDirectory.create for more details
   */
  suspend fun addDir(subpath: String, overwrite: Boolean = false): SubDirectory =
    addDirectory(subpath, overwrite)

  /**
   * Adds the given [subdirectory] to this path list.
   *
   * @return the directory that has been added
   *
   * @param subdirectory the sub-directory to be added to this path list.
   * @param recursively adding [subdirectory] and all its children to destination.
   * @param overwrite whether to overwrite [subdirectory] if it already exists in this path list.
   * @param keepSources whether to add [subdirectory] to this path list by [BaseDirectory.copyInto],
   *   otherwise the [subdirectory] will be moved by [BaseDirectory.moveInto].
   * @param followLinks if the [subdirectory] to be added is a symbolic link and the value is
   *   `true`, then add the link target, otherwise add the symbolic link itself.
   * @param filter optionally filter the contents of certain paths when adding [subdirectory].
   * @param onError what should be done when an error occurs when adding [subdirectory].
   *
   * @see BaseDirectory.copyTo for more details.
   * @see BaseDirectory.moveTo for more details.
   */
  suspend fun add(
    subdirectory: SubDirectory,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = DefaultPathListHandlingErrorSolution,
  ): SubDirectory = subdirectory.also {
    when {
      keepSources -> it.copyInto(target = this, overwrite, followLinks)
      else -> it.moveInto(target = this, overwrite, followLinks)
    }
  }

  /**
   * Adds the given [subfile] to this path list.
   *
   * @return the file that has been added
   *
   * @param subfile the sub-file to be added to this path list.
   * @param overwrite whether to overwrite [subfile] if it already exists in this path list.
   * @param keepSources whether to add [subfile] to this path list by [BaseFile.copyInto],
   *   otherwise the [subfile] will be moved by [BaseFile.moveInto].
   * @param followLinks if the [subfile] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   *
   * @see BaseFile.copyTo for more details.
   * @see BaseFile.moveTo for more details.
   */
  suspend fun add(
    subfile: SubFile,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
  ): SubFile = subfile.also {
    when {
      keepSources -> it.copyInto(target = this, overwrite, followLinks)
      else -> it.moveInto(target = this, overwrite, followLinks)
    }
  }

  /**
   * Adds all the given [subdirectories] to this path list.
   *
   * @return the list of directories that has been added
   *
   * @param subdirectories the sub-directories to be added to this path list.
   * @param recursively adding [subdirectories] and all its children to destination.
   * @param overwrite whether to overwrite [subdirectories] if it already exists in this path list.
   * @param keepSources whether to add [subdirectories] to this path list
   *   by [BaseDirectory.copyInto], otherwise the [subdirectories] will be moved
   *   by [BaseDirectory.moveInto].
   * @param followLinks if the [subdirectories] to be added is a symbolic link and the value is
   *   `true`, then add the link target, otherwise add the symbolic link itself.
   * @param filter optionally filter the contents of certain paths when adding [subdirectories].
   * @param onError what should be done when an error occurs when adding [subdirectories].
   *
   * @see BaseDirectory.copyTo for more details.
   * @see BaseDirectory.moveTo for more details.
   */
  suspend fun addAll(
    vararg subdirectories: SubDirectory,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = DefaultPathListHandlingErrorSolution,
  ): List<SubDirectory> = subdirectories.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subdirectories] to this path list.
   *
   * @return the list of directories that has been added
   *
   * @param subdirectories the sub-directories to be added to this path list.
   * @param recursively adding [subdirectories] and all its children to destination.
   * @param overwrite whether to overwrite [subdirectories] if it already exists in this path list.
   * @param keepSources whether to add [subdirectories] to this path list
   *   by [BaseDirectory.copyInto], otherwise the [subdirectories] will be moved
   *   by [BaseDirectory.moveInto].
   * @param followLinks if the [subdirectories] to be added is a symbolic link and the value is
   *   `true`, then add the link target, otherwise add the symbolic link itself.
   * @param filter optionally filter the contents of certain paths when adding [subdirectories].
   * @param onError what should be done when an error occurs when adding [subdirectories].
   *
   * @see BaseDirectory.copyTo for more details.
   * @see BaseDirectory.moveTo for more details.
   */
  suspend fun addAll(
    subdirectories: Iterable<SubDirectory>,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = DefaultPathListHandlingErrorSolution,
  ): List<SubDirectory> = subdirectories.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subfiles] to this path list.
   *
   * @return the list of files that has been added
   *
   * @param subfiles the sub-file to be added to this path list.
   * @param overwrite whether to overwrite [subfiles] if it already exists in this path list.
   * @param keepSources whether to add [subfiles] to this path list by [BaseFile.copyInto],
   *   otherwise the [subfiles] will be moved by [BaseFile.moveInto].
   * @param followLinks if the [subfiles] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   *
   * @see BaseFile.copyTo for more details.
   * @see BaseFile.moveTo for more details.
   */
  suspend fun addAll(
    vararg subfiles: SubFile,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
  ): List<SubFile> = subfiles.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subfiles] to this path list.
   *
   * @return the list of files that has been added
   *
   * @param subfiles the sub-file to be added to this path list.
   * @param overwrite whether to overwrite [subfiles] if it already exists in this path list.
   * @param keepSources whether to add [subfiles] to this path list by [BaseFile.copyInto],
   *   otherwise the [subfiles] will be moved by [BaseFile.moveInto].
   * @param followLinks if the [subfiles] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   *
   * @see BaseFile.copyTo for more details.
   * @see BaseFile.moveTo for more details.
   */
  suspend fun addAll(
    subfiles: Iterable<SubFile>,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
  ): List<SubFile> = subfiles.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Returns true if there is a children with the same path as the given [subpath] in this
   * path list.
   *
   * @see join
   */
  operator fun contains(subpath: CharSequence): Boolean = this.join(subpath).exists()

  /**
   * Returns true if there is a children with the same path as the given [subpath] in this
   * path list.
   *
   * @see join
   */
  operator fun contains(subpath: Path): Boolean = this.join(subpath).exists()

  /**
   * Returns the children that matches the given [name] in this path list.
   *
   * If multiple results are found, return the first one, and throw an [NoSuchElementException] if
   * not found.
   *
   * @param recursively whether to recursively find all children.
   * @see find
   */
  operator fun get(name: String, recursively: Boolean = false): SubPath = when (recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.first { it.name == name }

  /**
   * Returns the list of children that all matches the given [name] in this path list.
   *
   * Throw an [NoSuchElementException] if not found.
   *
   * @param recursively whether to recursively find all children.
   * @see findAll
   */
  fun getAll(name: String, recursively: Boolean = false): List<SubPath> = when (recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.filter { it.name == name }

  /**
   * Finds the children that matches the given [name] in this path list.
   *
   * If multiple results are found, return the first one, and throw an [NoSuchElementException] if
   * not found.
   *
   * @param recursively whether to recursively find all children.
   *
   * @return if there is a children in this path list with the given name, returns its path,
   *   otherwise return `null`.
   *
   * @see get
   */
  fun find(name: String, recursively: Boolean = false): SubPath? = when (recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.find { it.name == name }

  /**
   * Finds the children that matches the given [name] in this path list.
   *
   * @param recursively whether to recursively find all children.
   *
   * @return if there is a children in this path list with the given name, returns its path,
   *   otherwise return empty list.
   *
   * @see get
   */
  fun findAll(name: String, recursively: Boolean = false): List<SubPath> = when (recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.filter { it.name == name }

  /**
   * Copies this path list into the given [target] path list.
   *
   * Note that the contents of this path list will be included when moving. If only want to copy the
   * path list itself (i.e. create an empty path list at the [target]), please set the argument
   * [recursively] to `false`.
   *
   * @param recursively copies it and all its children to destination.
   * @param overwrite whether to overwrite when the target file already exists, otherwise, they
   *   will be skipped when the path list or its children to be copied already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then copies the link
   *   final target, otherwise copies the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this path list or its
   *   children.
   *
   * @return the path list of target path that has been copied
   */
  suspend fun <T: PathList> copyTo(
    target: T,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = DefaultPathListHandlingErrorSolution,
  ): T

  /**
   * Copies this path list into the given [target] path list.
   *
   * Note that the contents of this path list will be included when moving. If only want to copy the
   * path list itself (i.e. create an empty path list at the [target]), please set the argument
   * [recursively] to `false`.
   *
   * In fact, this function is similar to the following expression:
   * ```
   * val sourceDir = Directory("/foo/bar")
   * val targetDir = Directory("/gav/baz")
   * val copied = sourceDir.copyTo(targetDir.resolve(sourceDir.name))
   * println(copied) // "/gav/baz/bar"
   * ```
   *
   * @param recursively copies it and all its children to destination.
   * @param overwrite whether to overwrite when the target file already exists, otherwise, they
   *   will be skipped when the path list or its children to be copied already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then copies the link
   *   final target, otherwise copies the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this path list or its
   *   children.
   *
   * @return the path list of target path that has been copied
   *
   * @see BasePathList.copyTo
   */
  suspend fun copyInto(
    target: PathList,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = DefaultPathListHandlingErrorSolution,
  ): Directory

  /**
   * Moves this path list to the given [target] path list.
   *
   * Note that the contents of this path list will be included when moving. If only want to move the
   * path list itself (i.e. create an empty path list at the [target]), please set the argument
   * [recursively] to `false`.
   *
   * @param recursively moves it and all its children to destination.
   * @param overwrite whether to overwrite when the target file already exists, otherwise, they
   *   will be skipped when the path list or its children to be moved already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then moves the link
   *   final target, otherwise moves the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this path list or its
   *   children.
   *
   * @return the path list of target path that has been moved
   */
  suspend fun <T: PathList> moveTo(
    target: T,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = DefaultPathListHandlingErrorSolution,
  ): T

  /**
   * Moves this path list into the given [target] path list.
   *
   * Note that the contents of this path list will be included when moving. If only want to move the
   * path list itself (i.e. create an empty path list at the [target]), please set the argument
   * [recursively] to `false`.
   *
   * In fact, this function is similar to the following expression:
   * ```
   * val sourceDir = Directory("/foo/bar")
   * val targetDir = Directory("/gav/baz")
   * val moved = sourceDir.moveTo(targetDir.resolve(sourceDir.name))
   * println(moved) // "/gav/baz/bar"
   * ```
   *
   * @param recursively moves it and all its children to destination.
   * @param overwrite whether to overwrite when the target file already exists, otherwise, they
   *   will be skipped when the path list or its children to be moved already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then moves the link
   *   final target, otherwise moves the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this path list or its
   *   children.
   *
   * @return the path list of target path that has been moved
   *
   * @see BasePathList.moveTo
   */
  suspend fun moveInto(
    target: PathList,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = DefaultPathListHandlingErrorSolution,
  ): Directory

  /**
   * Deletes this path list safely. Skip if this path list does not exist. If there are any
   * symbolic link, delete the symbolic link itself not the final target of the link, if you want
   * to change this behavior, set [followLinks] to `true`, this will remove the target of the link.
   *
   * If this path list is not empty (contains children), the deletion fails (returns `false`).
   * If you want to delete the path list and all its children, set [recursively] to `true` or
   * call [deleteRecursively].
   *
   * @param recursively deletes this path list and all its children.
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter if the [recursively] is set to `true`, can filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this path list.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see deleteStrictly
   */
  suspend fun delete(
    recursively: Boolean = false,
    followLinks: Boolean = false,
    filter: suspend (SubPath) -> Boolean = { true },
    onError: suspend (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = DefaultPathListSuspendHandlingErrorSolution,
  ): Boolean

  /**
   * Deletes this path list and all its children safely. Skip if this path list does not exist.
   * If there are any symbolic link, delete the symbolic link itself not the final target of the
   * link, if you want to change this behavior, set [followLinks] to `true`, this will remove the
   * target of the link.
   *
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this path list.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see deleteStrictlyRecursively
   */
  suspend fun deleteRecursively(
    followLinks: Boolean = false,
    filter: suspend (SubPath) -> Boolean = { true },
    onError: suspend (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = DefaultPathListSuspendHandlingErrorSolution,
  ): Boolean = delete(recursively = true, followLinks, filter, onError)

  /**
   * Deletes this path list strictly.
   * Throws an [NoSuchPathException] if this path list does not exist.
   * If there are any symbolic link, delete the symbolic link itself not the final target of the
   * link, if you want to change this behavior, set [followLinks] to `true`, this will remove the
   * target of the link.
   *
   * If this path list is not empty (contains children), throws an [PathListNotEmptyException] to
   * deletion fails. If you want to delete the path list and all its children, set [recursively] to
   * `true` or call [deleteStrictlyRecursively].
   *
   * @param recursively deletes it and all its children.
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter if the [recursively] is set to `true`, can filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this path list.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see delete
   */
  @Throws(PathListNotEmptyException::class, NoSuchPathException::class)
  suspend fun deleteStrictly(
    recursively: Boolean = false,
    followLinks: Boolean = false,
    filter: suspend (SubPath) -> Boolean = { true },
    onError: suspend (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = DefaultPathListSuspendHandlingErrorSolution,
  ): Boolean

  /**
   * Deletes this path list all its children strictly.
   * Throws an [NoSuchPathException] if this path list does not exist. If there are any symbolic
   * link, delete the symbolic link itself not the final target of the link, if you want to change
   * this behavior, set [followLinks] to `true`, this will remove the target of the link.
   *
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this path list.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see delete
   */
  @Throws(DirectoryNotEmptyException::class, NoSuchPathException::class)
  suspend fun deleteStrictlyRecursively(
    followLinks: Boolean = false,
    filter: suspend (SubPath) -> Boolean = { true },
    onError: suspend (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = DefaultPathListSuspendHandlingErrorSolution,
  ): Boolean = deleteStrictly(recursively = true, followLinks, filter, onError)

  /**
   * Delete all children in this path list, but not the group itself.
   *
   * @return whether to clear the group successfully
   *
   * @param recursively if the value is `true`, deletes all children recursively, otherwise only
   *   deletes the directly touched children in this path list.
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param onError what to do when an error occurs when clearing.
   */
  suspend fun clear(
    recursively: Boolean = true,
    followLinks: Boolean = false,
    onError: suspend (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = DefaultPathListSuspendHandlingErrorSolution,
  ): Boolean = delete(recursively, followLinks, filter = { it != this }, onError)
}

/**
 * An object that represents a path group. This object represents the group holds path's children
 * of any type. For example, directory, archive, etc., these are regarded as one group.
 *
 * @see Directory
 * @see Zip
 *
 * @author 凛 (https://github.com/RinOrz)
 */
typealias PathList = BasePathList<*, *, *, *>