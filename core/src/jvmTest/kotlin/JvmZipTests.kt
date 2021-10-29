@file:Suppress("BlockingMethodInNonBlockingContext")

import com.meowool.mio.toNioPath
import com.meowool.sweekt.String
import com.meowool.sweekt.cast
import com.meowool.sweekt.iteration.toArray
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldNotBeEmpty
import okio.use
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.path.copyTo
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.streams.toList
import kotlin.system.measureNanoTime

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class JvmZipTests : FreeSpec({
  val uri = URI.create("jar:file:${javaClass.getResource("zip-test.zip")!!.path}")
  val fs = FileSystems.newFileSystem(uri, mapOf("create" to "true"))
  val root = fs.getPath(fs.separator)

  afterSpec { fs.cast<AutoCloseable>().use {  } }

  "time consuming" {
    measureNanoTime {
      Files.newInputStream(fs.getPath("/test/nested1/gradle.properties")).close()
    }.also { println("get entry: $it") }

    measureNanoTime {
      Files.newInputStream(
        Files.walk(root).toList().toArray()
          .first { it.startsWith("/test/nested1/gradle.properties") }
      ).close()
    }.also { println("find entry: $it") }
  }

  "list" {
    root.listDirectoryEntries().map(::String)
      .shouldContainExactlyInAnyOrder(listOf("/file.txt", "/empty.txt", "/test"))
  }

  "walk" {
    Files.walk(root).map(::String).toList()
      .shouldContainExactlyInAnyOrder(listOf(
        "/",
        "/file.txt",
        "/empty.txt",
        "/test",
        "/test/nested1", "/test/nested1/gradle.properties",
        "/test/nested2"
      ))
  }

  "write" {
    val path = fs.getPath("test/README.md")
    val text = "test string"

    path.writeText(text)
    path.exists().shouldBeTrue()
    path.readText() shouldBe text
    path.deleteIfExists().shouldBeTrue()
  }

  "read" {
    fs.getPath("empty.txt").readText().shouldBeEmpty()
    fs.getPath("file.txt").apply {
      readText().shouldNotBeEmpty()
      readLines() shouldContainInOrder listOf("One", "Two", "Three")
    }
  }

  "delete" {
    val temp = tempFile().toNioPath()
    val inZip = root.resolve(temp.name)

    temp.copyTo(inZip)
    inZip.exists().shouldBeTrue()
    shouldNotThrowAny { inZip.deleteExisting() }
  }

  "rename" {
    val path = fs.getPath("empty.txt")
    val newPath = path.resolveSibling("new.txt")

    path.exists().shouldBeTrue()
    newPath.exists().shouldBeFalse()

    path.moveTo(newPath)
    path.exists().shouldBeFalse()
    newPath.exists().shouldBeTrue()

    newPath.moveTo(path)
    path.exists().shouldBeTrue()
    newPath.exists().shouldBeFalse()
  }

  "attrs" {
    Files.readAttributes(fs.getPath("/test/nested1/gradle.properties"), "zip:*").forEach { (key, value) ->
      println("$key, $value")
    }
  }

  "performance" - {
    "use ZipFile while" {
      val uri = URI.create("jar:file:/Users/rin/Documents/Develop/Projects/meowool/toolkit/mio/benchmark/src/main/resources/kotlin-plugin.zip")
      FileSystems.getFileSystem(uri).close()
      val fs = FileSystems.newFileSystem(uri, mapOf("create" to "true"))
      afterTest { fs.close() }
      Files.walk(fs.getPath(fs.separator)).forEach {
        if (Files.isDirectory(it)) return@forEach
        println(Files.readAllBytes(it))
      }
    }
  }
})