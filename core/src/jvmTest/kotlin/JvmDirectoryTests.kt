import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.longs.shouldNotBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okio.ZipFileSystem
import okio.source
import java.io.FileNotFoundException
import java.nio.CharBuffer
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.NotDirectoryException
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.Deflater
import kotlin.io.path.getLastModifiedTime

/**
 * @author 凛 (https://github.com/RinOrz)
 */
open class JvmDirectoryTests : FreeSpec({
  "native test" {
    val tempDir = tempdir()
    val tempDirPath = tempdir().toPath()
    val tempFile = tempfile()
    val tempFilePath = tempfile().toPath()

    tempDir.length() shouldBe 64
    Files.size(tempDirPath) shouldBe 64

    tempFile.list().shouldBeNull()
    shouldThrow<NotDirectoryException> { Files.list(tempFilePath) }

    shouldThrow<FileNotFoundException> { tempDir.source() }.message shouldContain "Is a directory"
    Files.newByteChannel(tempDirPath).size() shouldBe 64
    shouldThrow<FileSystemException> {
      Files.newByteChannel(tempDirPath, StandardOpenOption.WRITE).write(Charsets.UTF_8.encode(CharBuffer.wrap("test")))
    }.message shouldContain "Is a directory"

    Files.readAttributes(tempDirPath, BasicFileAttributes::class.java).apply {
      lastModifiedTime().toMillis().shouldNotBeZero()
      lastAccessTime().toMillis().shouldNotBeZero()
      creationTime().toMillis().shouldNotBeZero()
    }
  }

})