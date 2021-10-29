package com.meowool.mio

import com.meowool.sweekt.array.ByteArrayBuilder
import java.io.EOFException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.Files
import java.nio.file.Paths

internal inline fun ByteBuffer.repos() = position(0)

abstract class BaseFileChannelBenchmark {
  private val path = Paths.get("/Users/rin/Documents/Develop/Projects/meowool/toolkit/mio/benchmark/src/main/resources/file.txt")
  private val channel = Files.newByteChannel(path)
  private val buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
  private val raf = RandomAccessFile(path.toFile(), "rw")

  private fun changeBuffer(limit: Int) = buffer.repos().limit(limit)

  open fun readRange() {
    var pos = 50L
    val count = channel.size() - 20L
    val builder = ByteArrayBuilder((count - pos).toInt())

    channel.position(pos)
    while (pos < count) {
      channel.read(changeBuffer(1))
      builder.append(buffer[0])
      pos++
    }

    builder.toByteArray()
  }

  open fun readRangeRaf() {
    var pos = 50L
    val count = raf.length() - 20L
    val builder = ByteArrayBuilder((count - pos).toInt())

    raf.seek(pos)
    while (pos < count) {
      builder.append(raf.readByte())
      pos++
    }

    builder.toByteArray()
  }

  open fun readAll() {
    val builder = ByteArrayBuilder()

    channel.position(100)
    while (true) {
      channel.read(changeBuffer(1)).takeIf { it != -1 } ?: break
      builder.append(buffer[0])
    }

    channel.position(0)
    for (i in 0 until 100) {
      channel.read(changeBuffer(1))
      builder.append(buffer[0])
    }

    builder.toByteArray()
  }

  open fun readAllRaf() {
    val builder = ByteArrayBuilder()

    raf.seek(100)
    while (true) {
      try {
        builder.append(raf.readByte())
      } catch (e: EOFException) {
        break
      }
    }

    raf.seek(0)
    for (i in 0 until 100) builder.append(raf.readByte())
  }

  open fun channel() {
//    channel.position(27).read(changeBuffer(1))
//    buffer.reposition().get()
//
//    channel.position(7).read(changeBuffer(4))
//    buffer.reposition().int
//
//    channel.position(8).read(changeBuffer(4))
//    buffer.reposition().int

    channel.position(0)
    readLine(channel)
  }

  open fun randomAccessFile() {
//    raf.seek(27)
//    raf.readByte()
//
//    raf.seek(7)
//    raf.readInt()

    raf.seek(0)
    raf.readLine()
  }
}

const val LineFeed = '\n'.code.toByte()
const val CarriageReturn = '\r'.code.toByte()

fun readLine(channel: SeekableByteChannel): String {
  val buffer = ByteBuffer.allocate(1)
  val builder = StringBuilder(DEFAULT_BUFFER_SIZE)
  var position = channel.position()

  fun readNext(block: (Byte) -> Boolean): Int {
    val read = channel.read(buffer.repos())
    if (read != -1) block(buffer[0]).let { if (!it) return -1 }
    return read
  }

  while (true) {
    val read = readNext { byte ->
      position++
      when (byte) {

        // In the case of `\r` or `\r\n`
        CarriageReturn -> {
          val mark = channel.position()
          // Consume next
          readNext { next ->
            // Reset mark if combine with the next one is not `\r\n`
            if (next != LineFeed) channel.position(mark)
            true
          }
          // Just `\r`, break the loop directly
          false
        }

        // Break the loop directly if the byte is `\n`
        LineFeed -> false

        // Normal reading byte
        else -> {
          builder.append(byte.toInt().toChar())
          true
        }
      }
    }
    if (read == -1) break
  }
  return builder.toString()
}


//fun readLastLine(channel: SeekableByteChannel): String {
//  val buffer = ByteBuffer.allocate(1)
//  val builder = StringBuilder(DEFAULT_BUFFER_SIZE)
//  var position = channel.size() - 1
//
//  fun readPrev(block: (Byte) -> Boolean): Int {
//    val read = channel.position(position).read(buffer.reposition())
//    if (read != -1) block(buffer[0]).let { if (!it) return -1 }
//    return read
//  }
//
//  while (true) {
//    val read = readNext { byte ->
//      position++
//      when (byte) {
//
//        // In the case of `\r` or `\r\n`
//        CarriageReturn -> {
//          val mark = channel.position()
//          // Consume next
//          readNext { next ->
//            // Reset mark if combine with the next one is not `\r\n`
//            if (next != LineFeed) channel.position(mark)
//            true
//          }
//          // Just `\r`, break the loop directly
//          false
//        }
//
//        // Break the loop directly if the byte is `\n`
//        LineFeed -> false
//
//        // Normal reading byte
//        else -> {
//          builder.append(byte.toInt().toChar())
//          true
//        }
//      }
//    }
//    if (read == -1) break
//  }
//  return builder.toString()
//}

fun readLast() {

}

fun main() {
  val path = Paths.get("/Users/rin/Documents/Develop/Projects/meowool/toolkit/mio/benchmark/src/main/resources/small_file.txt")
  val channel = Files.newByteChannel(path)
  Files.writeString(path, Files.readString(path).replace("\r", "\r\n"))
//  println(RandomAccessFile(path.toFile(), "rw").readLine())
//  println(readLine(channel))
}

