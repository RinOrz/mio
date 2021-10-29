@file:Suppress("BlockingMethodInNonBlockingContext")

package com.meowool.mio

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.encode
import okio.ByteString.Companion.encodeUtf8
import okio.Source
import okio.use


/**
 * Removes all bytes from this source and returns them as a string.
 *
 * @param charset character set to use.
 *
 * @author 凛 (https://github.com/RinOrz)
 */
actual fun Source.readText(charset: Charset): String = buffered.use {
  if (charset == Charsets.UTF_8) it.readUtf8() else it.readByteString().string(charset)
}

/**
 * Performs the given [action] on each line of this source.
 *
 * @param charset character set to use for reading line of source.
 */
actual inline fun Source.forEachLine(charset: Charset, action: (String) -> Unit) =
  readLines(charset).forEach(action)

/**
 * Collects every line in the source with a provided [action].
 *
 * @param charset character set to use for reading line of source.
 */
actual suspend fun Source.collectLine(charset: Charset, action: suspend (String) -> Unit) =
  lines(charset).collect(action)

/**
 * Lazily read each line in the source, only when the stream is collected, will the
 * line be read on demand.
 *
 * @param charset character set to use for reading line of source.
 */
actual fun Source.lines(charset: Charset): Flow<String> = buffered.use {
  flow { it.readUtf8Line()?.apply { emit(this) } }
}

/**
 * Reads all lines in the file directly.
 * Unlike [lines], this function will reads the entire source at once.
 *
 * @param charset character set to use for reading line of source.
 */
actual fun Source.readLines(charset: Charset): List<String> {
  val result = arrayListOf<String>()
  buffered.use {
    while (true) {
      result.add(it.readLine(charset) ?: break)
    }
  }
  return result
}

/**
 * Reads a line of string and delete them from the source.
 *
 * @param charset character set used by the read string.
 */
actual fun BufferedSource.readLine(charset: Charset): String? = when (charset) {
  Charsets.UTF_8 -> readUtf8Line()
  else -> readByteStringLine()?.string(charset)
}

/**
 * Reads a line of byte string and delete them from the source.
 */
actual fun BufferedSource.readByteStringLine(): ByteString? {
  val newline = indexOf('\n'.code.toByte())

  return if (newline == -1L) {
    if (buffer.size != 0L) {
      readByteString(buffer.size)
    } else {
      null
    }
  } else {
    buffer.readByteStringLine(newline)
  }
}

/**
 * Write the [charSequence] encoded in the given [charset] to the sink.
 */
actual fun BufferedSink.write(
  charSequence: CharSequence,
  charset: Charset
): BufferedSink = when(charset) {
  Charsets.UTF_8 -> writeUtf8(charSequence.toString())
  else -> write(charSequence.toString().encode(charset))
}

/**
 * System line separator.
 */
internal actual val lineSeparator: String = System.lineSeparator()

private fun Buffer.readByteStringLine(newline: Long): ByteString {
  return when {
    newline > 0 && this[newline - 1] == '\r'.code.toByte() -> {
      // Read everything until '\r\n', then skip the '\r\n'.
      val result = readByteString(newline - 1L)
      skip(2L)
      result
    }
    else -> {
      // Read everything until '\n', then skip the '\n'.
      val result = readByteString(newline)
      skip(1L)
      result
    }
  }
}