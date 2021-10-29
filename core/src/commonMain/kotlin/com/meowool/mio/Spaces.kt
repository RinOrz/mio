@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package com.meowool.mio

/**
 * Returns the block space of this path.
 *
 * Generally, it is used to mean the space of the file block, such as obtaining the size of
 * the android internal storage space.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
expect val IPath.blockSpace: Long

/**
 * Returns the available space of this path.
 *
 * @see blockSpace
 */
expect val IPath.availableSpace: Long

/**
 * Returns the used space of this file or directory.
 *
 * @see blockSpace
 * @see availableSpace
 */
val IPath.usedSpace: Long get() = blockSpace - availableSpace