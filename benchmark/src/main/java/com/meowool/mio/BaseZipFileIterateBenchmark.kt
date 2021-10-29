package com.meowool.mio

import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

abstract class BaseZipFileIterateBenchmark {
  abstract val path: String

  private lateinit var zip: ZipFile
  private lateinit var map: ConcurrentHashMap<String, ZipEntry>

  open fun setup() {
    zip = ZipFile(path)
    map = ConcurrentHashMap(zip.size())
  }

  open fun forEachValues() {
    val list = mutableListOf<ZipEntry>()
    map.values.forEach {
      list.add(it)
    }
  }

  open fun forEachValueParallel() {
    val list = mutableListOf<ZipEntry>()
    map.forEachValue(map.values.size.toLong()) {
      list.add(it)
    }
  }

  open fun forEachValueParallelMax() {
    val list = mutableListOf<ZipEntry>()
    map.forEachValue(map.values.size / 8L) {
      list.add(it)
    }
  }

  open fun finish() {
    zip.close()
  }
}