@file:Suppress("NOTHING_TO_INLINE")

package com.meowool.mio.internal

import com.meowool.mio.channel.DataChannelInfo
import com.meowool.sweekt.array.ByteArrayBuilder.Companion.MaxArraySize
import com.meowool.sweekt.throwIf
import java.nio.channels.ClosedChannelException

internal fun Long.toLegalInt(exception: String = "Required size too large"): Int {
  throwIf(this > MaxArraySize) { OutOfMemoryError(exception) }
  return this.toInt()
}

internal fun DataChannelInfo.checkIndices(startIndex: Long, endIndex: Long) {
  throwIf(startIndex < 0) { IndexOutOfBoundsException("startIndex:$startIndex < 0") }
  throwIf(endIndex > size) { IndexOutOfBoundsException("endIndex:$endIndex > channelSize:$size") }
}

internal fun DataChannelInfo.checkIndex(index: Long) {
  throwIf(index < 0) { IndexOutOfBoundsException("index:$index < 0") }
  throwIf(index > size) { IndexOutOfBoundsException("index:$index > channelSize:$size") }
}

internal inline fun Byte.isLineTerminator(): Boolean = this == LineFeed || this == CarriageReturn

internal const val LineFeed = '\n'.code.toByte()
internal const val CarriageReturn = '\r'.code.toByte()
