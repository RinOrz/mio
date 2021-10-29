@file:Suppress("BlockingMethodInNonBlockingContext")

import com.meowool.mio.internal.IoFile
import com.meowool.sweekt.String
import com.meowool.sweekt.iteration.toArray
import com.meowool.sweekt.size
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import okio.use
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.system.measureNanoTime

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class JvmOldZipTests : FreeSpec({
  val zip = ZipFile(javaClass.getResource("zip-test.zip")!!.path)

  suspend fun ZipFile.flow(recursively: Boolean) = flow {
    for (entry in entries())
      if (recursively || entry.name.filter { it == '/' }.size <= 1)
        emit(entry)
  }

  "create" {
    File(tempDir().toString(), "test.zip").also {
      it.outputStream().use { os ->
        ZipOutputStream(os).close()
      }
      it.exists().shouldBeTrue()
    }
  }

  "time consuming" {
    measureNanoTime {
      zip.getInputStream(zip.getEntry("test/nested1/gradle.properties")).close()
    } shouldBeLessThan measureNanoTime {
      zip.getInputStream(
        zip.entries().toList().toArray().first { it.name == "test/nested1/gradle.properties" }
      ).close()
    }
  }

  "list" {
    zip.flow(false).map(::String)
      .toList() shouldContainExactlyInAnyOrder listOf("empty.txt", "file.txt", "test/")
  }

  "walk" {
    flow {
      emit(ZipEntry("/"))
      emitAll(zip.flow(true))
    }.map(::String).toList() shouldContainExactlyInAnyOrder listOf(
      "/",
      "file.txt",
      "empty.txt",
      "test/",
      "test/nested1/", "test/nested1/gradle.properties",
      "test/nested2/"
    )
  }

  "get" {
    zip.getEntry("/file.txt")?.name.shouldBeNull()
    zip.getEntry("file.txt").name shouldBe "file.txt"
  }

  "write" {
    zip.close()
    val file = tempfile(suffix = ".zip")
    IoFile(zip.name).copyTo(file, overwrite = true)
    file.outputStream().buffered().use {
      ZipOutputStream(it).use { zos ->
        zos.putNextEntry(ZipEntry("new/"))
        zos.close()
      }
    }
    ZipFile(file).apply {
      flow(true).map(::String).toList() shouldContainExactlyInAnyOrder listOf("new/")
      getEntry("new/").isDirectory.shouldBeTrue()
    }
  }

  "entry path spec" {
    val temp = tempfile()
    ZipOutputStream(temp.outputStream().buffered()).use {
      it.putNextEntry(ZipEntry("/appendNew.txt"))
      it.write("added".toByteArray())
      it.closeEntry()
      it.putNextEntry(ZipEntry("appendNew1.txt"))
      it.write("added1".toByteArray())
      it.closeEntry()
    }
    ZipFile(temp).entries().toList().map(::String) shouldContainExactlyInAnyOrder listOf(
      "/appendNew.txt", "appendNew1.txt"
    )
  }
})