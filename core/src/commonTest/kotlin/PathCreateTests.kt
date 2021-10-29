@file:Suppress("BlockingMethodInNonBlockingContext")

import com.meowool.mio.DirectoryNotEmptyException
import com.meowool.mio.PathAlreadyExistsException
import com.meowool.mio.PathExistsAndIsNotDirectoryException
import com.meowool.mio.PathExistsAndIsNotFileException
import com.meowool.mio.asDirectory
import com.meowool.mio.asFile
import com.meowool.mio.joinDir
import com.meowool.mio.joinFile
import com.meowool.sweekt.iteration.contains
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class PathCreateTests : FreeSpec({
  val tempDir = tempDir()

  "file" - {
    val file = tempDir.joinFile("file.tmp")

    "create" {
      file.create() shouldBe file
      file.exists().shouldBeTrue()
      file.parent shouldBe tempDir

      tempDir.list().contains { it.name == file.name }.shouldBeTrue()
    }

    "create when exists" {
      file.write("test exists")
      file.exists().shouldBeTrue()
      file.text() shouldBe "test exists"
      file.create().text() shouldBe "test exists"
      file.create(overwrite = true) shouldBe file
      file.text() shouldBe ""
    }

    "create file but directory exists" {
      file.delete().shouldBeTrue()
      file.asDirectory().create().exists().shouldBeTrue()
      shouldThrow<PathExistsAndIsNotFileException> { file.create() }
    }

    "create strictly" {
      file.asDirectory().delete(recursively = true)
      file.create().exists().shouldBeTrue()
      shouldThrow<PathAlreadyExistsException> { file.createStrictly() }
    }
  }

  "directory" - {
    val dir = tempDir.joinDir("dir-temp")

    "create" {
      dir.create() shouldBe dir
      dir.exists().shouldBeTrue()
      dir.parent shouldBe tempDir
      dir.isDirectory.shouldBeTrue()

      tempDir.list().contains { it.name == dir.name }.shouldBeTrue()
    }

    "create when exists" {
      dir.exists().shouldBeTrue()
      dir.addFile("sub-file").exists().shouldBeTrue()
      dir.create(overwrite = true) shouldBe dir
      dir.list() shouldHaveSize 0
    }

    "create directory but file exists" {
      dir.delete().shouldBeTrue()
      dir.asFile().create().exists().shouldBeTrue()
      shouldThrow<PathExistsAndIsNotDirectoryException> { dir.create() }
    }

    "create strictly" {
      dir.delete(recursively = true).shouldBeTrue()
      dir.create().isDirectory.shouldBeTrue()
      dir.addFile("sub-file")
      shouldThrow<DirectoryNotEmptyException> { dir.createStrictly() }
      dir.delete(recursively = true)
      dir.create().exists().shouldBeTrue()
      shouldThrow<PathAlreadyExistsException> { dir.createStrictly() }
    }
  }
})