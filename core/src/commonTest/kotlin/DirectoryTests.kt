import com.meowool.mio.IllegalPathException
import com.meowool.mio.requireDirectory
import com.meowool.mio.requireExists
import com.meowool.mio.requireRegularFile
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.ints.shouldNotBeZero
import io.kotest.matchers.shouldBe

/**
 * @author 凛 (https://github.com/RinOrz)
 */
open class DirectoryTests : FreeSpec({
  val tempDir = tempDir()

  "is a existing directory" {
    shouldNotThrow<IllegalPathException> { tempDir.requireExists() }
    shouldNotThrow<IllegalPathException> { tempDir.requireDirectory() }
    shouldThrow<IllegalPathException> { tempDir.requireRegularFile() }
  }

  "clear directory" {
    tempDir.addFile("test").exists().shouldBeTrue()
    tempDir.list().size.shouldNotBeZero()
    tempDir.clear().shouldBeTrue()
    tempDir.list() shouldBe emptyList()
  }
})