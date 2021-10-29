import com.meowool.mio.channel.ByteOrder
import com.meowool.mio.internal.DataBuffer
import com.meowool.mio.internal.PieceBuffer
import com.meowool.mio.internal.toByteOrder
import com.meowool.sweekt.array.buildByteArray
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.nio.ByteBuffer

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class PieceBufferTests : FreeSpec() {
  init {
    val raw = "DATA".toByteArray()
    val buffer = PieceBuffer(TestDataBuffer(ByteBuffer.wrap(raw)))

    "check init" {
      buildByteArray {
        buffer.forEach(::append)
      } shouldBe raw
    }

    "add" {
      buffer.add('_'.code.toByte())
      buffer shouldBeData listOf("DATA", "_")
      buffer.add('2'.code.toByte())
      buffer shouldBeData listOf("DATA", "_2")
    }

    "remove" {
      buffer.remove(0, 2)
      buffer shouldBeData listOf("TA", "_2")
      buffer.remove(3)
      buffer shouldBeData listOf("TA", "_")
    }

    "put" {
      buffer.put(8, '8'.code.toByte())
      buffer shouldBeData listOf("TA", "_", "     ", "8")

      buffer.put(3, '3'.code.toByte())
      buffer shouldBeData listOf("TA", "_", "3", "    ", "8")

      buffer.put(6, '6'.code.toByte())
      buffer shouldBeData listOf("TA", "_", "3", "  ", "6", " ", "8")

      buffer.put(6, ' '.code.toByte())
      buffer.put(7, '7'.code.toByte())
      buffer shouldBeData listOf("TA", "_", "3", "  ", " ", "7", "8")

      buffer.put(5, '5'.code.toByte())
      buffer shouldBeData listOf("TA", "_", "3", " ", "5", " ", "7", "8")

      buffer.put(4, '4'.code.toByte())
      buffer shouldBeData listOf("TA", "_", "3", "4", "5", " ", "7", "8")

      buffer.put(2, '|'.code.toByte())
      buffer shouldBeData listOf("TA", "|", "3", "4", "5", " ", "7", "8")

      buffer.put(1, '1'.code.toByte())
      buffer shouldBeData listOf("T1", "|", "3", "4", "5", " ", "7", "8")
    }

    "change size" {
      buffer.size += 10
      buffer shouldBeData listOf("T1", "|", "3", "4", "5", " ", "7", "8", "          ")
      buffer.size = 15
      buffer shouldBeData listOf("T1", "|", "3", "4", "5", " ", "7", "8", "      ")
      buffer.size = 6
      buffer shouldBeData listOf("T1", "|", "3", "4", "5")
      buffer.size = 0
      buffer shouldBeData emptyList()
    }

    "insert" {
      buffer.insert(0, '0'.code.toByte())
      buffer shouldBeData listOf("0")
      buffer.insert(5, '5'.code.toByte())
      buffer shouldBeData listOf("0", "    ", "5")
      buffer.insert(2, '2'.code.toByte())
      buffer shouldBeData listOf("0", " ", "2", "   ", "5")
      buffer.insert(7, '7'.code.toByte())
      buffer shouldBeData listOf("0", " ", "2", "   ", "5", "7")
    }
  }

  private infix fun PieceBuffer.shouldBeData(expected: List<String>): List<String> {
    val list = mutableListOf<String>()
    pieces.forEach {
      val byte = it.selectBuffer()?.getBytes(it.relativeStart, it.size.toInt())
      val data = byte?.decodeToString() ?: " ".repeat(it.size.toInt())
      list.add(data)
    }
    expected.asClue {
      list shouldBe expected
      size shouldBe expected.sumOf { it.length }
    }
    return list
  }

  companion object {
    private class TestDataBuffer(private val buffer: ByteBuffer) : DataBuffer<TestDataBuffer> {

      override val size: Long
        get() = buffer.limit().toLong()

      override var order: ByteOrder
        get() = buffer.order().toByteOrder()
        set(value) {
          buffer.order(value.toByteOrder())
        }

      override fun getByte(index: Long): Byte = buffer.get(index.toInt())

      override fun getChar(index: Long): Char = buffer.getChar(index.toInt())

      override fun getInt(index: Long): Int = buffer.getInt(index.toInt())

      override fun getLong(index: Long): Long = buffer.getLong(index.toInt())

      override fun getFloat(index: Long): Float = buffer.getFloat(index.toInt())

      override fun getShort(index: Long): Short = buffer.getShort(index.toInt())

      override fun getDouble(index: Long): Double = buffer.getDouble(index.toInt())

      override fun getBytes(index: Long, count: Int): ByteArray =
        ByteArray(count).also { buffer.position(index.toInt()).get(it) }

      override fun getAllBytes(): ByteArray = when {
        buffer.hasArray() -> buffer.array()
        else -> ByteArray(buffer.limit()).apply { buffer.rewind().get(this) }
      }

      override fun put(index: Long, data: Byte): TestDataBuffer =
        apply { buffer.put(index.toInt(), data) }

      override fun put(index: Long, data: Short): TestDataBuffer =
        apply { buffer.putShort(index.toInt(), data) }

      override fun put(index: Long, data: Char): TestDataBuffer =
        apply { buffer.putChar(index.toInt(), data) }

      override fun put(index: Long, data: Int): TestDataBuffer =
        apply { buffer.putInt(index.toInt(), data) }

      override fun put(index: Long, data: Float): TestDataBuffer =
        apply { buffer.putFloat(index.toInt(), data) }

      override fun put(index: Long, data: Long): TestDataBuffer =
        apply { buffer.putLong(index.toInt(), data) }

      override fun put(index: Long, data: Double): TestDataBuffer =
        apply { buffer.putDouble(index.toInt(), data) }

      override fun clear(): TestDataBuffer =
        apply { buffer.clear() }
    }
  }
}