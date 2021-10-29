package com.meowool.mio

/**
 * Throws an [IllegalPathException] if this path is not exists. Otherwise returns itself.
 *
 * @param followLinks see [IPath.notExists]
 */
inline fun IPath.requireExists(
  followLinks: Boolean = true,
  lazyMessage: () -> Any = { "because $this does not exists." }
): IPath {
  if (exists(followLinks).not()) {
    val message = lazyMessage()
    throw IllegalPathException(this, message.toString())
  }
  return this
}

/**
 * Throws an [IllegalPathException] if this path is already exists. Otherwise returns itself.
 *
 * @param followLinks see [IPath.notExists]
 */
inline fun IPath.requireNotExists(
  followLinks: Boolean = true,
  lazyMessage: () -> Any = { "because $this already exists." }
): IPath {
  if (notExists(followLinks).not()) {
    val message = lazyMessage()
    throw IllegalPathException(this, message.toString())
  }
  return this
}

/**
 * Throws an [IllegalPathException] if this path is a file. Otherwise returns itself.
 *
 * @see IPath.isDirectory
 */
inline fun IPath.requireDirectory(
  lazyMessage: () -> Any = { "because $this is not a directory." }
): IPath {
  if (isDirectory.not()) {
    val message = lazyMessage()
    throw IllegalPathException(this, message.toString())
  }
  return this
}

/**
 * Throws an [IllegalPathException] if this path is a directory. Otherwise returns itself.
 *
 * @see IPath.isDirectory
 */
inline fun IPath.requireRegularFile(
  lazyMessage: () -> Any = { "because $this is not a file." }
): IPath {
  if (isRegularFile.not()) {
    val message = lazyMessage()
    throw IllegalPathException(this, message.toString())
  }
  return this
}