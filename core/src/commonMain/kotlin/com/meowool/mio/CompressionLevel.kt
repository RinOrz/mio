package com.meowool.mio

/**
 * For the definition of different compression levels, the compression ratio depends on [level].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
sealed class CompressionLevel(val level: Int) {

  /** Only stores the files, do not compress */
  object None: CompressionLevel(0)

  /** Compress in standard mode */
  object Default: CompressionLevel(8)

  /** Compress with the fastest speed, the compression rate in this mode is the lowest */
  object Fastest: CompressionLevel(1)

  /** Compress with the best method, the compression rate in this mode is the highest */
  object Optimal: CompressionLevel(9)

  internal class Custom(level: Int) : CompressionLevel(level)

  companion object {
    /** Custom compression level */
    operator fun invoke(level: Int): CompressionLevel = Custom(level)
  }
}