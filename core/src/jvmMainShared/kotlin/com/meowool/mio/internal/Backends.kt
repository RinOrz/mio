package com.meowool.mio.internal

import com.meowool.mio.IoFile
import com.meowool.mio.JavaZip
import com.meowool.mio.JavaZipEntry
import com.meowool.mio.NioPath

internal interface NioPathBackend {
  val nioPath: NioPath
}

internal interface IoFileBackend {
  val ioFile: IoFile
}

internal interface JavaZipBackend {
  val javaZip: JavaZip
}

internal interface JavaZipEntryBackend {
  val javaZipEntry: JavaZipEntry
}
