import com.meowool.mio.internal.IoFile
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class JvmOldFileTests : StringSpec({
  "normalize" {
    IoFile("/zip-test/a/b").path shouldBe "/zip-test/a/b"
    IoFile("/zip-test/./a/../b").normalize().path shouldBe "/zip-test/b"
  }
})