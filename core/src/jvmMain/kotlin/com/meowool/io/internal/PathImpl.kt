package com.meowool.io.internal

import com.meowool.io.NioPath
import com.meowool.io.Path

/**
 * The default implementation of Mio path based on [NioPath].
 *
 * @author 凛 (https://github.com/RinOrz)
 */
@PublishedApi
internal class PathImpl(nioPath: NioPath) : BasePathImpl<PathImpl>(nioPath) {
  override fun NioPath.materialize(): PathImpl = PathImpl(this)
}