@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

import com.meowool.mio.toNioPath
import com.meowool.mio.write
import io.kotest.core.spec.style.FreeSpec
import okio.use

/**
 * @author 凛 (https://github.com/RinOrz)
 */
class ZipBackendTests : FreeSpec({
  "generates" - {
    val file = tempFile(suffix = ".zip").toNioPath()
    ZipImpl(file).create(overwrite = true).use { zip ->
      zip.addFile("dir/foo.txt").write("A new file entry")
    }
  }
})