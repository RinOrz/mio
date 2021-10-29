import com.meowool.mio.Directory
import com.meowool.mio.IFile
import com.meowool.mio.createTempDirectory
import com.meowool.mio.createTempFile
import io.kotest.core.TestConfiguration

fun TestConfiguration.tempFile(prefix: String? = null, suffix: String? = null): IFile =
  createTempFile(prefix ?: this::class.simpleName, suffix).apply {
    afterSpec { delete() }
  }

fun TestConfiguration.tempDir(prefix: String? = null): Directory =
  createTempDirectory(prefix ?: this::class.simpleName).apply {
    afterSpec { delete(recursively = true) }
  }
