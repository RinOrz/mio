import com.meowool.mio.toMioPath
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempdir

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class PathLinkTests : FreeSpec({
  val tempDir = tempdir().toMioPath()

//  "symbolic link" {
//    val file = tempDir.resolve("lns.txt").createFile()
//    val linkTarget = tempDir.resolve("symlink").deleteIfExists()
//
//    file.linkSymbolTo(linkTarget) shouldBe linkTarget
//  }
//  val tempDir = tempdir().toMioPath()
//  Files.createSymbolicLink()
//  "Path.toRealPath" - {
//    "should return the length of the string" {
//      "sammy".length shouldBe 5
//      "".length shouldBe 0
//    }
//  }
//
//  "startsWith should test for a prefix" {
//    "world" should startWith("wor")
//  }
})