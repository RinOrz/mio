@file:Suppress("SpellCheckingInspection")

import com.meowool.toolkit.sweekt.firstCharLowercase
import com.meowool.toolkit.sweekt.firstCharTitlecase
import com.meowool.toolkit.sweekt.firstCharUppercase
import com.meowool.toolkit.sweekt.isChinese
import com.meowool.toolkit.sweekt.isContainsChinese
import com.meowool.toolkit.sweekt.isContainsEnglish
import com.meowool.toolkit.sweekt.isEnglish
import com.meowool.toolkit.sweekt.lastCharLowercase
import com.meowool.toolkit.sweekt.lastCharUppercase
import com.meowool.toolkit.sweekt.remove
import com.meowool.toolkit.sweekt.removeBlanks
import com.meowool.toolkit.sweekt.removeFirst
import com.meowool.toolkit.sweekt.removeLast
import com.meowool.toolkit.sweekt.replaceLastChar
import com.meowool.toolkit.sweekt.substringAfter
import com.meowool.toolkit.sweekt.substringBefore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotStartWith

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class StringTests : StringSpec({
  "removeBlanks" { "com . meowool . toolkit . sweekt".removeBlanks() shouldBe "com.meowool.toolkit.sweekt" }

  "firstCharTitlecase" {
    "sweekt".firstCharTitlecase() shouldBe "Sweekt"
    ".t？".firstCharTitlecase() shouldBe ".t？"
    "什么".firstCharTitlecase() shouldBe "什么"
  }
  "firstCharUppercase" { "abc".firstCharUppercase() shouldBe "Abc" }
  "firstCharLowercase" { "SWEEKT".firstCharLowercase() shouldBe "sWEEKT" }

  "lastCharUppercase" { "sweekt".lastCharUppercase() shouldBe "sweekT" }
  "lastCharLowercase" { "SWEEKTS".lastCharLowercase() shouldBe "SWEEKTs" }

  "replaceLastChar" {
    "测试".replaceLastChar { "个屁" } shouldBe "测个屁"
    "test".replaceLastChar("ted") shouldBe "tested"
    "QAA".replaceLastChar('Q') shouldBe "QAQ"
  }

  "isChinese" {
    "全中文".isChinese() shouldBe true
    "符号，对吧".isChinese() shouldBe true
    "英文符号,不对".isChinese() shouldBe false
    "Tomorrow 我的 friend 要交个作业".isChinese() shouldBe false
  }

  "isContainsChinese" {
    "Tomorrow 我的 friend 要交个作业".isContainsChinese() shouldBe true
    "test。".isContainsChinese(ignorePunctuation = false) shouldBe true
    "test。".isContainsChinese(ignorePunctuation = true) shouldBe false
  }

  "isEnglish" {
    "Be patient! It may take a little while to generate your strings...".isEnglish() shouldBe true
    "A text...然后".isEnglish() shouldBe false
  }

  "isContainsEnglish" {
    "Tomorrow 我的 friend 要交个作业".isContainsEnglish() shouldBe true
    "测试.".isContainsEnglish(ignorePunctuation = false) shouldBe true
    "测试.".isContainsEnglish(ignorePunctuation = true) shouldBe false
  }

  "substringBefore" {
    "01234567".substringBefore("2") shouldBe "01"
    "01234567".substringBefore(2) shouldBe "01"
    "01234567".substringBefore(0) shouldBe ""
    "01234567".substringBefore(-1) shouldBe "01234567"
    "01234567".substringBefore(10) shouldBe "01234567"
  }
  "substringAfter" {
    "01234567".substringAfter("5") shouldBe "67"
    "01234567".substringAfter(5) shouldBe "67"
    "01234567".substringAfter(7) shouldBe ""
    "01234567".substringAfter(-1) shouldBe "01234567"
    "01234567".substringAfter(10) shouldBe "01234567"
  }

  "remove index" {
    "01234567".remove(5) shouldBe "0123467"
  }
  "removeFirst" {
    "首尾".removeFirst() shouldNotStartWith "首"
    "1文本测试".removeFirst() shouldBe "文本测试"
  }
  "removeLast" {
    "首尾".removeLast() shouldNotStartWith "尾"
    "一段文本。".removeLast() shouldBe "一段文本"
  }
})