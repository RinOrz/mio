@file:Suppress("NewApi")

package com.meowool.mio

import android.os.StatFs

/**
 * Returns the block space of this path.
 *
 * Generally, it is used to mean the space of the file block, such as obtaining the size of
 * the android internal storage space.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
actual val Path.blockSize: Long get() = StatFs(this.toString()).let { it.blockSizeLong * it.blockCountLong }

/**
 * Returns the available size of this file or directory.
 *
 * @see blockSize
 */
actual val Path.availableSize: Long get() = StatFs(this.toString()).let { it.blockSizeLong * it.availableBlocksLong }