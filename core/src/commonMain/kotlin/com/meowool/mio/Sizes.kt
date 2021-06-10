package com.meowool.mio

/**
 * Returns the total size of this file or directory.
 *
 * The difference between it and [Path.size] is that if this is a directory, the size
 * occupied by it and all its children will be returned.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
expect val Path.totalSize: Long

/**
 * Returns the block size of this file or directory.
 *
 * Generally, it is used to means the size of the file block, such as obtaining the size of
 * the android internal storage space.
 */
expect val Path.blockSize: Long

/**
 * Returns the available size of this file or directory.
 *
 * @see blockSize
 */
expect val Path.availableSize: Long

/**
 * Returns the used size of this file or directory.
 *
 * @see blockSize
 * @see availableSize
 */
val Path.usedSize: Long get() = blockSize - availableSize