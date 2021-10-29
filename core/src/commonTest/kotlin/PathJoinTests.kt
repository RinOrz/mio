import com.meowool.mio.Path
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class PathJoinTests: StringSpec({
  val home = Path("/home")
  val data = Path("/data")

  "join chars" {
    home.join("tmp") shouldBe "/home/tmp"
    home.join("/tmp") shouldBe "/tmp"

    home / "tmp" shouldBe "/home/tmp"
    home / "/tmp" shouldBe "/tmp"

    Path("/foo").join("bar", "gav") shouldBe "/foo/bar/gav"
    Path("/foo").join("/bar", "gav") shouldBe "/bar/gav"
  }

  "join paths" {
    home / data shouldBe home.join(data)
    home / data shouldBe "/data"

    Path("/foo").join(Path("gav")) shouldBe "/foo/gav"
    Path("/foo").join(Path("bar"), Path("/gav")) shouldBe "/gav"
  }

  "join to parent" {
    home.joinToParent("tmp") shouldBe "/tmp"
    home.joinToParent(data) shouldBe "/data"

    Path("/foo/bar").joinToParent("baz", "gav") shouldBe "/foo/baz/gav"
    Path("/foo/bar").joinToParent("/gav") shouldBe "/gav"
  }
})