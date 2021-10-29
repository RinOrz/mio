import com.meowool.mio.Zip
import com.meowool.mio.asZip
import com.meowool.mio.asZipOrNull
import com.meowool.mio.deleteRecursively
import com.meowool.mio.flowRecursively
import com.meowool.mio.joinDir
import com.meowool.mio.joinFile
import com.meowool.mio.listRecursively
import com.meowool.sweekt.String
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import kotlinx.coroutines.flow.toList
import kotlin.io.path.exists

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class ZipTests : StringSpec({
  val builtin = Zip(javaClass.getResource("zip-test.zip")!!.path)
  var zip = tempFile().apply { extension = "zip"; delete() }.asZip()

  "list builtin zip" {
    val expected = mutableListOf("/file.txt", "/empty.txt", "/test")
    builtin.list().map(::String).shouldContainExactlyInAnyOrder(expected)
    builtin.flow().toList().map(::String).shouldContainExactlyInAnyOrder(expected)

    expected.apply {
      add("/test/nested1")
      add("/test/nested1/gradle.properties")
      add("/test/nested2")
    }

    builtin.listRecursively().map(::String).shouldContainExactlyInAnyOrder(expected)
    builtin.flowRecursively().toList().map(::String).shouldContainExactlyInAnyOrder(expected)
  }

  "create zip" {
    zip.create().asZipOrNull().shouldNotBeNull()
    zip.exists().shouldBeTrue()
  }

  "write" {
    val path = zip.joinFile("README.md")
    val text = "test string"

    path.write(text)
    path.exists().shouldBeTrue()
    path.text() shouldBe text
  }

  "copy temp file into zip" {
    val path = zip.joinFile("temp.txt")
    val temp = tempFile()
    temp.write("temp file")
    temp.copyTo(path)
    path.text() shouldBe temp.text()
  }

  "move entry out to zip" {
    val path = zip.joinFile("temp.txt")
    path.moveInto(zip.parent!!, overwrite = true).exists().shouldBeTrue()
    path.exists().shouldBeFalse()
  }

  "add directory" {
    val dir = tempDir().apply { addFile("test") }

    dir.list().first().name shouldBe "test"
    zip.add(dir).exists().shouldBeTrue()
    zip.joinDir(dir.name).apply {
      walk().map(::String)
        .shouldContainExactlyInAnyOrder(listOf("/${dir.name}", "/${dir.name}/test"))
    }

    zip.addDir(dir.name, overwrite = true).exists().shouldBeTrue()
    zip.joinDir(dir.name).apply {
      listRecursively().shouldBeEmpty()
      deleteRecursively().shouldBeTrue()
    }
  }

  "add file" {
    val file = tempFile().write("test")
    zip.add(file).exists().shouldBeTrue()
    zip.joinFile(file.name).apply {
      isRegularFile.shouldBeTrue()
      text() shouldBe "test"
    }

    zip.addFile(file.name, overwrite = true).exists().shouldBeTrue()
    zip.joinFile(file.name).apply {
      text().shouldBeEmpty()
      delete().shouldBeTrue()
    }
  }

  "list test zip" {
    zip.close()
    zip.exists().shouldBeTrue()

    zip = Zip(zip.toString())

    val expected = mutableListOf("/README.md")
    zip.list().map(::String).shouldContainExactlyInAnyOrder(expected)
    zip.flow().toList().map(::String).shouldContainExactlyInAnyOrder(expected)
  }

})