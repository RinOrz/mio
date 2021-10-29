//@file:Suppress("NO_ACTUAL_FOR_EXPECT", "NOTHING_TO_INLINE")
//
//package com.meowool.mio
//
//import com.meowool.sweekt.ifNull
//import com.meowool.sweekt.safeCast
//import kotlinx.coroutines.flow.Flow
//import okio.Buffer
//import okio.BufferedSink
//import okio.BufferedSource
//import okio.ByteString
//import okio.Source
//import okio.buffer
//
///**
// * Removes all bytes from this source and returns them as a string.
// *
// * @param charset character set to use.
// *
// * @author 凛 (https://github.com/RinOrz)
// */
//expect fun Source.readText(charset: Charset = Charsets.UTF_8): String
//
///**
// * Performs the given [action] on each line of this source.
// *
// * @param charset character set to use for reading line of source.
// */
//expect inline fun Source.forEachLine(
//  charset: Charset = Charsets.UTF_8,
//  action: (String) -> Unit
//)
//
///**
// * Collects every line in the source with a provided [action].
// *
// * @param charset character set to use for reading line of source.
// */
//expect suspend fun Read.collectLine(
//  charset: Charset = Charsets.UTF_8,
//  action: suspend (String) -> Unit
//)
//
///**
// * Lazily read each line in the source, only when the stream is collected, will the
// * line be read on demand.
// *
// * @param charset character set to use for reading line of source.
// */
//expect fun Source.lines(charset: Charset = Charsets.UTF_8): Flow<String>
//
///**
// * Reads all lines in the file directly.
// * Unlike [lines], this function will reads the entire source at once.
// *
// * @param charset character set to use for reading line of source.
// */
//expect fun Source.readLines(charset: Charset = Charsets.UTF_8): List<String>
//
///**
// * Reads a line of string and delete them from the source.
// *
// * @param charset character set used by the read string.
// */
//expect fun BufferedSource.readLine(charset: Charset = Charsets.UTF_8): String?
//
///**
// * Reads a line of byte string and delete them from the source.
// */
//expect fun BufferedSource.readByteStringLine(): ByteString?
//
//inline fun BufferedSource.readUInt(): UInt = readInt().toUInt()
//
//inline fun BufferedSource.readUIntLe(): UInt = readIntLe().toUInt()
//
//inline fun BufferedSource.readUByte(): UByte = readByte().toUByte()
//
//inline fun BufferedSource.readULong(): ULong = readLong().toULong()
//
//inline fun BufferedSource.readULongLe(): ULong = readLongLe().toULong()
//
//inline fun BufferedSource.readUShort(): UShort = readShort().toUShort()
//
//inline fun BufferedSource.readUShortLe(): UShort = readShortLe().toUShort()
//
///**
// * Write the [charSequence] encoded in the given [charset] to the sink.
// */
//expect fun BufferedSink.write(
//  charSequence: CharSequence,
//  charset: Charset = Charsets.UTF_8
//): BufferedSink
//
///**
// * Writes the line of [byteString] to this sink, will add a newline at the end of the sink.
// *
// * It is equivalent to the following expression:
// * ```
// * bufferedSink.write(byteString).writeUtf8(System.lineSeparator())
// * ```
// */
//fun BufferedSink.writeLine(byteString: ByteString): BufferedSink =
//  this.write(byteString).writeUtf8(lineSeparator)
//
///**
// * Encodes the line of [charSequence] in UTF-8 and writes it to this sink, will add a newline at
// * the end of the sink.
// *
// * It is equivalent to the following expression:
// * ```
// * bufferedSink.write("Ha ha ha").writeUtf8(System.lineSeparator())
// * ```
// */
//fun BufferedSink.writeLine(
//  charSequence: CharSequence,
//  charset: Charset = Charsets.UTF_8
//): BufferedSink = this.write(charSequence, charset).writeUtf8(lineSeparator)
//
///**
// * Encodes the line of [string] in UTF-8 and writes it to this sink, will add a newline at the end
// * of the sink.
// *
// * It is equivalent to the following expression:
// * ```
// * bufferedSink.writeUtf8("Ha ha ha").writeUtf8(System.lineSeparator())
// * ```
// */
//fun BufferedSink.writeUtf8Line(string: String): BufferedSink =
//  this.writeUtf8(string).writeUtf8(lineSeparator)
//
///**
// * Returns a copy of [Buffer] whose content is the bytes from [offset] to [byteCount]
// * of this buffer.
// */
//fun Buffer.copy(offset: Long, byteCount: Long = size - offset): Buffer = Buffer().also { out ->
//  this.copyTo(out, offset, byteCount)
//}
//
///**
// * Reads [byteCount] bytes in this buffer to the given [target] buffer
// */
//fun Buffer.readToBuffer(byteCount: Long, target: Buffer = Buffer()): Buffer = target.also {
//  read(it, byteCount)
//}
//
///**
// * System line separator.
// */
//internal expect val lineSeparator: String
//
//internal val Source.buffered: BufferedSource
//  get() = this.safeCast<BufferedSource>().ifNull(::buffer)