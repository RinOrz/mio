package com.meowool.mio

/**
 * Add the sub file to this directory.
 *
 * @param name the sub file name to be added to this directory.
 * @param createIfNotExists if the file to be added does not exist, whether to create a empty file.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.createFile
 */
fun Path.addFile(name: String, createIfNotExists: Boolean = true) {
  requireDirectory()
  val added = resolve(name)
  if (createIfNotExists && added.notExists()) added.createFile()
}

/**
 * Add the sub files to this directory.
 *
 * @param names the sub files name to be added to this directory.
 * @param createIfNotExists if the file to be added does not exist, whether to create a empty file.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.createFile
 */
fun Path.addFiles(
  vararg names: String,
  createIfNotExists: Boolean = true
) {
  requireDirectory()
  names.forEach {
    val added = resolve(it)
    if (createIfNotExists && added.notExists()) added.createFile()
  }
}

/**
 * Add the subdirectories to this directory.
 *
 * @param name the subdirectory name to be added to this directory.
 * @param createIfNotExists when there is a subdirectory that needs to be added to this directory
 * does not exist, whether to create an empty directory to add.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.createDirectories
 */
fun Path.addDirectory(name: String, createIfNotExists: Boolean = true) {
  requireDirectory()
  val added = resolve(name)
  if (createIfNotExists && added.notExists()) added.createDirectories()
}

/**
 * Add the subdirectories to this directory.
 *
 * @param names the subdirectories name to be added to this directory.
 * @param createIfNotExists when there is a subdirectory that needs to be added to this directory
 * does not exist, whether to create an empty directory to add.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.createDirectories
 */
fun Path.addDirectories(
  vararg names: String,
  createIfNotExists: Boolean = true
) {
  requireDirectory()
  names.forEach {
    val added = resolve(it)
    if (createIfNotExists && added.notExists()) added.createDirectories()
  }
}

/**
 * Add the sub file/directory to this directory.
 *
 * @param overwrite whether to overwrite when the target already exists.
 * @param [site] the sub file/directory to be added to this directory.
 * @param keepSources whether to add [site] to this directory by [Path.copyTo] operation,
 * otherwise the [site] will be moved ([Path.moveTo]).
 * @param keepAttributes added all the attributes of this path to the target.
 * @param followLinks if the path to be added is a symbolic link and the value is `true`, then
 * add the link target, otherwise add the symbolic link itself.
 * @param onError what should be done when an error occurs when adding this path.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @throws PathAlreadyExistsException if the target already exists and [overwrite]
 * argument is set to `false`.
 * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
 * argument is set to `true`, the copy fails, the target folder must be emptied first.
 *
 * @see Path.copyTo
 * @see Path.moveTo
 */
fun Path.add(
  site: Path,
  overwrite: Boolean = false,
  keepSources: Boolean = true,
  keepAttributes: Boolean = false,
  followLinks: Boolean = true,
  onError: (Path, Throwable) -> FileHandlingErrorSolution = { _, exception -> throw exception }
) {
  requireDirectory()
  val added = relativeTo(site)
  when {
    keepSources -> site.copyTo(
      target = added,
      overwrite = overwrite,
      keepAttributes = keepAttributes,
      followLinks = followLinks,
      onError = onError
    )
    else -> site.moveTo(
      target = added,
      overwrite = overwrite,
      keepAttributes = keepAttributes,
      followLinks = followLinks,
      onError = onError
    )
  }
}

/**
 * Add the sub files/directories to this directory.
 *
 * @param overwrite whether to overwrite when the target already exists.
 * @param [sites] the sub files/directories to be added to this directory.
 * @param keepSources whether to add [sites] to this directory by [Path.copyTo] operation,
 * otherwise the [sites] will be moved ([Path.moveTo]).
 * @param keepAttributes added all the attributes of this path to the target.
 * @param followLinks if the path to be added is a symbolic link and the value is `true`, then
 * add the link target, otherwise add the symbolic link itself.
 * @param onError what should be done when an error occurs when adding this path.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @throws PathAlreadyExistsException if the target already exists and [overwrite]
 * argument is set to `false`.
 * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
 * argument is set to `true`, the copy fails, the target folder must be emptied first.
 *
 * @see Path.copyTo
 * @see Path.moveTo
 */
fun Path.addAll(
  vararg sites: Path,
  overwrite: Boolean = false,
  keepSources: Boolean = true,
  keepAttributes: Boolean = false,
  followLinks: Boolean = true,
  onError: (Path, Throwable) -> FileHandlingErrorSolution = { _, exception -> throw exception }
) {
  requireDirectory()
  sites.forEach {
    val added = relativeTo(it)
    when {
      keepSources -> it.copyTo(
        target = added,
        overwrite = overwrite,
        keepAttributes = keepAttributes,
        followLinks = followLinks,
        onError = onError
      )
      else -> it.moveTo(
        target = added,
        overwrite = overwrite,
        keepAttributes = keepAttributes,
        followLinks = followLinks,
        onError = onError
      )
    }
  }
}

/**
 * Add the sub files/directories to this directory.
 *
 * @param overwrite whether to overwrite when the target already exists.
 * @param [specs] the sub files/directories to be added to this directory.
 * @param keepSources whether to add [specs] to this directory by [Path.copyTo] operation,
 * otherwise the [specs] will be moved ([Path.moveTo]).
 * @param keepAttributes added all the attributes of this path to the target.
 * @param followLinks if the path to be added is a symbolic link and the value is `true`, then
 * add the link target, otherwise add the symbolic link itself.
 * @param onError what should be done when an error occurs when adding this path.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @throws PathAlreadyExistsException if the target already exists and [overwrite]
 * argument is set to `false`.
 * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
 * argument is set to `true`, the copy fails, the target folder must be emptied first.
 *
 * @see Path.copyTo
 * @see Path.moveTo
 */
fun Path.addAll(
  specs: Iterable<Path>,
  overwrite: Boolean = false,
  keepSources: Boolean = true,
  keepAttributes: Boolean = false,
  followLinks: Boolean = true,
  onError: (Path, Throwable) -> FileHandlingErrorSolution = { _, exception -> throw exception }
) {
  requireDirectory()
  specs.forEach {
    val added = relativeTo(it)
    when {
      keepSources -> it.copyTo(
        target = added,
        overwrite = overwrite,
        keepAttributes = keepAttributes,
        followLinks = followLinks,
        onError = onError
      )
      else -> it.moveTo(
        target = added,
        overwrite = overwrite,
        keepAttributes = keepAttributes,
        followLinks = followLinks,
        onError = onError
      )
    }
  }
}

/**
 * Add the sub files/directories to this directory.
 *
 * @param overwrite whether to overwrite when the target already exists.
 * @param [specs] the sub files/directories to be added to this directory.
 * @param keepSources whether to add [specs] to this directory by [Path.copyTo] operation,
 * otherwise the [specs] will be moved ([Path.moveTo]).
 * @param keepAttributes added all the attributes of this path to the target.
 * @param followLinks if the path to be added is a symbolic link and the value is `true`, then
 * add the link target, otherwise add the symbolic link itself.
 * @param onError what should be done when an error occurs when adding this path.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @throws PathAlreadyExistsException if the target already exists and [overwrite]
 * argument is set to `false`.
 * @throws  DirectoryNotEmptyException if the target is a non-empty directory and the [overwrite]
 * argument is set to `true`, the copy fails, the target folder must be emptied first.
 *
 * @see Path.copyTo
 * @see Path.moveTo
 */
fun Path.addAll(
  specs: Sequence<Path>,
  overwrite: Boolean = false,
  keepSources: Boolean = true,
  keepAttributes: Boolean = false,
  followLinks: Boolean = true,
  onError: (Path, Throwable) -> FileHandlingErrorSolution = { _, exception -> throw exception }
) {
  requireDirectory()
  specs.forEach {
    val added = relativeTo(it)
    when {
      keepSources -> it.copyTo(
        target = added,
        overwrite = overwrite,
        keepAttributes = keepAttributes,
        followLinks = followLinks,
        onError = onError
      )
      else -> it.moveTo(
        target = added,
        overwrite = overwrite,
        keepAttributes = keepAttributes,
        followLinks = followLinks,
        onError = onError
      )
    }
  }
}

/**
 * Cleans a directory including sub-directories without deleting directories.
 *
 * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
 * link final target, otherwise delete the symbolic link itself.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.createFile
 */
//expect fun Path.cleanDirectory(
//  followLinks: Boolean = false,
//  filter: (Path) -> Boolean = { true },
//  onError: (Path, Throwable) -> DeleteErrorSolution = { _, exception -> throw exception }
//)

/**
 * Deletes a sub file/directory safely. If encounter the symbolic link, then the symbolic link
 * itself, not the final target of the link, is deleted, if you want to change this behavior, set
 * [followLinks] to `true`, this will remove the target of the link.
 *
 * @param name the sub file/directory name to be deleted in this directory.
 * @param recursively if encounter a folder, do you want to delete it recursively.
 * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
 * link final target, otherwise delete the symbolic link itself.
 * @param filter if encounter a directory and [recursively] is true, you can filter to exclude some
 * files from deleting.
 * @param onError what should be done when an error occurs when deleting [name].
 *
 * @return if the deletion fails, it returns `false`.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.delete
 */
fun Path.deleteChild(
  name: String,
  recursively: Boolean = false,
  followLinks: Boolean = false,
  filter: (Path) -> Boolean = { true },
  onError: (Path, Throwable) -> DeleteErrorSolution = { _, exception -> throw exception }
): Boolean {
  requireDirectory()
  return resolve(name).delete(recursively, followLinks, filter, onError)
}

/**
 * Deletes a sub files/directories safely. If encounter the symbolic link, then the symbolic link
 * itself, not the final target of the link, is deleted, if you want to change this behavior, set
 * [followLinks] to `true`, this will remove the target of the link.
 *
 * @param names the sub files/directories name to be deleted in this directory.
 * @param recursively if encounter a folder, do you want to delete it recursively.
 * @param followLinks if encounter a symbolic link and the value is `true`, then delete the
 * link final target, otherwise delete the symbolic link itself.
 * @param filter if encounter a directory and [recursively] is true, you can filter to exclude some
 * files from deleting.
 * @param onError what should be done when an error occurs when deleting [names].
 *
 * @return if the deletion fails, it returns `false`.
 *
 * @throws IllegalPathException if this is not a directory, see [requireDirectory].
 *
 * @see Path.delete
 * @see Path.children
 */
fun Path.deleteChildren(
  vararg names: String,
  recursively: Boolean = false,
  followLinks: Boolean = false,
  filter: (Path) -> Boolean = { true },
  onError: (Path, Throwable) -> DeleteErrorSolution = { _, exception -> throw exception }
): Boolean {
  requireDirectory()
  return names.all {
    val added = resolve(it)
    added.delete(recursively, followLinks, filter, onError)
  }
}

/** @see Path.add */
operator fun Path.plus(child: Path) = apply { add(child) }
operator fun Path.plusAssign(child: Path) = add(child)

/** @see Path.addAll */
operator fun Path.plus(children: Iterable<Path>) = apply { addAll(children) }
operator fun Path.plus(children: Sequence<Path>) = apply { addAll(children) }
operator fun Path.plus(children: Array<Path>) = apply { addAll(*children) }
operator fun Path.plusAssign(children: Iterable<Path>) = addAll(children)
operator fun Path.plusAssign(children: Sequence<Path>) = addAll(children)
operator fun Path.plusAssign(children: Array<Path>) = addAll(*children)

/** @see Path.deleteChild */
operator fun Path.minus(child: String) = apply { deleteChild(child) }
operator fun Path.minus(child: Path) = apply { child.delete() }
operator fun Path.minusAssign(child: String) = deleteChild(child).let { }
operator fun Path.minusAssign(child: Path) = child.delete().let { }