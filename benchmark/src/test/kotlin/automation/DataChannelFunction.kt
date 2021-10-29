@file:Suppress("TestFunctionName", "MemberVisibilityCanBePrivate")

package automation

import com.meowool.sweekt.firstCharLowercase
import com.meowool.sweekt.firstCharTitlecase
import com.meowool.sweekt.iteration.joinTo
import com.meowool.sweekt.takeIfNotEmpty
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class DataChannelFunction(
  val kind: String,
  val returns: KClass<*>,
  val direction: Direction,
  val nameWithReturnsTitle: Boolean,
  val isNullable: Boolean,
) {
  private var codeBody: String? = null

  val doc = KDoc()
  val kindTitle = kind.firstCharTitlecase()
  val returnsName: String = returns.simpleName!!
  var returnsNameTitle: String = returnsName.replace("ByteArray", "Bytes")
  val returnsNameDesc: String = returnsName.firstCharLowercase()
  val isReturnByte = returns == Byte::class
  val isReturnPrimitive = when (returns) {
    Byte::class, Short::class, Int::class, Long::class, Float::class, Double::class, Char::class -> true
    else -> false
  }
  val isReturnOneByteCount by lazy {
    returnsByteCount() == Byte.SIZE_BYTES
  }
  val leftOrRight by lazy {
    when (direction) {
      Direction.Left -> "left"
      Direction.Right -> "right"
    }
  }
  val builder by lazy { FunSpec.builder(name) }
  val leftOrRightTitle by lazy {
    leftOrRight.firstCharTitlecase()
  }
  val leftTitleOrEmpty by lazy {
    when (direction) {
      Direction.Left -> "Left"
      Direction.Right -> ""
    }
  }

  val plusOrMinus = when (direction) {
    Direction.Left -> "-"
    Direction.Right -> "+"
  }

  val nullSymbolOrEmpty by lazy {
    when (isNullable) {
      true -> "?"
      else -> ""
    }
  }

  val name by lazy {
    buildString {
      append(kind)
      if (direction == Direction.Left) append("Left")
      if (nameWithReturnsTitle) append(returnsNameTitle)
      if (isNullable) append("OrNull")
    }
  }

  fun valueRef(wrapLink: Boolean = false, elseCount: String = ""): String {
    val value = returnsNameDesc.replace("byteArray", if (wrapLink) "bytes" else "byte array")
    val ref = buildString {
      append("[$value]")
      if (isReturnByte.not() && isReturnPrimitive) {
        append(" value")
        returnsByteCountStatement(elseCount).takeIfNotEmpty()?.let { append(" ($it)") }
      }
    }
    return if (wrapLink) ref else ref.replace("[", "").replace("]", "")
  }

  fun returnsByteCount(elseCount: Int = -1) = when (returns) {
    Boolean::class, Byte::class -> Byte.SIZE_BYTES
    Short::class -> Short.SIZE_BYTES
    Int::class -> Int.SIZE_BYTES
    Long::class -> Long.SIZE_BYTES
    Float::class -> Float.SIZE_BYTES
    Double::class -> Double.SIZE_BYTES
    Char::class -> Char.SIZE_BYTES
    else -> elseCount
  }

  fun returnsByteCountStr(elseCount: String = "") = when (val count = returnsByteCount(-1)) {
    -1 -> elseCount
    else -> count.toString()
  }

  fun returnsByteCountWord(elseCount: String = "") = when (returnsByteCount()) {
    1 -> "one"
    2 -> "two"
    4 -> "four"
    8 -> "eight"
    else -> elseCount
  }

  fun returnsByteCountStatement(elseStatement: String) = when (returnsByteCount()) {
    1 -> "one byte"
    -1 -> elseStatement
    else -> "${returnsByteCountWord()} bytes"
  }

  fun movePlusOrMinus(count: String, base: String = "index") =
    "cursor.moveTo($base·$plusOrMinus·$count)"

  inline fun <reified T> returns(comment: String? = null) = returns(T::class.simpleName!!, comment)

  fun returns(type: String, comment: String? = null) = apply {
    comment?.let(doc::returns)
    builder.returns(TopLevelClass(type))
  }

  inline fun <reified T> param(
    name: String,
    comment: String? = null,
    defaultValue: String? = null
  ) = param(name, T::class.simpleName!!, comment, defaultValue)

  fun param(
    name: String,
    type: String,
    comment: String? = null,
    defaultValue: String? = null
  ) = apply {
    comment?.let { doc.param(name, it) }
    builder.addParameter(
      ParameterSpec.builder(name, TopLevelClass(type))
        .defaultValue(defaultValue?.let { CodeBlock.of(it) })
        .build()
    )
  }

  inline fun <reified T> throws(comment: String) = throws(T::class.simpleName!!, comment)

  fun throws(throwable: String, comment: String) = apply {
    val typeName = TopLevelClass("Throws")
    when (val annotation = builder.annotations.firstOrNull { it.typeName == typeName }) {
      null -> builder.addAnnotation(
        AnnotationSpec.builder(typeName)
          .addMember("$throwable::class")
          .build()
      )
      else -> {
        builder.annotations.remove(annotation)
        builder.addAnnotation(annotation.toBuilder().addMember("$throwable::class").build())
      }
    }
    doc.throws(throwable, comment)
  }

  fun body(codeBody: String) {
    this.codeBody = codeBody
  }

  fun returnBody(codeBody: String) {
    this.codeBody = "return $codeBody"
  }

  fun print() {
    println()
    @Suppress("SpellCheckingInspection")
    println(
      FileSpec.builder("", "").addType(
        TypeSpec.interfaceBuilder("Foo").addFunction(
          builder.addKdoc(doc.toString()).apply {
            if (builder.build().returnType == null) {
              returns(TopLevelClass(returnsName).copy(isNullable))
            }
            codeBody?.also(::addCode) ?: addModifiers(KModifier.ABSTRACT)
          }.build()
        ).build()
      ).build().toString()
        .replace("poped", "popped")
        .replace("droped", "dropped")
        .replace("geted", "returned")
        .replace(" - 0", "")
        .replace("=\n    ", "= ")
        .replace("\n    =", " =")
        .replace("seted", "set")
        .replace("`get`", "get")
        .replace(": Unit", "")
        .replace("public fun", "fun")
        .replace("public operator fun", "operator fun")
        .replace("public interface Foo {", "")
        .trimEnd().removeSuffix("}")
        .trimIndent()
        .trimEnd().trimStart()
        .replace(":\n    String", ": String")
    )
  }

  private fun TopLevelClass(name: String) = ClassName("", name)

  enum class Direction { Left, Right }

  class KDoc {
    private var paragraphs = mutableListOf<String>()
    private var examples = mutableListOf<String>()
    private var sees = mutableListOf<String>()
    private var params = mutableMapOf<String, String>()
    private var throws = mutableMapOf<String, String>()
    private var returns: String? = null

    fun add(comment: String) = apply { paragraphs += comment.wrapLine() }

    fun example(title: String = "For example", code: String) = apply {
      examples.add(buildString {
        append(title.wrapLine())
        appendLine(':')
        appendLine("```")
        appendLine(code.trimIndent())
        append("```")
      })
    }

    fun consistentBehavior(code: String) = example(
      "This function is consistent with the behavior of the following expression, but this function has better performance".wrapLine(),
      code
    )

    fun param(name: String, comment: String) = apply {
      params[name] = comment
    }

    fun throws(throwable: String, comment: String) = apply {
      throws[throwable] = comment
    }

    fun returns(comment: String) = apply {
      returns = comment
    }

    fun see(link: String) = apply {
      sees += link
    }

    override fun toString(): String = buildString {
      // Paragraph1
      //
      // Paragraph2
      //
      // Example1
      //
      // Example2
      //
      // Param1
      // Param2
      //
      // Throw1
      // Throw2
      //
      // Returns
      //
      // See1
      // See2

      paragraphs.joinTo(this, "\n\n")

      if (examples.isNotEmpty()) examples.joinTo(this, "\n\n", prefix = "\n\n")

      if (params.isNotEmpty()) params.joinTo(this, "\n", prefix = "\n\n") { k, v ->
        "@param $k $v".formatMultiLine()
      }

      if (throws.isNotEmpty()) throws.joinTo(this, "\n", prefix = "\n\n") { k, v ->
        "@throws $k $v".formatMultiLine()
      }

      returns?.let {
        appendLine()
        appendLine()
        append("@return $it".formatMultiLine())
      }

      if (sees.isNotEmpty()) sees.joinTo(this, "\n", prefix = "\n\n") {
        "@see $it".formatMultiLine()
      }
    }.replace("a int", "an int")

    private fun String.formatMultiLine(): String = buildString {
      var prevLine: String? = null
      wrapLine().lines().forEachIndexed { index, line ->
        when (index) {
          0 -> append(line)
          else -> {
            val spaced = when (prevLine) {
              null -> "  $line"
              else -> "$prevLine $line"
            }.wrapLine()
            prevLine = spaced.substringAfter('\n', "").takeIfNotEmpty()

            appendLine()
            when (prevLine) {
              null -> append(spaced)
              else -> {
                append(spaced.removeSuffix(prevLine!!))
                prevLine = null
              }
            }
          }
        }
      }
    }
  }
}

inline fun <reified T> Any.printFunction(
  name: String = this.toString(),
  nameWithReturnsTitle: Boolean = false,
  isNullable: Boolean = false,
  direction: DataChannelFunction.Direction = DataChannelFunction.Direction.Right,
  noinline block: DataChannelFunction.() -> Unit = {}
) {
  DataChannelFunction(
    name,
    T::class,
    direction,
    nameWithReturnsTitle,
    isNullable,
  ).apply(block).print()
}

inline fun <reified T> Any.printFunctions(
  kind: String = this.toString(),
  nameWithReturnsTitle: Boolean = true,
  allowNullable: Boolean = true,
  allowDoubleDirection: Boolean = true,
  noinline block: DataChannelFunction.() -> Unit = {}
) = _printFunctions(
  kind,
  T::class,
  nameWithReturnsTitle,
  allowNullable,
  allowDoubleDirection,
  block
)

fun _printFunctions(
  kind: String,
  returns: KClass<*>,
  nameWithReturnsTitle: Boolean,
  allowNullable: Boolean,
  allowDoubleDirection: Boolean,
  block: DataChannelFunction.() -> Unit = {}
) {
  mutableListOf(DataChannelFunction.Direction.Right)
    .apply { if (allowDoubleDirection) add(DataChannelFunction.Direction.Left) }
    .forEach { direction ->
      mutableListOf(false)
        .apply { if (allowNullable) add(true) }
        .forEach { isNullable ->
          DataChannelFunction(kind, returns, direction, nameWithReturnsTitle, isNullable)
            .apply(block).print()
        }
    }
}