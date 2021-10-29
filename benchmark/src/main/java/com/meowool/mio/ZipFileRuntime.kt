package com.meowool.mio

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile as ApacheZipFile


/**
 * @author 凛 (https://github.com/RinOrz)
 */
class ZipFileRuntime {
  private lateinit var file: File
  private lateinit var zipFS: FileSystem
  private lateinit var zipFile: ZipFile
  private lateinit var zipFileApache: ApacheZipFile
  private val temps = mutableListOf<File>()

  fun setup(path: String) {
    file = File(path)
    zipFile = ZipFile(file)
    zipFS = FileSystems.newFileSystem(
      URI.create("jar:file:${file.absolutePath}"),
      mapOf("create" to "true")
    )
    zipFileApache = org.apache.commons.compress.archivers.zip.ZipFile(file)
  }

  fun finish() {
    if (::zipFS.isInitialized) zipFS.close()
    if (::zipFile.isInitialized) zipFile.close()
    if (::zipFileApache.isInitialized) zipFileApache.close()
    temps.forEach { it.delete() }
  }

  fun useZipFileWhile() = ZipFile(file).use { zipFile ->
    val list = mutableListOf<ZipEntry>()
    val entries = zipFile.entries()
    while (entries.hasMoreElements()) {
      val entry = entries.nextElement()
      list.add(entry)
    }
  }

  fun useZipFileSequence() = ZipFile(file).use { zipFile ->
  val list = mutableListOf<ZipEntry>()
    zipFile.entries().asSequence().forEach { entry ->
      list.add(entry)
    }
  }

  fun useZipFileForEach() = ZipFile(file).use { zipFile ->
  val list = mutableListOf<ZipEntry>()
    for (entry in zipFile.entries()) {
      list.add(entry)
    }
  }

  fun useZipFileForEachByIndex() = ZipFile(file).use { zipFile ->
  val list = mutableListOf<ZipEntry>()
    val entries = zipFile.entries()
    repeat(zipFile.size()) {
      val entry = entries.nextElement()
      list.add(entry)
    }
  }

  fun readAllOld() {
    val entries = zipFile.entries()
    while (entries.hasMoreElements()) {
      val entry = entries.nextElement()
      if (entry.isDirectory) continue
      zipFile.getInputStream(entry).readAllBytes()
    }
  }

  fun readAllNew() {
    Files.walk(zipFS.getPath(zipFS.separator)).forEach {
      if (Files.isDirectory(it)) return@forEach
      Files.newInputStream(it).readAllBytes()
    }
  }

  fun readAllApache() {
    val entries = zipFileApache.entries
    while (entries.hasMoreElements()) {
      val entry = entries.nextElement()
      if (entry.isDirectory) continue
      zipFileApache.getInputStream(entry).readAllBytes()
    }
  }

  fun readSpecifiedOld() {
    zipFile.getInputStream(zipFile.getEntry("Kotlin/kotlinc/build.txt"))
      .readAllBytes()
  }

  fun readSpecifiedNew() {
    Files.newInputStream(zipFS.getPath("/Kotlin/kotlinc/build.txt"))
      .readAllBytes()
  }

  fun readSpecifiedApache() {
    zipFileApache.getInputStream(zipFileApache.getEntry("Kotlin/kotlinc/build.txt"))
      .readAllBytes()
  }

  fun writeNew() {
    val temp = File.createTempFile("zipbenchmark", "zip").apply { delete() }.apply(temps::add)
    FileSystems.newFileSystem(
      URI.create("jar:file:${temp.absolutePath}"),
      mapOf("create" to "true")
    ).use { fs ->
      val entry = fs.getPath("test")
      Files.newOutputStream(entry).use {
        file.inputStream().buffered().use { fis ->
          fis.copyTo(it)
        }
      }
      Files.setLastModifiedTime(entry, FileTime.fromMillis(file.lastModified()))
    }
  }

  fun writeOld() {
    File.createTempFile("zipbenchmark", "zip").apply(temps::add).outputStream().buffered().use { out ->
      ZipOutputStream(out).use { zos ->
        zos.putNextEntry(ZipEntry(file.name))
        if (file.isFile) file.inputStream().buffered().use { it.copyTo(zos) }
      }
    }
  }

  fun writeApache() {
    val temp = File.createTempFile("zipbenchmark", "zip").apply(temps::add)
    ZipArchiveOutputStream(temp).use { zos ->
      zos.putArchiveEntry(zos.createArchiveEntry(file, "test"))
      if (file.isFile) file.inputStream().buffered().use { it.copyTo(zos) }
      zos.closeArchiveEntry()
    }
  }

  fun writeApacheParallel() {
    val scatterZipCreator = ParallelScatterZipCreator()
    val temp = File.createTempFile("zipbenchmark", "zip").apply(temps::add)
    scatterZipCreator.addArchiveEntry(ZipArchiveEntry(file, "test").apply { method = ZipEntry.DEFLATED }) {
      file.inputStream().buffered()
    }

    ZipArchiveOutputStream(temp).use { zos ->
      scatterZipCreator.writeTo(zos)
    }
  }
}
