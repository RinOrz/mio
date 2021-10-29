import com.meowool.mio.Path
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class PathRelativeTests: StringSpec({
  "relative path" {
    val base = Path("/data")
    val target = Path("/data/sub/sub2/file.txt")

    base.relativeTo(target) shouldBe "sub/sub2/file.txt"
    target.relativeTo(base) shouldBe "../../.."
  }

  "relative directory content" {
    val baseDir = Path("/home/dir/")
    val work = Path("/home/dir/work/")
    val file = Path("/home/dir/work/file.text")
    val targetDir = Path("/home/target-dir/")
    val workRelative = baseDir.relativeTo(work).normalized
    val fileRelative = baseDir.relativeTo(file).normalized
    workRelative shouldBe "work"
    fileRelative shouldBe "work/file.text"
    targetDir.join(workRelative).normalized shouldBe "/home/target-dir/work"
    targetDir.join(fileRelative).normalized shouldBe "/home/target-dir/work/file.text"
  }
})