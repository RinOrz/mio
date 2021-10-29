@file:Suppress("TestFunctionName")

package automation

import com.meowool.sweekt.firstCharLowercase
import com.meowool.sweekt.firstCharTitlecase
import com.meowool.sweekt.ifNull
import com.meowool.sweekt.takeIfNotEmpty
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestContext
import kotlin.reflect.KClass

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class DataChannelPrinter2 : FreeSpec({
  // pop, peek, drop, remove, set

  val peeksPrebuilt = """
    /**
     * Peeks a string from a range of this channel starting at the [startIndex] and ending right
     * before the [endIndex].
     *
     * @param startIndex The start index (inclusive, default is current [cursor] index).
     * @param endIndex The end index (exclusive, default is channel size).
     * @param charset The charset to use for decoding string (default is utf-8).
     *
     * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
     * than the [size] of this channel.
     * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
     */
    @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
    fun peekRange(
      startIndex: Long = cursor.index,
      endIndex: Long = size,
      charset: Charset = Charsets.UTF_8,
    ): String = peekRangeBytes(startIndex, endIndex).toString(charset)
  
    /**
     * Peeks a byte array from a range of this channel starting at the [startIndex] and ending right
     * before the [endIndex].
     *
     * @param startIndex The start index (inclusive, default is current [cursor] index).
     * @param endIndex The end index (exclusive, default is channel size).
     *
     * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
     * than the [size] of this channel.
     * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
     */
    @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
    fun peekRangeBytes(startIndex: Long = cursor.index, endIndex: Long = size): ByteArray
  
    /**
     * Peeks a string decoded from all bytes of this channel.
     *
     * @param charset The charset to use for decoding string (default is utf-8).
     *
     * @throws ChannelEmptyException If this channel has no more bytes.
     */
    @Throws(ChannelEmptyException::class)
    fun peekAll(charset: Charset = Charsets.UTF_8): String = peekAllBytes().toString(charset)
  
    /**
     * Peeks a string decoded from all bytes of this channel, or `null` if there are no more bytes in
     * this channel.
     *
     * @param charset The charset to use for decoding string (default is utf-8).
     */
    fun peekAllOrNull(charset: Charset = Charsets.UTF_8): String? =
      peekAllBytesOrNull()?.toString(charset)
  
    /**
     * Peeks all bytes of this channel.
     *
     * This function is consistent with the behavior of the following expression, but has better
     * performance:
     * ```
     * throwIf(isEmpty()) { ChannelEmptyException() }
     * val bos = ByteArrayBuilder()
     * cursor.moveToFirst()
     * while (true) {
     *   peekOrNull()?.also {
     *     bos.append(it)
     *   } ?: break
     * }
     * bos.toByteArray()
     * ```
     *
     * @throws ChannelEmptyException If this channel has no more bytes.
     */
    @Throws(ChannelEmptyException::class)
    fun peekAllBytes(): ByteArray
  
    /**
     * Peeks all bytes of this channel, or `null` if there are no more bytes in this channel.
     *
     * This function is consistent with the behavior of the following expression, but has better
     * performance:
     * ```
     * when(isEmpty()) {
     *   true -> null
     *   else -> {
     *     val bos = ByteArrayBuilder()
     *     cursor.moveToFirst()
     *     while (true) {
     *       peekOrNull()?.also { bos.append(it) } ?: break
     *     }
     *     bos.toByteArray()
     *   }
     * }
     * ```
     */
    fun peekAllBytesOrNull(): ByteArray?
  """.trimIndent()

  "peek" {
    println(
      """
        /**
         * Returns a byte at the specified [index] of this channel.
         *
         * Note that the behavior of this function is almost the same as [peek], the only difference is
         * that this function does not move the [cursor].
         *
         * For example:
         * ```
         * A B C D E
         * -----------------------
         * channel.get(2)  ->  C
         * channel[2]      ->  C
         * ```
         *
         * @param index The index of the data to be returned (default is current [cursor] index).
         *
         * @throws IndexOutOfBoundsException If the specified [index] is less than zero or exceeds the
         * [size] range of this channel.
         * @throws ChannelUnderflowException If there is no data at the specified [index] of this channel.
         */
        @Throws(
          IndexOutOfBoundsException::class,
          ChannelUnderflowException::class
        )
        operator fun get(index: Long): Byte = cursor.runTemporarily { peek(index) }
      """.trimIndent()
    )

    val doc = KDoc(
      consistentBehavior = {
        when (isLeftChannel) {
          true -> """
            cursor.moveTo(index - $byteCount).runTemporarily {
              moveRight()
              peek()
            }
          """.replaceIndent("").replace("moveTo(index - 1)", "moveLeft()")

          else -> ""
        }
      },
      codeComment = {
        if (isLeftChannel) """
          More simply, refer to the following ${if (realName == "peekLeft") "" else "[peekLeft]"} process:
          ```
          A B C D E F G
                      ^
          -----------------
          A B C D E F G
                    ^
          -----------------
          Output: G
          ```
        """.replaceIndent("")
        else ""
      }) {
      when (isLeftChannel) {
        true -> "Peeks $aValueRef from the left side of the specified [index] of this channel.\n" +
          "In other words, first moves the cursor to the left by ${byteCountStatement("[count]")}, " +
          "that is: `cursor.moveTo(index - $byteCount)`, and then returns the right " +
          "${theValueRef.removePrefix("the ")} (not include the byte where the current cursor)."

        else -> "Peeks $aValueRef at the specified [index] of this channel, and then moves the " +
          "cursor to the right of $theValueRef being $passiveVoice, that is: `cursor.moveTo(index + $byteCount)`."
      }
    }

    printFunction<Byte>(doc)
    printFunction<Boolean>(doc) {
      if (isNullable) {
        "return ${realName.replace("Boolean", "")}()?.equals(1.toByte())"
      } else {
        "return ${realName.replace("Boolean", "")}() == 1.toByte()"
      }
    }
    printFunction<Short>(doc)
    printFunction<Char>(doc)
    printFunction<Int>(doc)
    printFunction<Long>(doc)
    printFunction<Float>(doc)
    printFunction<Double>(doc)

    val arrayDoc = KDoc(consistentBehavior = {
      if (isLeftChannel) {
        """
          cursor.moveLeft(count - 1).runTemporarily {
            moveRight()
            ByteArray(count) { peek() }
          }
        """.replaceIndent("")
      } else "ByteArray(count) { peek() }"
    }) {
      when (isLeftChannel) {
        true -> "Peeks $aValueRef from the left side of the specified [index] of this channel.\n" +
          "In other words, first moves the cursor to the left by ${byteCountStatement("[count]")}, " +
          "that is: `cursor.moveLeft(count - 1)`, and then returns the right " +
          "${theValueRef.removePrefix("the ")} (not include the byte where the current cursor)."

        else -> "Peeks $aValueRef starting at the specified [index] (inclusive) and consists of " +
          "[count] bytes to the $leftOrRight, and then moves the cursor to the right of bytes being " +
          "$passiveVoice, that is: `cursor.moveRight(count - 1)`."
      }
    }
    val arrayParams: List<FunctionInfo.() -> ParameterSpec.Builder> = listOf {
      ParameterSpec.builder("count", INT).addKdoc("The total byte count of the $typeName.")
    }
    val charset = ParameterSpec.builder("charset", TopLevelClass("Charset"))
      .defaultValue("Charsets.UTF_8")
      .addKdoc("The charset to use for decoding string (default is utf-8).")

    printFunction<ByteArray>(
      arrayDoc,
      requiredParams = arrayParams,
      requiredByteCount = { "[count]" }
    )
    printFunction<String>(
      arrayDoc,
      requiredParams = arrayParams,
      optionalParams = listOf { charset },
      requiredByteCount = { "[count]" },
      codeBody = {
        "return ${realName.replace("String", "Bytes")}(count)$nullableSymbolOrEmpty" +
          ".toString(charset)"
      }
    )

    val lineKdoc = KDoc(
      customNote = true,
      hasUnderflowException = false,
      consistentBehavior = {
        val reverseBytes = if (isLeftChannel) ".reverse()" else ""
        val comment = if (isLeftChannel) "Move cursor to end of the previous line" else "Move cursor to start of the next line"
        """
          val bos = ByteArrayBuilder()
          while (true) {
            when (val byte = $name$leftOrEmpty()) {
              "\n".toByte(), "\r\n".toByte(), "\r".toByte() -> {
                // $comment
                cursor.move${leftOrRight.firstCharTitlecase()}()
                break
              }
              else -> bos.append(byte)
            }
          }
          bos$reverseBytes.toByteArray()
        """.replaceIndent("")
      },
      codeComment = {
        when (isLeftChannel) {
          true -> """
            For example, the cursor is on the `L`:
            ```
            A B C D E F G
            H I J K L M N
                    ^
            ------------------
            Output: H I J K L
            ```
          """.replaceIndent("")
          else -> """
            For example, the cursor is on the `F`:
            ```
            A B C D E F G
                      ^
            H I J K L M N
            ------------------
            Output: F G
            ```
          """.replaceIndent("")
        }
      },
      throws = { mapOf("ChannelEmptyException" to "If this channel has no more bytes.") }
    ) {
      val moveCall = if (isLeftChannel) "moveToPreviousLine().moveToEndOfLine()" else "moveToNextLine()"
      val nullException = if (isNullable) ", returns `null` if this channel has no more bytes" else ""
      val note = when (isLeftChannel) {
        true -> "returns all bytes from the specified [index] (inclusive) to the end of the " +
          "previous line terminator (exclusive)$nullException."

        false -> "returns all bytes starting at the specified [index] (inclusive) and up to the " +
          "line terminator (exclusive)$nullException."
      }
      val previousOrNextLine = if (isLeftChannel) "end of the previous line" else "start of the next line"
      "Peeks a line of $valueRef on the $leftOrRight at the specified [index] of this channel, and then moves the " +
        "cursor to the $previousOrNextLine, that is: `cursor.$moveCall`.\n\n" +

        "A line is considered to be terminated by the \"Unix\" line feed character `\\n`, or the " +
        "\"Windows\" carriage return character + line feed character `\\r\\n`, or the \"macOS\" " +
        "carriage return character `\\r`, or by reaching the end of channel.\n\n" +
        "Note that the result returned not include the terminator, in other words, " +
        note
    }

    printFunction<ByteArray>(lineKdoc, typeNameTitle = "LineBytes")

    printFunction<String>(
      lineKdoc.copy(consistentBehavior = {
        lineKdoc.consistentBehavior(this)
          .replace("toByteArray()", "toByteArray().toString(charset)")
      }),
      typeNameTitle = "Line",
      optionalParams = listOf { charset },
    )

    println(peeksPrebuilt)
  }

  "pop and drop" - {
    "pop:popped" {
      val doc = KDoc(
        link = {
          listOf(
            "peek${realName.removePrefix("pop")}",
            "drop${realName.removePrefix("pop").removeSuffix("OrNull")}"
          )
        },
        consistentBehavior = {
          """
            cursor.runTemporarily { 
              peek${realName.removePrefix("pop")}(index)
            }.apply {
              drop${realName.removePrefix("pop").removeSuffix("OrNull")}(index)
            }
          """.replaceIndent("")
            .replace("dropString", "dropBytes")
            .replace("dropLeftString", "dropLeftBytes")
            .replace("dropLineBytes", "dropLine")
            .replace("dropLeftLineBytes", "dropLeftLine")
        }
      ) {
        val otherWords = if (isByte.not()) "\nIn other words, " + when (isLeftChannel) {
          true -> "moves the cursor ${byteCountStatement("[count]")} to the left, " +
            "and then deletes the bytes on the right and returns."
          false -> "deletes the ${byteCountStatement("[count]")} on the right at the " +
            "specified [index] (inclusive) and returns."
        } else ""
        "Pops (deletes and returns) $aValueRef at the specified [index] of this channel.$otherWords"
      }

      printFunction<Byte>(doc)
      printFunction<Boolean>(doc) {
        if (isNullable) {
          "return ${realName.replace("Boolean", "")}()?.equals(1.toByte())"
        } else {
          "return ${realName.replace("Boolean", "")}() == 1.toByte()"
        }
      }
      printFunction<Short>(doc)
      printFunction<Char>(doc)
      printFunction<Int>(doc)
      printFunction<Long>(doc)
      printFunction<Float>(doc)
      printFunction<Double>(doc)

      val arrayDoc = KDoc(consistentBehavior = {
        doc.consistentBehavior(this).replace("(index)", "(count, index)")
      })
      val arrayParams: List<FunctionInfo.() -> ParameterSpec.Builder> = listOf {
        ParameterSpec.builder("count", INT).addKdoc("The total byte count of the $typeName.")
      }
      val charset = ParameterSpec.builder("charset", TopLevelClass("Charset"))
        .defaultValue("Charsets.UTF_8")
        .addKdoc("The charset to use for decoding string (default is utf-8).")

      printFunction<ByteArray>(
        arrayDoc,
        requiredParams = arrayParams,
        requiredByteCount = { "[count]" }
      )
      printFunction<String>(
        arrayDoc,
        requiredParams = arrayParams,
        optionalParams = listOf { charset },
        requiredByteCount = { "[count]" },
        codeBody = {
          "return ${realName.replace("String", "Bytes")}(count)$nullableSymbolOrEmpty" +
            ".toString(charset)"
        }
      )

      val lineKdoc = KDoc(
        customNote = true,
        hasUnderflowException = false,
        consistentBehavior = doc.consistentBehavior,
        codeComment = {
          when (isLeftChannel) {
            true -> """
              For example, the cursor is on the `L`:
              ```
              A B C D E F G
              H I J K L M N
                      ^
              -----------------------------
              Output and deleted: H I J K L
              ```
            """.replaceIndent("")
            else -> """
              For example, the cursor is on the `D`:
              ```
              A B C D E F G
                    ^
              H I J K L M N
              -----------------------------
              Output and deleted: D E F G
            ```
          """.replaceIndent("")
          }
        },
        throws = { mapOf("ChannelEmptyException" to "If this channel has no more bytes.") }
      ) {
        val nullException = if (isNullable) ", returns `null` if this channel has no more bytes" else ""
        val note = when (isLeftChannel) {
          true -> "deletes and returns all bytes from the specified [index] (inclusive) to the end of the " +
            "previous line terminator (exclusive)$nullException."

          false -> "deletes and returns all bytes starting at the specified [index] (inclusive) and up to the " +
            "line terminator (exclusive)$nullException."
        }
        "Pops (deletes and returns) a line of $valueRef on the $leftOrRight at the specified [index] of this channel.\n\n" +

          "A line is considered to be terminated by the \"Unix\" line feed character `\\n`, or the " +
          "\"Windows\" carriage return character + line feed character `\\r\\n`, or the \"macOS\" " +
          "carriage return character `\\r`, or by reaching the end of channel.\n\n" +
          "Note that the result returned not include the terminator, in other words, " +
          note
      }

      printFunction<ByteArray>(lineKdoc, typeNameTitle = "LineBytes")

      printFunction<String>(
        lineKdoc.copy(consistentBehavior = {
          lineKdoc.consistentBehavior(this)
            .replace("toByteArray()", "toByteArray().toString(charset)")
        }),
        typeNameTitle = "Line",
        optionalParams = listOf { charset },
      )

      println(peeksPrebuilt.replace("Peeks", "Pops").replace("peek", "pop"))
    }
    "drop:dropped" {
      val title = "Drops (only deletes without returning)"

      val doc = KDoc(returns = { "Boolean" to "Returns `true` if $theValueRef is successfully dropped, otherwise returns `false`." }) {
        val otherWords = if (isByte.not()) "\nIn other words, " + when (isLeftChannel) {
          true -> "moves the cursor ${byteCountStatement("[count]")} to the left, " +
            "and then deletes the bytes on the right."
          false -> "deletes the ${byteCountStatement("[count]")} on the right at the " +
            "specified [index] (inclusive)."
        } else ""
        "$title $aValueRef at the specified [index] of this channel.$otherWords"
      }

      printFunction<Byte>(doc, nullable = false)
      printFunction<Boolean>(doc, nullable = false) {
        "return ${realName.replace("Boolean", "")}()"
      }
      printFunction<Short>(doc, nullable = false)
      printFunction<Char>(doc, nullable = false)
      printFunction<Int>(doc, nullable = false)
      printFunction<Long>(doc, nullable = false)
      printFunction<Float>(doc, nullable = false)
      printFunction<Double>(doc, nullable = false)

      val arrayParams: List<FunctionInfo.() -> ParameterSpec.Builder> = listOf {
        ParameterSpec.builder("count", INT).addKdoc("The total byte count of the $typeName.")
      }

      printFunction<ByteArray>(
        doc,
        requiredParams = arrayParams,
        requiredByteCount = { "[count]" },
        nullable = false
      )

      val lineKdoc = KDoc(
        customNote = true,
        hasUnderflowException = false,
        returns = doc.returns,
        codeComment = {
          when (isLeftChannel) {
            true -> """
              For example, the cursor is on the `L`:
              ```
              A B C D E F G
              H I J K L M N
                      ^
              -------------------
              Deleted: H I J K L
              ```
            """.replaceIndent("")
            else -> """
              For example, the cursor is on the `D`:
              ```
              A B C D E F G
                    ^
              H I J K L M N
              -------------------
              Deleted: D E F G
            ```
          """.replaceIndent("")
          }
        },
      ) {
        val note = when (isLeftChannel) {
          true -> "deletes all bytes from the specified [index] (inclusive) to the end of the " +
            "previous line terminator (exclusive)."

          false -> "deletes all bytes starting at the specified [index] (inclusive) and up to the " +
            "line terminator (exclusive)."
        }
        "$title a line of $valueRef on the $leftOrRight at the specified [index] of this channel.\n\n" +

          "A line is considered to be terminated by the \"Unix\" line feed character `\\n`, or the " +
          "\"Windows\" carriage return character + line feed character `\\r\\n`, or the \"macOS\" " +
          "carriage return character `\\r`, or by reaching the end of channel.\n\n" +
          "Note that the result returned not include the terminator, in other words, " +
          note
      }

      printFunction<ByteArray>(lineKdoc, typeNameTitle = "Line", nullable = false)

      println(
        """
          /**
           * Drops some bytes from a range of this channel starting at the [startIndex] and ending right
           * before the [endIndex].
           *
           * @return Returns `true` if the specified range is successfully dropped, otherwise 
           * returns `false`.
           *
           * @param startIndex The start index (inclusive, default is current [cursor] index).
           * @param endIndex The end index (exclusive, default is channel size).
           *
           * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater
           * than the [size] of this channel.
           * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
           */
          @Throws(IndexOutOfBoundsException::class, IllegalArgumentException::class)
          fun dropRange(
            startIndex: Long = cursor.index,
            endIndex: Long = size,
          ): Boolean
        
          /**
           * Drops all bytes of this channel.
           *
           * This function is consistent with the behavior of the following expression, but has better
           * performance:
           * ```
           * cursor.moveToFirst()
           * while (dropLine()) {}
           * ```
           *
           * @return Returns `true` if all bytes is successfully dropped, otherwise returns `false`.
           */
          fun dropAll(): Boolean
        """.trimIndent()
      )
    }
  }

  "push" {
    val doc = KDoc {
      "Pushes (inserts) $aValueArgRef to the specified [index], " +
        "and then moves the cursor to $leftOrRight $theValueArgRef being pushed, that is: " +
        "`cursor.moveTo(index $plusOrMinus $byteCount)`."
    }

  }
}) {
  companion object {
    private val THROWS = TopLevelClass("Throws")
    private val LONG = TopLevelClass("Long")
    val INT = TopLevelClass("Int")

    private fun Int.toWord() = when (this) {
      2 -> "two"
      4 -> "four"
      8 -> "eight"
      else -> this.toString()
    }

    private data class FunctionInfo(
      /** peek, pop, drop, etc... */
      val name: String,
      val type: KClass<*>,
      val passiveVoice: String,
      val isLeftChannel: Boolean,
      val isNullable: Boolean,
      val typeRawName: String = type.simpleName!!,
      val typeNameTitle: String = typeRawName.replace("ByteArray", "Bytes")
    ) {
      val nameTitle = name.firstCharTitlecase()
      val isByte = type == Byte::class
      val isPrimitive = when (type) {
        Byte::class, Short::class, Int::class, Long::class, Float::class, Double::class, Char::class -> true
        else -> false
      }
      val typeName = typeRawName.firstCharLowercase().replace("byteArray", "byte array")
      val byteCount = when (type) {
        Boolean::class, Byte::class -> Byte.SIZE_BYTES
        Short::class -> Short.SIZE_BYTES
        Int::class -> Int.SIZE_BYTES
        Long::class -> Long.SIZE_BYTES
        Float::class -> Float.SIZE_BYTES
        Double::class -> Double.SIZE_BYTES
        Char::class -> Char.SIZE_BYTES
        else -> Int.MAX_VALUE
      }
      val byteCountWord = when (byteCount) {
        1 -> "one"
        2 -> "two"
        4 -> "four"
        8 -> "eight"
        else -> byteCount.toString()
      }

      fun byteCount(requiredByteCount: String) = when (byteCount) {
        Int.MAX_VALUE -> requiredByteCount
        else -> byteCount
      }

      fun byteCountStatement(requiredByteCount: String) = when (byteCount) {
        1 -> "one byte"
        2, 4, 8 -> "$byteCountWord bytes"
        else -> "$requiredByteCount bytes"
      }

      val nullableSymbolOrEmpty = when (isNullable) {
        true -> "?"
        else -> ""
      }

      /** a [`something`] */
      val aValueArgRef = when (isPrimitive) {
        true -> when (isByte) {
          true -> "a [byte]"
          false -> "a [$typeName] value ($byteCountWord bytes)".replace("one bytes", "one byte")
        }
        false -> "a [$typeName]"
      }.replace("a [int]", "an [int]").replace("byte array", "bytes")

      /** [`something`] */
      val valueArgRef = aValueArgRef.removePrefix("an ").removePrefix("a ")

      /** the [`something`] */
      val theValueArgRef = "the $valueArgRef"

      /** a something */
      val aValueRef = aValueArgRef.replace("[", "").replace("]", "")

      /** something */
      val valueRef = valueArgRef.replace("[", "").replace("]", "")

      /** the something */
      val theValueRef = "the $valueRef"


      val leftOrEmpty = when (isLeftChannel) {
        true -> "Left"
        false -> ""
      }

      val leftOrRight = when (isLeftChannel) {
        true -> "left"
        false -> "right"
      }

      val plusOrMinus = when (isLeftChannel) {
        true -> "-"
        false -> "+"
      }

      val realName = buildString {
        append(name)
        if (isLeftChannel) append("Left")
        if (isByte.not()) append(typeNameTitle)
        if (isNullable) append("OrNull")
      }

      fun underflowException(requiredByteCount: String) = when (isByte) {
        true -> "If there is no data at the specified [index] of this channel"
        false -> "If the $leftOrRight side of the specified [index] (inclusive) of this channel is less than $requiredByteCount bytes"
      }.replace("one bytes", "one byte")
    }

    private data class KDoc(
      var customNote: Boolean = false,
      var hasUnderflowException: Boolean = true,
      var hasOverflowException: Boolean = false,
      var link: FunctionInfo.() -> List<String> = { emptyList() },
      val consistentBehavior: FunctionInfo.() -> String = { "" },
      val throws: FunctionInfo.() -> Map<String, String> = { emptyMap() },
      val returns: FunctionInfo.() -> Pair<String, String>? = { null },
      val codeComment: FunctionInfo.() -> String = { "" },
      val content: FunctionInfo.() -> String = { "" },
    )

    private fun TopLevelClass(name: String) = ClassName("", name)

    private fun String.simpleMove() =
      this.replace("moveTo(index + 1)", "moveRight()").replace("moveTo(index - 1)", "moveLeft()")

    private inline fun <reified T> TestContext.printFunction(
      kDoc: KDoc,
      name: String = testCase.name.testName.substringBefore(':'),
      typeNameTitle: String? = null,
      passiveVoice: String = testCase.name.testName.substringAfter(':', "")
        .takeIfNotEmpty()
        .ifNull { name + "ed" },
      requiredParams: List<FunctionInfo.() -> ParameterSpec.Builder> = emptyList(),
      optionalParams: List<FunctionInfo.() -> ParameterSpec.Builder> = emptyList(),
      extraIOOB: List<String> = emptyList(),
      nullable: Boolean = true,
      noinline requiredByteCount: FunctionInfo.() -> String = { byteCountWord },
      noinline codeBody: FunctionInfo.() -> String = { "" }
    ) = printFunction(
      kDoc,
      name,
      typeNameTitle,
      T::class,
      passiveVoice,
      requiredParams,
      optionalParams,
      extraIOOB,
      nullable,
      requiredByteCount,
      codeBody
    )

    private fun printFunction(
      kDoc: KDoc,
      name: String,
      typeNameTitle: String?,
      type: KClass<*>,
      passiveVoice: String,
      requiredParams: List<FunctionInfo.() -> ParameterSpec.Builder>,
      optionalParams: List<FunctionInfo.() -> ParameterSpec.Builder>,
      extraIOOB: List<String>,
      nullable: Boolean,
      requiredByteCount: FunctionInfo.() -> String,
      codeBody: FunctionInfo.() -> String
    ) {
      listOf(false, true).forEach { isLeftChannel ->
        listOf(false, true).takeIf { nullable }.ifNull { listOf(false) }.forEach { isNullable ->
          val info = if (typeNameTitle == null) {
            FunctionInfo(name, type, passiveVoice, isLeftChannel, isNullable)
          } else {
            FunctionInfo(
              name,
              type,
              passiveVoice,
              isLeftChannel,
              isNullable,
              typeNameTitle = typeNameTitle
            )
          }
          val link = if (info.isPrimitive) {
            listOf("${info.typeRawName}.SIZE_BYTES") + kDoc.link(info)
          } else kDoc.link(info)
          val underflowException = info.underflowException(requiredByteCount(info))
          val paramKDocs = mutableMapOf<String, CodeBlock>()

          fun List<FunctionInfo.() -> ParameterSpec.Builder>.buildParamSpecs() = map {
            val raw = info.it().build()
            paramKDocs[raw.name] = raw.kdoc
            ParameterSpec.builder(raw.name, raw.type, raw.modifiers)
              .defaultValue(raw.defaultValue)
              .build()
          }

          val requiredParamSpecs = requiredParams.buildParamSpecs()
          val optionalParamSpecs = optionalParams.buildParamSpecs()

          val func = FunSpec.builder(info.realName)
            .addKdoc(kDoc.content(info).simpleMove().wrapLine())
            .apply {
              kDoc.consistentBehavior(info).simpleMove().takeIfNotEmpty()?.also {
                addKdoc("\n\nThis function is consistent with the behavior of the following expression, but has better performance:")
                addKdoc("\n```\n$it\n```")
              }
              kDoc.codeComment(info).simpleMove().takeIfNotEmpty()?.also {
                addKdoc("\n\n$it")
              }

              val extraThrows = kDoc.throws(info)
              val throws = extraThrows.keys.map { "$it::class" }.toMutableList()

              addKdoc(
                buildString {
                  fun addParamKDocs() {
                    appendLine()
                    appendLine()
                    if (info.byteCount == Byte.SIZE_BYTES) {
                      appendLine("@param index The index of the data to be ${info.passiveVoice} (default is current [cursor] index).")
                    } else {
                      appendLine("@param index The starting index of the data to be ${info.passiveVoice} (default is current [cursor] index).")
                    }
                    paramKDocs.forEach { (param, kdoc) -> appendLine("@param $param $kdoc") }
                  }
                  when (info.isNullable) {
                    true -> {
                      if (kDoc.customNote.not()) {
                        appendLine()
                        appendLine()
                        if (info.isPrimitive) {
                          append("Note that it returns a boxed ${info.typeName} object, ")
                          append("${underflowException.firstCharLowercase()}, it returns `null`.")
                        } else {
                          append("Note that ${underflowException.firstCharLowercase()}, it returns `null`.")
                        }
                      }
                      addParamKDocs()
                    }
                    false -> {
                      addParamKDocs()

                      appendLine()
                      appendLine(
                        "@throws IndexOutOfBoundsException If the specified [index]" +
                          "${
                            if (extraIOOB.isEmpty()) {
                              " "
                            } else {
                              extraIOOB.joinToString(" or ", " or ", " ") { "[$it]" }
                            }
                          }is less than zero or " +
                          "exceeds the [size] range of this channel."
                      )
                      throws += "IndexOutOfBoundsException::class"

                      if (kDoc.hasUnderflowException) {
                        appendLine("@throws ChannelUnderflowException $underflowException.")
                        throws += "ChannelUnderflowException::class"
                      }

                      if (kDoc.hasOverflowException) {
                        appendLine("@throws ChannelOverflowException If exceeds the extreme size range of this channel.")
                        throws += "ChannelOverflowException::class"
                      }
                    }
                  }
                  extraThrows.forEach { (throwable, kdoc) ->
                    appendLine("@throws $throwable $kdoc")
                  }
                }
              )

              if (throws.isNotEmpty()) addAnnotation(
                AnnotationSpec.builder(THROWS)
                  .apply { throws.forEach(::addMember) }
                  .build()
              )

              codeBody(info).takeIfNotEmpty()?.also(::addCode) ?: addModifiers(KModifier.ABSTRACT)
            }
            .addParameters(requiredParamSpecs)
            .addParameter(
              ParameterSpec.builder("index", LONG)
                .defaultValue("cursor.index")
                .build()
            )
            .addParameters(optionalParamSpecs)
            .addKdoc(link.joinToString(separator = "\n", prefix = "\n\n") { "@see $it" })
            .apply {
              when (val returns = kDoc.returns(info)) {
                null -> returns(TopLevelClass(info.typeRawName).copy(info.isNullable))
                else -> returns(TopLevelClass(returns.first), CodeBlock.of(returns.second))
              }
            }

          print(generateFunction(func))
        }
      }
    }

    private fun generateFunction(builder: FunSpec.Builder) = FileSpec.builder("", "")
      .addType(
        TypeSpec
          .interfaceBuilder("Foo")
          .addFunction(builder.build())
          .build()
      ).build().toString()
      .replace("public fun", "fun")
      .replace("public interface Foo {", "")
      .replace(":\n      String?", ": String?")
      .replace("   *\n   *\n   * ", "   *\n   * ")
      .replace("\n   *\n   */", "\n   */")
      .trimEnd()
      .removeSuffix("}")
  }

}