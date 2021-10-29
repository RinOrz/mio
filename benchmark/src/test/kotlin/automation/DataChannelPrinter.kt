@file:Suppress("NAME_SHADOWING")

package automation

import automation.DataChannelFunction.Direction
import com.meowool.sweekt.firstCharLowercase
import com.meowool.sweekt.takeIfEmpty
import com.meowool.sweekt.takeIfNotEmpty
import com.squareup.kotlinpoet.KModifier
import io.kotest.core.spec.style.StringSpec
import java.io.IOException
import java.nio.charset.Charset

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class DataChannelPrinter : StringSpec({
  Kind.Peek.name {
    printFunction<Byte>("get") {
      doc.add(
        "Gets a byte at the specified [index] of this channel, " +
          "and then moves the cursor to the right of the byte being returned, " +
          "that is: `cursor.moveRight()`."
      )
      doc.example(
        title = "Note that the behavior of this function is almost the same as [peek], " +
          "the only difference is that this function overloaded Kotlin's operator, for example",
        code = """
          A B C D E
          -----------------------
          channel.get(2)  ->  C
          channel[2]      ->  C
        """
      )
      builder.addModifiers(KModifier.OPERATOR)
      addIndexParam(isRead = true, withThrows = true, defaultValue = false)
      addUnderflow()
      returnBody("peek(index)")
    }
    commonPrint(Kind.Peek)
  }

  Kind.Pop.name {
    commonPrint(Kind.Pop)
  }

  Kind.Drop.name {
    commonPrint(Kind.Drop)
  }

  "Amend" {
    amendPrint<Byte>()
    amendPrint<Boolean> { specifiedIndex ->
      val index = if (specifiedIndex) "index,·" else ""
      returnBody("$name(${index}if·(boolean)·1·else·0)")
    }
    amendPrint<Short>()
    amendPrint<Char>()
    amendPrint<Int>()
    amendPrint<Long>()
    amendPrint<Float>()
    amendPrint<Double>()
  }
}) {
  private companion object {
    const val LineNote = "A line is considered to be terminated by the \"Unix\" line feed character `\\n`, " +
      "or the \"Windows\" carriage return character + line feed character `\\r\\n`, " +
      "or the \"macOS\" carriage return character `\\r`, or by reaching the end of channel."

    fun theIndex(specifiedIndex: Boolean) = "the " + when {
      specifiedIndex -> "specified [index]"
      else -> "current [cursor] index"
    }

    fun atTheIndex(specifiedIndex: Boolean) = "at " + theIndex(specifiedIndex)

    fun DataChannelFunction.addCharsetParam() {
      param<Charset>(
        name = "charset",
        comment = "The charset to use for decoding string (default is utf-8).",
        defaultValue = "Charsets.UTF_8"
      )
    }

    fun DataChannelFunction.addIndexParam(
      isRead: Boolean,
      withThrows: Boolean,
      defaultValue: Boolean = true
    ) {
      val starting = if (isReturnOneByteCount) "" else " starting"
      val commentEnd = if (defaultValue) " (default is current [cursor] index)." else "."
      param<Long>(
        name = "index",
        comment = when {
          isRead -> "The$starting index of the data to be ${kind}ed"
          else -> "The$starting target index to $kind data to"
        } + commentEnd,
        defaultValue = "cursor.index".takeIf { defaultValue }
      )
      if (isReturnPrimitive) doc.see("$returnsName.SIZE_BYTES")
      if (withThrows) {
        throws<IndexOutOfBoundsException>("If the specified [index] is less than zero or exceeds the [size] range of this channel.")
      }
    }

    fun DataChannelFunction.addUnderflow(elseCount: String = "[count]", specifiedIndex: Boolean = true) {
      val underflowException = when (isReturnOneByteCount) {
        true -> "If there is no data ${atTheIndex(specifiedIndex)} of this channel"
        false -> "If the $leftOrRight side of ${theIndex(specifiedIndex)} (inclusive) of this channel " +
          "is less than ${returnsByteCountStatement(elseCount)}"
      }

      if (isNullable) {
        doc.add(
          if (isReturnPrimitive) {
            "Note that it returns a boxed ${valueRef()} object, " +
              "${underflowException.firstCharLowercase()}, it returns `null`."
          } else {
            "Note that ${underflowException.firstCharLowercase()}, it returns `null`."
          }
        )
      } else {
        throws("ChannelUnderflowException", "$underflowException.")
      }
    }

    fun DataChannelFunction.throwClosed() {
      throws("ClosedChannelException", "If this channel is closed.")
    }

    fun DataChannelFunction.throwIO(other: Boolean = false) {
      val an = if (other) "some other" else "an"
      throws<IOException>("If $an I/O error occurs.")
    }

    fun commonPrint(funcKind: Kind) = with(funcKind.subtitle) {
      val docStart = "${funcKind.name}s${funcKind.descriptionWrapper} a "
      fun DataChannelFunction.addSees() {
        if (funcKind.isPop) {
          doc.see(name.replace(Kind.Pop.subtitle, Kind.Peek.subtitle))
          doc.see(
            name.replace(Kind.Pop.subtitle, Kind.Drop.subtitle)
              .removeSuffix("OrNull")
              .replace("String", "Bytes")
              .replace("LineBytes", "Line")
              .replace("RangeBytes", "Range")
              .replace("AllBytes", "All")
          )
        }
      }

      fun DataChannelFunction.leftOtherWords() =
        "In other words, first moves the cursor to the left by ${returnsByteCountStatement("[count]")}, " +
          "that is: `${movePlusOrMinus(returnsByteCountStr("count"))}`, " +
          "and then ${funcKind.description} the ${valueRef(elseCount = "count")} on the right (not include the byte where the current cursor)."

      val outputOrDeleted = when (funcKind) {
        Kind.Peek -> "Output"
        Kind.Pop -> "Deleted & Output"
        Kind.Drop -> "Deleted"
      }

      ////////////////////////////////////////////////////
      ////                  Primitive                 ////
      ////////////////////////////////////////////////////

      fun DataChannelFunction.primitive() {
        if (isReturnByte) returnsNameTitle = ""
        val valueRef = valueRef()
        val moveCall = movePlusOrMinus(returnsByteCountStr("count"))
        doc.add(
          "$docStart$valueRef " + when (direction) {
            Direction.Left -> "from the left side of the specified [index] of this channel. ${leftOtherWords()}"

            Direction.Right -> "at the specified [index] of this channel" + buildString {
              if (funcKind.isPeek) {
                append(", and then moves the cursor to the right of the $valueRef being ${kind}ed, ")
                append("that is: `$moveCall`.")
              } else {
                append('.')
              }
            }
          }
        )

        if (direction == Direction.Left) {
          returnBody(
            "$moveCall.runTemporarily·{·${
              name.replace(
                "Left",
                ""
              )
            }(index·-·${returnsByteCount() - 1})·}"
          )
        }

        addIndexParam(isRead = true, withThrows = true)
        addUnderflow()
        addSees()
        throwClosed()
        throwIO(other = true)

        if (funcKind.isDrop) {
          returns<Boolean>("Returns `true` if the $valueRef is successfully dropped, otherwise returns `false`.")
        }
      }

      printFunctions<Byte>(allowNullable = !funcKind.isDrop) { primitive() }
      printFunctions<Boolean>(allowNullable = !funcKind.isDrop) {
        primitive()
        val byteFunc = name.replace("Boolean", "")
        returnBody(
          if (funcKind.isDrop) {
            "$byteFunc()"
          } else when (isNullable) {
            true -> "$byteFunc()?.equals(1.toByte())"
            false -> "$byteFunc() == 1.toByte()"
          }
        )
      }
      printFunctions<Short>(allowNullable = !funcKind.isDrop) { primitive() }
      printFunctions<Char>(allowNullable = !funcKind.isDrop) { primitive() }
      printFunctions<Int>(allowNullable = !funcKind.isDrop) { primitive() }
      printFunctions<Long>(allowNullable = !funcKind.isDrop) { primitive() }
      printFunctions<Float>(allowNullable = !funcKind.isDrop) { primitive() }
      printFunctions<Double>(allowNullable = !funcKind.isDrop) { primitive() }

      ////////////////////////////////////////////////////
      ////              String and Bytes              ////
      ////////////////////////////////////////////////////

      fun DataChannelFunction.countable(specifiedIndex: Boolean) {
        val valueRef = valueRef()
        val moveCall = movePlusOrMinus(returnsByteCountStr("count"))

        doc.add(
          "$docStart$valueRef starting ${atTheIndex(specifiedIndex)} (inclusive) and consists of [count] bytes to the $leftOrRight" + buildString {
            if (direction == Direction.Left) {
              append('.')
              appendLine()
              append(leftOtherWords())
            } else {
              if (funcKind.isPeek) {
                append(", and then moves the cursor to the $leftOrRight of bytes being ${kind}ed, ")
                append("that is: `$moveCall`.")
              } else {
                append('.')
              }
            }
          }
        )

        if (specifiedIndex) {
          addIndexParam(isRead = true, withThrows = true, defaultValue = false)
          if (direction == Direction.Left) {
            returnBody("cursor.moveTo(index·-·count).runTemporarily·{·${name.replace("Left", "")}(index·-·count·+·1,·count)·}")
          }
        } else {
          returnBody("$name(cursor.index, count)")
        }
        param<Int>("count", "The total byte count of the $returnsNameDesc.")
        addUnderflow(specifiedIndex = specifiedIndex)
        addSees()
        throwClosed()
        throwIO(other = true)

        if (funcKind.isDrop) {
          returns<Boolean>("Returns `true` if the $valueRef is successfully dropped, otherwise returns `false`.")
        }
      }

      arrayOf(true, false).forEach { specifiedIndex ->
        printFunctions<ByteArray>(allowNullable = !funcKind.isDrop) { countable(specifiedIndex) }

        if (funcKind.isDrop.not()) {
          val index = if (specifiedIndex) "index, " else ""
          printFunctions<String> {
            countable(specifiedIndex)
            addCharsetParam()
            returnBody(
              "${
                name.replace(
                  "String",
                  "Bytes"
                )
              }(${index}count)$nullSymbolOrEmpty.toString(charset)"
            )
          }
        }
      }

      ////////////////////////////////////////////////////
      ////                    Line                    ////
      ////////////////////////////////////////////////////

      fun DataChannelFunction.line(isString: Boolean) {
        val valueRef = valueRef()
        val moveCall = when (direction) {
          Direction.Left -> "moveToPreviousLine().moveToEndOfLine()"
          Direction.Right -> "moveToNextLine()"
        }
        val previousOrNextLine = when (direction) {
          Direction.Left -> "the end of the previous line"
          Direction.Right -> "the start of the next line"
        }
        val nullException = if (isNullable) ", returns `null` if this channel has no more bytes" else ""
        val returnedOrDeleted = when (funcKind) {
          Kind.Peek, Kind.Pop -> "returned"
          Kind.Drop -> "deleted"
        }

        doc.add(
          docStart + "line of $valueRef on the $leftOrRight at the specified [index] of this channel" + buildString {
            if (funcKind.isPeek) {
              append(", and then moves the cursor to $previousOrNextLine, ")
              append("that is: `$moveCall`.")
            } else {
              append('.')
              if (direction == Direction.Left) {
                append(
                  "\nIn other words, first moves the cursor to $previousOrNextLine, " +
                    "and then ${funcKind.description} the next line (not include the byte where the current cursor)."
                )
              }
            }
          } + "\n\n" +

            "$LineNote\n\n" +

            "Note that the result $returnedOrDeleted not include the terminator, in other words, " +
            "${funcKind.description} all bytes from the specified [index] (inclusive) to $previousOrNextLine terminator (exclusive)" +

            nullException
        )

        val reverseBytes = if (direction == Direction.Left) ".reverse()" else ""
        val toStringOrEmpty = if (isString) ".toString(charset)" else ""
        doc.consistentBehavior(
          """
            val bos = ByteArrayBuilder()
            while (true) {
              when (val byte = ${funcKind.subtitle}$leftTitleOrEmpty()) {
                // A terminator was encountered, the end of a line
                "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
                  // Move cursor to $previousOrNextLine
                  cursor.move$leftOrRightTitle()
                  break
                }
                else -> bos.append(byte)
              }
            }
            bos$reverseBytes.toByteArray()$toStringOrEmpty
          """
        )

        doc.example(
          "For example, the cursor is on the " + when (direction) {
            Direction.Left -> "`L`"
            Direction.Right -> "`F`"
          },
          when (direction) {
            Direction.Left -> """
              A B C D E F G
              H I J K L M N
                      ^
              ------------------
              $outputOrDeleted: H I J K L
            """
            Direction.Right -> """
              A B C D E F G
                        ^
              H I J K L M N
              ------------------
              $outputOrDeleted: F G
            """
          }
        )
        throws("ChannelEmptyException", "If this channel has no more bytes.")
        addIndexParam(isRead = true, withThrows = true)
        addUnderflow()
        addSees()
        throwClosed()
        throwIO(other = true)

        if (funcKind.isDrop) {
          returns<Boolean>("Returns `true` if the line is successfully dropped, otherwise returns `false`.")
        }
      }
      printFunctions<ByteArray>(allowNullable = !funcKind.isDrop) {
        returnsNameTitle = if (funcKind.isDrop) "Line" else "LineBytes"
        line(isString = false)
      }
      if (funcKind.isDrop.not()) {
        printFunctions<String> {
          returnsNameTitle = "Line"
          line(isString = true)
          addCharsetParam()
          val bytesName = buildString {
            append(name.removeSuffix("OrNull"))
            append("Bytes")
            if (isNullable) append("OrNull")
          }
          returnBody("$bytesName()$nullSymbolOrEmpty.toString(charset)")
        }
      }

      ////////////////////////////////////////////////////
      ////                 Remaining                  ////
      ////////////////////////////////////////////////////

      fun DataChannelFunction.commonRemaining(hasCharset: Boolean) {
        doc.add(
          docStart + "remaining ${valueRef()} to the right of the current cursor index in the channel" + if (funcKind.isPeek) {
            " and then moves the cursor to the right of bytes being peeked, that is: `cursor.moveTo(size)`."
          } else {
            '.'
          }
        )
        addSees()

        if (funcKind.isDrop) {
          returns<Boolean>("Returns `true` if the remaining bytes is successfully dropped, otherwise returns `false`.")
        } else {
          throws<OutOfMemoryError>(
            "If the required size by the remaining bytes cannot be allocated, " +
              "for example the remaining bytes is larger that `2 GB`."
          )
        }
        val charset = if (hasCharset) ".toString(charset)" else ""
        returnBody("${funcKind.subtitle}Bytes(remainingSize.toLegalInt(\"Remaining·bytes·size·too·large\"))$charset")
        throwClosed()
        throwIO(other = true)
      }

      printFunction<ByteArray>(
        funcKind.subtitle + "Remaining",
        nameWithReturnsTitle = funcKind.isDrop.not()
      ) { commonRemaining(false) }

      if (funcKind.isDrop.not()) {
        printFunction<String>(funcKind.subtitle + "Remaining") {
          commonRemaining(true)
          addCharsetParam()
        }
      }

      ////////////////////////////////////////////////////
      ////                   Range                    ////
      ////////////////////////////////////////////////////

      fun DataChannelFunction.commonRange() {
        doc.add(
          docStart + "${valueRef()} from a range of this channel starting at the [startIndex] and ending right before the [endIndex]" + if (funcKind.isPeek) {
            " and then moves the cursor to the [endIndex], that is: `cursor.moveTo(endIndex)`."
          } else {
            '.'
          }
        )

        param<Long>(
          name = "startIndex",
          comment = "The start index of range. (inclusive)",
        )
        param<Long>(
          name = "endIndex",
          comment = "The end index of range. (exclusive)",
        )
        throws<IndexOutOfBoundsException>("If [startIndex] is less than zero or [endIndex] is greater than the [size] of this channel.")
        throws<IllegalArgumentException>("If [startIndex] is greater than [endIndex].")
        addSees()

        if (funcKind.isDrop) {
          returns<Boolean>("Returns `true` if the specified range is successfully dropped, otherwise returns `false`.")
        } else {
          throws<OutOfMemoryError>(
            "If the required size by the bytes of the specified range cannot be allocated, " +
              "for example the bytes of the specified range is larger that `2 GB`."
          )
        }
        throwClosed()
        throwIO(other = true)
      }
      printFunction<ByteArray>(
        funcKind.subtitle + "Range",
        nameWithReturnsTitle = funcKind.isDrop.not()
      ) {
        commonRange()
        returnBody(
          """
            cursor.runTemporarily { 
              checkIndices(startIndex, endIndex)
              ${funcKind.subtitle}Bytes(index·=·startIndex,·count·=·(endIndex·-·startIndex).toLegalInt())
            }
          """
        )
      }

      if (funcKind.isDrop.not()) {
        printFunction<String>(funcKind.subtitle + "Range") {
          commonRange()
          addCharsetParam()
          returnBody(buildString {
            append(kind)
            append("Bytes")
            append("(startIndex, endIndex)${nullSymbolOrEmpty}.toString(charset)")
          })
        }
      }

      ////////////////////////////////////////////////////
      ////                    All                     ////
      ////////////////////////////////////////////////////

      fun DataChannelFunction.commonAll() {
        val exception = "If this channel has no more bytes"
        when (isNullable) {
          true -> doc.add("Note that ${exception.firstCharLowercase()}, the `null` is returned.")
          false -> throws("ChannelEmptyException", "$exception.")
        }
        addSees()

        if (funcKind.isDrop) {
          returns<Boolean>("Returns `true` if all bytes is successfully dropped, otherwise returns `false`.")
        } else {
          throws<OutOfMemoryError>(
            "If the required size by the all bytes of this channel cannot be allocated, " +
              "for example the channel size is larger that `2 GB`."
          )
        }
        throwClosed()
        throwIO(other = true)
      }

      printFunctions<ByteArray>(
        funcKind.subtitle + "All",
        nameWithReturnsTitle = funcKind.isDrop.not(),
        allowNullable = !funcKind.isDrop,
        allowDoubleDirection = false,
      ) {
        doc.add(docStart + "byte array of all bytes of this channel.")
        val code = when (funcKind) {
          Kind.Peek, Kind.Pop -> """
            val bos = ByteArrayBuilder()
            cursor.moveToFirst()
            while (true) {
              ${funcKind.subtitle}LineBytesOrNull()?.also {
                bos.append(it)
              } ?: break
            }
            bos.toByteArray()
          """
          Kind.Drop -> """
            cursor.moveToFirst()
            while (dropLine()) {}
          """
        }
        doc.consistentBehavior(
          when (isNullable) {
            true -> """
              when(isEmpty()) {
                true -> null
                else -> {
${code.replaceIndent("                  ")}
                }
              }
            """.trimIndent()
            else -> """
              throwIf(isEmpty()) { ChannelEmptyException() }
${code.replaceIndent("              ")}
            """
          }
        )
        commonAll()
      }
      if (funcKind.isDrop.not()) {
        printFunctions<String>(
          funcKind.subtitle + "All",
          allowNullable = !funcKind.isDrop,
          allowDoubleDirection = false,
          nameWithReturnsTitle = false,
        ) {
          doc.add(docStart + "string decoded from all bytes of this channel.")
          commonAll()
          addCharsetParam()
          returnBody(buildString {
            append(kind)
            append("Bytes")
            if (isNullable) append("OrNull")
            append("()${nullSymbolOrEmpty}.toString(charset)")
          })
        }
      }
    }

    inline fun <reified T> amendPrint(
      crossinline block: DataChannelFunction.(specifiedIndex: Boolean) -> Unit = {}
    ) = with("amend") {
      val primitive: DataChannelFunction.(Boolean) -> Unit = { specifiedIndex: Boolean ->
        val valueRef = valueRef(true)
        val atIndex = atTheIndex(specifiedIndex)
        doc.add(
          "Amends (writes) a $valueRef $atIndex of this channel, " +
            "and then moves the cursor to the $leftOrRight of the $valueRef being written, " +
            "that is: `${movePlusOrMinus(returnsByteCountStr())}`."
        )
        doc.add(
          "Note that if there is no data $atIndex or the channel [size] range is exceeded, " +
            "empty bytes will be used to grow the channel [size], " +
            "and then fills the given $valueRef."
        )
        if (isReturnByte) doc.example(
          code = """
            - At first, the index is `2`:
              A B C D E F G
                  ^
                  
            - Update the byte at the index to `X`:
              A B X D E F G
                  ^
            
            
          """.trimIndent() + when (direction) {
            Direction.Left -> """
              - Then move the cursor to the left:
                A B X D E F G
                  ^ (current cursor)
            """
            Direction.Right -> """
              - Then move the cursor to the right:
                A B X D E F G
                      ^ (current cursor)
            """
          }.trimIndent()
        )
        if (specifiedIndex) addIndexParam(isRead = true, withThrows = true, defaultValue = false)
        param<T>(returnsNameDesc, "The new data to be used to replace old data.")
        returns<Unit>()
      }
      arrayOf(false, true).forEach { specifiedIndex ->
        printFunction<T> {
          primitive(specifiedIndex)
          block(specifiedIndex)
          if (!specifiedIndex) returnBody("${name}(cursor.index,·$returnsNameDesc)")
        }
      }
      arrayOf(false, true).forEach { specifiedIndex ->
        printFunction<T>(direction = Direction.Left) {
          primitive(specifiedIndex)
          block(specifiedIndex)
          if (!specifiedIndex) returnBody("${name}(cursor.index,·$returnsNameDesc)")
        }
      }
    }

    enum class Kind(private val _description: String) {
      Peek(""), Pop("returns and deletes"), Drop("only deletes without returning");

      val isPeek get() = this == Peek
      val isPop get() = this == Pop
      val isDrop get() = this == Drop
      val subtitle get() = name.firstCharLowercase()

      val description get() = _description.takeIfNotEmpty() ?: "peek"
      val descriptionWrapper get() = _description.takeIfEmpty() ?: " ($_description)"
    }
  }
}