package com.meowool.mio.internal

import com.meowool.mio.ChannelEmptyException
import com.meowool.mio.ChannelUnderflowException
import com.meowool.mio.channel.ByteOrder
import com.meowool.mio.ifNull
import com.meowool.sweekt.array.ByteArrayBuilder
import com.meowool.sweekt.ifTrue
import com.meowool.sweekt.throwIf
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * @author 凛 (https://github.com/RinOrz)
 */
internal class FileDataBuffer(private val fileChannel: FileChannel) : DataBuffer<FileDataBuffer> {

  override val size: Long
    get() = fileChannel.size()

  override var order: ByteOrder
    get() = singleByteBuffer.order().toByteOrder()
    set(value) {
      value.toByteOrder().also {
        singleByteBuffer.order(it)
        bytesBufferCache?.order(it)
      }
    }

  override fun getByte(index: Long): Byte {
    readFully(index, singleByteBuffer)
    return singleByteBuffer.get(0)
  }

  override fun getShort(index: Long): Short = bytesBuffer(Short.SIZE_BYTES).let {
    readFully(index, it)
    it.getShort(0)
  }

  override fun getChar(index: Long): Char = bytesBuffer(Char.SIZE_BYTES).let {
    readFully(index, it)
    it.getChar(0)
  }

  override fun getInt(index: Long): Int = bytesBuffer(Int.SIZE_BYTES).let {
    readFully(index, it)
    it.getInt(0)
  }

  override fun getLong(index: Long): Long = bytesBuffer(Long.SIZE_BYTES).let {
    readFully(index, it)
    it.getLong(0)
  }

  override fun getFloat(index: Long): Float = bytesBuffer(Float.SIZE_BYTES).let {
    readFully(index, it)
    it.getFloat(0)
  }

  override fun getDouble(index: Long): Double = bytesBuffer(Double.SIZE_BYTES).let {
    readFully(index, it)
    it.getDouble(0)
  }

  override fun getBytes(index: Long, count: Int): ByteArray = bytesBuffer(count).let {
    readFully(index, it)
    ByteArray(count).apply { it.rewind().get(this) }
  }

  override fun getAllBytes(): ByteArray = bytesBuffer(size.toLegalInt()).let {
    when {
      it.hasArray() -> it.array().copyOf(it.limit())
      else -> ByteArray(it.limit()).apply { it.rewind().get(this) }
    }
  }

  override fun getByteOrNull(index: Long): Byte? =
    read(index, singleByteBuffer).ifTrue { singleByteBuffer.get(0) }

  override fun getShortOrNull(index: Long): Short? = bytesBuffer(Short.SIZE_BYTES).let {
    read(index, it).ifTrue { it.getShort(0) }
  }

  override fun getCharOrNull(index: Long): Char? = bytesBuffer(Char.SIZE_BYTES).let {
    read(index, it).ifTrue { it.getChar(0) }
  }

  override fun getIntOrNull(index: Long): Int? = bytesBuffer(Int.SIZE_BYTES).let {
    read(index, it).ifTrue { it.getInt(0) }
  }

  override fun getFloatOrNull(index: Long): Float? = bytesBuffer(Float.SIZE_BYTES).let {
    read(index, it).ifTrue { it.getFloat(0) }
  }

  override fun getLongOrNull(index: Long): Long? = bytesBuffer(Long.SIZE_BYTES).let {
    read(index, it).ifTrue { it.getLong(0) }
  }

  override fun getDoubleOrNull(index: Long): Double? = bytesBuffer(Double.SIZE_BYTES).let {
    read(index, it).ifTrue { it.getDouble(0) }
  }

  override fun getBytesOrNull(index: Long, count: Int): ByteArray? = bytesBuffer(count).let {
    read(index, it).ifTrue { ByteArray(count).apply { it.rewind().get(this) } }
  }

  override fun put(index: Long, data: Byte): FileDataBuffer = apply {
    fileChannel.position(index).write(singleByteBuffer.put(data))
  }

  override fun put(index: Long, data: Short): FileDataBuffer = apply {
    fileChannel.position(index).write(bytesBuffer(Short.SIZE_BYTES).putShort(data))
  }

  override fun put(index: Long, data: Char): FileDataBuffer = apply {
    fileChannel.position(index).write(bytesBuffer(Char.SIZE_BYTES).putChar(data))
  }

  override fun put(index: Long, data: Int): FileDataBuffer = apply {
    fileChannel.position(index).write(bytesBuffer(Int.SIZE_BYTES).putInt(data))
  }

  override fun put(index: Long, data: Float): FileDataBuffer = apply {
    fileChannel.position(index).write(bytesBuffer(Float.SIZE_BYTES).putFloat(data))
  }

  override fun put(index: Long, data: Long): FileDataBuffer = apply {
    fileChannel.position(index).write(bytesBuffer(Long.SIZE_BYTES).putLong(data))
  }

  override fun put(index: Long, data: Double): FileDataBuffer = apply {
    fileChannel.position(index).write(bytesBuffer(Double.SIZE_BYTES).putDouble(data))
  }

  override fun clear(): FileDataBuffer = apply {
    fileChannel.truncate(0)
  }

  private fun readFully(index: Long, target: ByteBuffer) = throwIf(read(index, target)) {
    ChannelUnderflowException("There is no data at index `$index`.")
  }

  private fun read(index: Long, target: ByteBuffer) = fileChannel.position(index).read(target) == -1

  companion object {
    private var singleByteBufferCache: ByteBuffer? = null
    private var bytesBufferCache: ByteBuffer? = null

    private val singleByteBuffer: ByteBuffer
      get() = singleByteBufferCache.ifNull {
        ByteBuffer.allocate(1).also { singleByteBufferCache = it }
      }.rewind()

    private fun bytesBuffer(count: Int): ByteBuffer = bytesBufferCache.ifNull {
      ByteBuffer.allocate(DEFAULT_BUFFER_SIZE).also { bytesBufferCache = it }
    }.rewind().limit(count)
  }
}