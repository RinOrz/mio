package datachannel

import com.meowool.mio.DataChannel
import com.meowool.mio.internal.DataChannelImpl
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.longs.shouldBeNegative
import io.kotest.matchers.longs.shouldBeZero
import io.kotest.matchers.longs.shouldNotBeZero
import io.kotest.matchers.shouldBe
import java.nio.file.Files

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class DataChannelTests: FreeSpec() {
  private val fileString = """
    ONE_LINE
    TWO_LINE
  """.trimIndent()

  init {
    val channel = tempfile().toPath()
      .also { Files.writeString(it, fileString) }
      .let(Files::newByteChannel)
    DataChannelImpl(channel).use { it.test() }
  }

  private fun DataChannel.test() {
    "test cursors" {
      startCursor.apply {
        testIndex().shouldBeZero()

        testIndex { moveRight() }.shouldNotBeZero()
        testIndex { moveRight(5) } shouldBe 5
        testIndex { moveRight(5) } shouldBe 5

        testIndex { moveLeft() }.shouldBeNegative()
        testIndex { moveLeft(5) } shouldBe -5

        test { isReachStart }.shouldBeTrue()
        test { moveToLast().isReachEnd }.shouldBeTrue()

        testIndex { moveRight().moveDown() }.let(::peekRange) shouldBe "TWO_LINE"

        testIndex { moveRight(9) }.let(::peekAt) shouldBeChar 'T'
        test { moveRight(9).moveUp() }.apply {
          index shouldBe 0
          peekLine() shouldBe "ONE_LINE"
        }
      }

      endCursor.testIndex() shouldBe lastIndex
    }

    "test byte" - {
      "test order" {
        retest {
          peek() shouldBeChar 'O'
          peekLast() shouldBeChar 'E'
        }

        retest {
          peekString(3) shouldBe "ONE"
          peekRange(4, 8) shouldBe "LINE"
        }
      }

      "test random access" {
        peekAt(4) shouldBeChar 'L'
        peekAt(5) shouldBeChar 'I'
        peekAt(6) shouldBeChar 'N'
        peekAt(7) shouldBeChar 'E'
      }
    }

    "test all bytes" {
      peekAll() shouldBe fileString
      peekAllBytes().decodeToString() shouldBe fileString
    }

    "test lines" {
      val list = listOf("ONE_LINE", "TWO_LINE")

      retest {
        peekLine() shouldBe list[0]
        peekLine() shouldBe list[1]
      }
      retest {
        peekLastLine() shouldBe list[1]
        peekLastLine() shouldBe list[0]
      }
      retest {
        peekLineBytes().decodeToString() shouldBe list[0]
        peekLastLineBytes().decodeToString() shouldBe list[1]
      }
      retest { peekAllLines() shouldBe list }
      retest { peekAllLines(reversed = true) shouldBe list.asReversed() }
    }

    "test pops" {
      retest {
        popAt(2) shouldBeChar 'E'
        println("skip")
        peekLine() shouldBe "ON_LINE"
      }
    }
  }


  private inline fun <R> DataChannel.Cursor.test(
    block: DataChannel.Cursor.() -> R
  ): R = this.asClue { reset(); block() }

  private inline fun DataChannel.Cursor.testIndex(
    block: DataChannel.Cursor.() -> DataChannel.Cursor = { this }
  ): Long = test { block().index }

  private inline fun DataChannel.retest(
    block: DataChannel.() -> Unit
  ) = apply { resetCursors(); block() }

  private infix fun Byte.shouldBeChar(char: Char) = this.toInt().toChar().shouldBe(char)
}