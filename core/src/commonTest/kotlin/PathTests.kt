import com.meowool.mio.Directory
import com.meowool.mio.File
import com.meowool.mio.Path
import com.meowool.mio.asPath
import com.meowool.mio.endsWith
import com.meowool.mio.startsWith
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class PathTests : FreeSpec({
  val root = Path("/")
  val tempDir = tempDir()
  val tempFile = tempFile(prefix = "file-test")
  val relative = Path("./relative.tmp")
  val notNormalize = Path("foo/./bar/gav/../baz.txt")

  "test path exists" {
    tempDir.exists().shouldBeTrue()
  }

  "test both path are equals" {
    File("a/b/c") shouldNotBe Directory("a/b/c")
    File("a/b/c") shouldBe File("a/b/c")
    Path("a/b/c") shouldBe Path("a/b/d/../c")
    Path("a/b/c") shouldBe Path("a/b/d/../c")
  }

  "test name" {
    notNormalize.split() shouldBe listOf("foo", "bar", "baz.txt")
    notNormalize.endsWith("bar", "baz.txt").shouldBeTrue()
    notNormalize.endsWith(listOf("foo", "bar")).shouldBeFalse()
    notNormalize.startsWith(listOf("foo", "bar")).shouldBeTrue()

    val path = "a/b/c".asPath()
    path.split() shouldBe listOf("a", "b", "c")
    path.startsWith("a").shouldBeTrue()
    path.endsWith("/c").shouldBeFalse()

    tempFile.name shouldStartWith "file-test"
    notNormalize.name shouldBe "baz.txt"

    val file = tempDir.joinFile("name.test").create()
    file.name shouldBe "name.test"
    file.name = "rename.test"
    file.name shouldBe "rename.test"
  }

  "test path" {
    notNormalize shouldBe "foo/./bar/gav/../baz.txt"
    notNormalize.normalized shouldBe "foo/bar/baz.txt"
  }

  "test path parent" {
    root.parent.shouldBeNull()
  }

  "test path is root" {
    root.isRoot.shouldBeTrue()
  }

  "test extension" {
    val foo = tempDir.joinFile("foo.txt").create()
    val aaa =  tempDir.joinFile(".aaa.jpg").create()
    val xyz =  tempDir.joinFile(".xyz").create()

    foo.extension shouldBe "txt"
    aaa.extension shouldBe "jpg"
    xyz.extension shouldBe ""

    foo.extension = "zip"
    aaa.extension = "png"
    xyz.extension = "tmp"

    foo.extension shouldBe "zip"
    aaa.extension shouldBe "png"
    xyz.extension shouldBe "tmp"

    foo.name shouldBe "foo.zip"
    aaa.name shouldBe ".aaa.png"
    xyz.name shouldBe ".xyz.tmp"
  }

  "test is absolute" {
    tempFile.isAbsolute shouldBe true
    relative.isAbsolute shouldBe false
    notNormalize.isAbsolute shouldBe false
  }
})