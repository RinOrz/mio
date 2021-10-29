package com.meowool.mio

import kotlinx.coroutines.flow.Flow

/**
 * An object that represents a path group. This object represents the group holds path's children
 * of any type. For example, directory, archive, etc., these are regarded as one group.
 *
 * @see Directory
 * @see Zip
 *
 * @author 凛 (https://github.com/RinOrz)
 */
typealias PathGroup = IPathGroup<*, *, *, *>

/**
 * An object that represents a path group. This object represents the group holds path's children
 * of type [SubPath]. For example, directory, archive, etc., these are regarded as one group.
 */
interface IPathGroup<
  Self : IPathGroup<Self, *, *, *>,
  SubPath : Path,
  SubFile : File,
  SubDirectory : Directory,
  > : IPath<Self> {

  /**
   * Returns `true` if there are no children in this group.
   */
  fun isEmpty(): Boolean = list(depth = 1).isEmpty()

  /**
   * Returns `true` if there are children in this group.
   */
  fun isNotEmpty(): Boolean = isEmpty().not()

  /**
   * Returns a flow that lazily emits children in this group.
   *
   * @param depth the maximum number of directory levels to traverse. when the value
   *   is [Int.MAX_VALUE], recursively emits all children and their children, when the value is `1`,
   *   only the sub-files or sub-directories in direct contact with this group are emitted.
   *
   * @see flowRecursively
   */
  fun flow(depth: Int = 1): Flow<SubPath>

  /**
   * Returns a flow that lazily emits children in this group recursively.
   *
   * @see flow
   */
  fun flowRecursively(): Flow<SubPath> = flow(depth = Int.MAX_VALUE)

  /**
   * Returns a list that directly adds children in this group.
   *
   * @param depth the maximum number of directory levels to traverse. when the value
   *   is [Int.MAX_VALUE], recursively adds all children and their children, when the value is `1`,
   *   only the sub-files or sub-directories in direct contact with this group are added.
   *
   * @see listRecursively
   */
  fun list(depth: Int = 1): List<SubPath>

  /**
   * Returns a list that directly adds children in this group recursively.
   *
   * @see list
   */
  fun listRecursively(): List<SubPath> = list(depth = Int.MAX_VALUE)

  /**
   * Start walking this group and its children.
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
   * If the group is large, the walks can be time-consuming, it is recommended to call it in a
   * background thread.
   *
   * @return the list of each path walked
   *
   * @param depth the maximum number of directory levels to visiting. when the value
   *   is [Int.MAX_VALUE], recursively visit all subdirectories, when the value is `1`, only
   *   the children in direct contact with this group are visited.
   * @param walkDirs if the value is `true`, all sub-directories in this group will be walked,
   *   if `false`, directories themselves will not be walked
   *   (whether the files in them are walked depends on [walkFiles]).
   * @param walkFiles if the value is `true`, all sub-files in this group will be walked,
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
  fun walk(
    depth: Int = Int.MAX_VALUE,
    walkDirs: Boolean = true,
    walkFiles: Boolean = true,
    followLinks: Boolean = false,
    filterDirs: (SubDirectory) -> Boolean = { true },
    filterFiles: (SubFile) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = { _, throwable -> throw throwable },
    onEnterDirectory: (SubDirectory) -> Unit = {},
    onLeaveDirectory: (SubDirectory) -> Unit = {},
    onVisitFile: (SubFile) -> Unit = {},
  ): List<SubPath>

  /**
   * Creates sub-file in this group according to the given [subpath].
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
   * @param subpath the path of the sub-file to be created in this group.
   * @param overwrite if the value is `true`, when a file with the same path already exists in this
   *   group, it will be overwritten with a new file, otherwise nothing will happen.
   *
   * @see SubFile
   * @see IFile.create for more details
   */
  fun addFile(subpath: String, overwrite: Boolean = false): SubFile

  /**
   * Creates sub-directory in this group according to the given [subpath].
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
   * @param subpath the path of the sub-directory to be created in this group.
   * @param overwrite if the value is `true`, when a directory with the same path already exists in
   *   this group, it will be overwritten with an empty directory, otherwise nothing will happen.
   *
   * @see SubDirectory
   * @see IDirectory.create for more details
   */
  fun addDirectory(subpath: String, overwrite: Boolean = false): SubDirectory

  /**
   * Creates sub-directory in this group according to the given [subpath].
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
   * @param subpath the path of the sub-directory to be created in this group.
   * @param overwrite if the value is `true`, when a directory with the same path already exists in
   *   this group, it will be overwritten with an empty directory, otherwise nothing will happen.
   *
   * @see SubDirectory
   * @see IDirectory.create for more details
   */
  fun addDir(subpath: String, overwrite: Boolean = false): SubDirectory =
    addDirectory(subpath, overwrite)

  /**
   * Adds the given [subdirectory] to this group.
   *
   * @return the directory that has been added
   *
   * @param subdirectory the sub-directory to be added to this group.
   * @param recursively adding [subdirectory] and all its children to destination.
   * @param overwrite whether to overwrite [subdirectory] if it already exists in this group.
   * @param keepSources whether to add [subdirectory] to this group by [IDirectory.copyInto],
   *   otherwise the [subdirectory] will be moved by [IDirectory.moveInto].
   * @param followLinks if the [subdirectory] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   * @param filter optionally filter the contents of certain paths when adding [subdirectory].
   * @param onError what should be done when an error occurs when adding [subdirectory].
   *
   * @see IDirectory.copyTo for more details.
   * @see IDirectory.moveTo for more details.
   */
  fun add(
    subdirectory: SubDirectory,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = { _, exception -> throw exception },
  ): SubDirectory = subdirectory.also {
    when {
      keepSources -> it.copyInto(target = this, overwrite, followLinks)
      else -> it.moveInto(target = this, overwrite, followLinks)
    }
  }

  /**
   * Adds the given [subfile] to this group.
   *
   * @return the file that has been added
   *
   * @param subfile the sub-file to be added to this group.
   * @param overwrite whether to overwrite [subfile] if it already exists in this group.
   * @param keepSources whether to add [subfile] to this group by [IFile.copyInto], otherwise the
   *   [subfile] will be moved by [IFile.moveInto].
   * @param followLinks if the [subfile] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   *
   * @see IFile.copyTo for more details.
   * @see IFile.moveTo for more details.
   */
  fun add(
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
   * Adds all the given [subdirectories] to this group.
   *
   * @return the list of directories that has been added
   *
   * @param subdirectories the sub-directories to be added to this group.
   * @param recursively adding [subdirectories] and all its children to destination.
   * @param overwrite whether to overwrite [subdirectories] if it already exists in this group.
   * @param keepSources whether to add [subdirectories] to this group by [IDirectory.copyInto],
   *   otherwise the [subdirectories] will be moved by [IDirectory.moveInto].
   * @param followLinks if the [subdirectories] to be added is a symbolic link and the value is
   *   `true`, then add the link target, otherwise add the symbolic link itself.
   * @param filter optionally filter the contents of certain paths when adding [subdirectories].
   * @param onError what should be done when an error occurs when adding [subdirectories].
   *
   * @see IDirectory.copyTo for more details.
   * @see IDirectory.moveTo for more details.
   */
  fun addAll(
    vararg subdirectories: SubDirectory,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = { _, exception -> throw exception },
  ): List<SubDirectory> = subdirectories.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subdirectories] to this group.
   *
   * @return the list of directories that has been added
   *
   * @param subdirectories the sub-directories to be added to this group.
   * @param recursively adding [subdirectories] and all its children to destination.
   * @param overwrite whether to overwrite [subdirectories] if it already exists in this group.
   * @param keepSources whether to add [subdirectories] to this group by [IDirectory.copyInto],
   *   otherwise the [subdirectories] will be moved by [IDirectory.moveInto].
   * @param followLinks if the [subdirectories] to be added is a symbolic link and the value is
   *   `true`, then add the link target, otherwise add the symbolic link itself.
   * @param filter optionally filter the contents of certain paths when adding [subdirectories].
   * @param onError what should be done when an error occurs when adding [subdirectories].
   *
   * @see IDirectory.copyTo for more details.
   * @see IDirectory.moveTo for more details.
   */
  fun addAll(
    subdirectories: Iterable<SubDirectory>,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = { _, exception -> throw exception },
  ): List<SubDirectory> = subdirectories.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subdirectories] to this group.
   *
   * @return the list of directories that has been added
   *
   * @param subdirectories the sub-directories to be added to this group.
   * @param recursively adding [subdirectories] and all its children to destination.
   * @param overwrite whether to overwrite [subdirectories] if it already exists in this group.
   * @param keepSources whether to add [subdirectories] to this group by [IDirectory.copyInto],
   *   otherwise the [subdirectories] will be moved by [IDirectory.moveInto].
   * @param followLinks if the [subdirectories] to be added is a symbolic link and the value is
   *   `true`, then add the link target, otherwise add the symbolic link itself.
   * @param filter optionally filter the contents of certain paths when adding [subdirectories].
   * @param onError what should be done when an error occurs when adding [subdirectories].
   *
   * @see IDirectory.copyTo for more details.
   * @see IDirectory.moveTo for more details.
   */
  fun addAll(
    subdirectories: Sequence<SubDirectory>,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> PathHandlingErrorSolution = { _, exception -> throw exception },
  ): Sequence<SubDirectory> = subdirectories.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subfiles] to this group.
   *
   * @return the list of files that has been added
   *
   * @param subfiles the sub-file to be added to this group.
   * @param overwrite whether to overwrite [subfiles] if it already exists in this group.
   * @param keepSources whether to add [subfiles] to this group by [IFile.copyInto], otherwise the
   *   [subfiles] will be moved by [IFile.moveInto].
   * @param followLinks if the [subfiles] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   *
   * @see IFile.copyTo for more details.
   * @see IFile.moveTo for more details.
   */
  fun addAll(
    vararg subfiles: SubFile,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
  ): List<SubFile> = subfiles.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subfiles] to this group.
   *
   * @return the list of files that has been added
   *
   * @param subfiles the sub-file to be added to this group.
   * @param overwrite whether to overwrite [subfiles] if it already exists in this group.
   * @param keepSources whether to add [subfiles] to this group by [IFile.copyInto], otherwise the
   *   [subfiles] will be moved by [IFile.moveInto].
   * @param followLinks if the [subfiles] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   *
   * @see IFile.copyTo for more details.
   * @see IFile.moveTo for more details.
   */
  fun addAll(
    subfiles: Iterable<SubFile>,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
  ): List<SubFile> = subfiles.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Adds all the given [subfiles] to this group.
   *
   * @return the list of files that has been added
   *
   * @param subfiles the sub-file to be added to this group.
   * @param overwrite whether to overwrite [subfiles] if it already exists in this group.
   * @param keepSources whether to add [subfiles] to this group by [IFile.copyInto], otherwise the
   *   [subfiles] will be moved by [IFile.moveInto].
   * @param followLinks if the [subfiles] to be added is a symbolic link and the value is `true`,
   *   then add the link target, otherwise add the symbolic link itself.
   *
   * @see IFile.copyTo for more details.
   * @see IFile.moveTo for more details.
   */
  fun addAll(
    subfiles: Sequence<SubFile>,
    overwrite: Boolean = false,
    keepSources: Boolean = true,
    followLinks: Boolean = true,
  ): Sequence<SubFile> = subfiles.map { add(it, overwrite, keepSources, followLinks) }

  /**
   * Returns true if there is a children with the same path as the given [subpath] in this group.
   *
   * @see join
   */
  operator fun contains(subpath: CharSequence): Boolean = this.join(subpath).exists()

  /**
   * Returns true if there is a children with the same path as the given [subpath] in this group.
   *
   * @see join
   */
  operator fun contains(subpath: Path): Boolean = this.join(subpath).exists()

  /**
   * Returns the children that matches the given [name] in this group.
   *
   * If multiple results are found, return the first one, and throw an [NoSuchElementException] if
   * not found.
   *
   * @param recursively whether to recursively find all children.
   * @see find
   */
  operator fun get(name: String, recursively: Boolean = false): SubPath = when(recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.first { it.name == name}

  /**
   * Returns the list of children that all matches the given [name] in this group.
   *
   * Throw an [NoSuchElementException] if not found.
   *
   * @param recursively whether to recursively find all children.
   * @see findAll
   */
  fun getAll(name: String, recursively: Boolean = false): List<SubPath> = when(recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.filter { it.name == name}

  /**
   * Finds the children that matches the given [name] in this group.
   *
   * If multiple results are found, return the first one, and throw an [NoSuchElementException] if
   * not found.
   *
   * @param recursively whether to recursively find all children.
   *
   * @return if there is a children in this group with the given name, returns its path, otherwise
   *   return `null`.
   *
   * @see get
   */
  fun find(name: String, recursively: Boolean = false): SubPath? = when(recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.find { it.name == name}

  /**
   * Finds the children that matches the given [name] in this group.
   *
   * @param recursively whether to recursively find all children.
   *
   * @return if there is a children in this group with the given name, returns its path, otherwise
   *   return empty list.
   *
   * @see get
   */
  fun findAll(name: String, recursively: Boolean = false): List<SubPath> = when(recursively) {
    false -> list(depth = 1)
    true -> list(depth = Int.MAX_VALUE)
  }.filter { it.name == name}

  /**
   * Copies this group into the given [target] group.
   *
   * Note that the contents of this group will be included when moving. If only want to copy the
   * group itself (i.e. create an empty group at the [target]), please set the argument
   * [recursively] to `false`.
   *
   * @param recursively copies it and all its children to destination.
   * @param overwrite whether to overwrite when the target file already exists, otherwise, they
   *   will be skipped when the group or its children to be copied already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then copies the link
   *   final target, otherwise copies the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this group or its children.
   *
   * @return the group of target path that has been copied
   */
  fun copyTo(
    target: PathGroup,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = { _, throwable -> throw throwable },
  ): PathGroup

  /**
   * Copies this group into the given [target] group.
   *
   * Note that the contents of this group will be included when moving. If only want to copy the
   * group itself (i.e. create an empty group at the [target]), please set the argument
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
   *   will be skipped when the group or its children to be copied already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then copies the link
   *   final target, otherwise copies the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this group or its children.
   *
   * @return the group of target path that has been copied
   *
   * @see IPathGroup.copyTo
   */
  fun copyInto(
    target: PathGroup,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = { _, throwable -> throw throwable },
  ): PathGroup

  /**
   * Moves this group to the given [target] group.
   *
   * Note that the contents of this group will be included when moving. If only want to move the
   * group itself (i.e. create an empty group at the [target]), please set the argument
   * [recursively] to `false`.
   *
   * @param recursively moves it and all its children to destination.
   * @param overwrite whether to overwrite when the target file already exists, otherwise, they
   *   will be skipped when the group or its children to be moved already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then moves the link
   *   final target, otherwise moves the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this group or its children.
   *
   * @return the group of target path that has been moved
   */
  fun moveTo(
    target: PathGroup,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = { _, throwable -> throw throwable },
  ): PathGroup

  /**
   * Moves this group into the given [target] group.
   *
   * Note that the contents of this group will be included when moving. If only want to move the
   * group itself (i.e. create an empty group at the [target]), please set the argument
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
   *   will be skipped when the group or its children to be moved already exist.
   * @param followLinks if encounter a symbolic link and the value is `true`, then moves the link
   *   final target, otherwise moves the symbolic link itself.
   * @param filter if the argument [recursively] is `true`, can filter to exclude some children
   *   from moving.
   * @param onError what should be done when an error occurs when moving this group or its children.
   *
   * @return the group of target path that has been moved
   *
   * @see IPathGroup.moveTo
   */
  fun moveInto(
    target: PathGroup,
    recursively: Boolean = true,
    overwrite: Boolean = false,
    followLinks: Boolean = true,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> CopyErrorSolution = { _, throwable -> throw throwable },
  ): PathGroup

  /**
   * Deletes this group safely. Skip if this group does not exist. If there are any symbolic link,
   * delete the symbolic link itself not the final target of the link, if you want to change this
   * behavior, set [followLinks] to `true`, this will remove the target of the link.
   *
   * If this group is not empty (contains children), the deletion fails (returns `false`).
   * If you want to delete the group and all its children, set [recursively] to `true` or
   * call [deleteRecursively].
   *
   * @param recursively deletes this group and all its children.
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter if the [recursively] is set to `true`, can filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this group.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see deleteStrictly
   */
  fun delete(
    recursively: Boolean = false,
    followLinks: Boolean = false,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = { _, throwable -> throw throwable },
  ): Boolean

  /**
   * Deletes this group and all its children safely. Skip if this group does not exist. If there
   * are any symbolic link, delete the symbolic link itself not the final target of the link, if
   * you want to change this behavior, set [followLinks] to `true`, this will remove the target of
   * the link.
   *
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this group.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see deleteStrictlyRecursively
   */
  fun deleteRecursively(
    followLinks: Boolean = false,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = { _, throwable -> throw throwable },
  ): Boolean = delete(recursively = true, followLinks, filter, onError)

  /**
   * Deletes this group strictly. Throws an [NoSuchPathException] if this group does not exist.
   * If there are any symbolic link, delete the symbolic link itself not the final target of the
   * link, if you want to change this behavior, set [followLinks] to `true`, this will remove the
   * target of the link.
   *
   * If this group is not empty (contains children), throws an [GroupNotEmptyException] to deletion
   * fails. If you want to delete the group and all its children, set [recursively] to `true`
   * or call [deleteStrictlyRecursively].
   *
   * @param recursively deletes it and all its children.
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter if the [recursively] is set to `true`, can filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this group.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see delete
   */
  @Throws(GroupNotEmptyException::class, NoSuchPathException::class)
  fun deleteStrictly(
    recursively: Boolean = false,
    followLinks: Boolean = false,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = { _, throwable -> throw throwable },
  ): Boolean

  /**
   * Deletes this group all its children strictly. Throws an [NoSuchPathException] if this group
   * does not exist. If there are any symbolic link, delete the symbolic link itself not the final
   * target of the link, if you want to change this behavior, set [followLinks] to `true`, this will
   * remove the target of the link.
   *
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param filter filter children from deleting.
   * @param onError what should be done when an error occurs when deleting this group.
   *
   * @return if the deletion fails, it returns `false`.
   *
   * @see delete
   */
  @Throws(DirectoryNotEmptyException::class, NoSuchPathException::class)
  fun deleteStrictlyRecursively(
    followLinks: Boolean = false,
    filter: (SubPath) -> Boolean = { true },
    onError: (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = { _, throwable -> throw throwable },
  ): Boolean = deleteStrictly(recursively = true, followLinks, filter, onError)

  /**
   * Delete all children in this group, but not the group itself.
   *
   * @return whether to clear the group successfully
   *
   * @param recursively if the value is `true`, deletes all children recursively, otherwise only
   *   deletes the directly touched children in this group.
   * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
   *   link final target, otherwise delete the symbolic link itself.
   * @param onError what to do when an error occurs when clearing.
   */
  fun clear(
    recursively: Boolean = true,
    followLinks: Boolean = false,
    onError: (path: SubPath, throwable: Throwable) -> DeleteErrorSolution = { _, throwable -> throw throwable },
  ): Boolean = delete(recursively, followLinks, filter = { it != this }, onError)
}