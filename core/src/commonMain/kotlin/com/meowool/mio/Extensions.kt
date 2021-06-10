package com.meowool.mio

import com.meowool.sweekt.select

/**
 * Resolve and return the extension in the [fileName]. Whether the returned result contains dot
 * depends on [withDot].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
fun resolveFileExtension(fileName: String, withDot: Boolean = false): String =
  isHiddenNameWithoutExtension(fileName).select(
    `true` = "", // no extension
    `false` = when (val extensionIndex = fileName.lastIndexOf('.')) {
      -1 -> ""
      else -> fileName.substring(extensionIndex + withDot.select(0, 1), fileName.length)
    }
  )

/**
 * if there is only one dot and at the first character,
 * then all are returned (both a hidden file without extension).
 */
internal fun isHiddenNameWithoutExtension(fileName: String): Boolean =
  fileName.first() == '.' && fileName.count { it == '.' } == 1
