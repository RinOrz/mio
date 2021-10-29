/*
 * Copyright (c) 2021. The Meowool Organization Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 * In addition, if you modified the project, you must include the Meowool
 * organization URL in your code file: https://github.com/meowool
 *
 * 如果您修改了此项目，则必须确保源文件中包含 Meowool 组织 URL: https://github.com/meowool
 */
package com.meowool.mio;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.openjdk.jmh.annotations.*;

@Fork(2)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@SuppressWarnings("ALL")
public class ZipFileFetchBenchmark {

    @Param({"src/main/resources/kt-plugin.zip", "src/main/resources/small.zip"})
    private String path;

    private ZipFile zip;
    private HashMap<String, ZipEntry> map = new HashMap<>();
    private ConcurrentHashMap<String, ZipEntry> chm = new ConcurrentHashMap<>();

    @Setup
    public void setup() throws IOException {
        zip = new ZipFile(path);
        chm.clear();
        map.clear();

        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            map.put(entry.getName(), entry);
            chm.put(entry.getName(), entry);
        }
    }

    @Benchmark
    public void getEntryByCHMap() {
        chm.get("Kotlin/kotlinc/build.txt");
    }

    @Benchmark
    public void getEntryByMap() {
        map.get("Kotlin/kotlinc/build.txt");
    }

    //  @Benchmark
    //  public void getEntryByMapWithIndex() {
    //    ZipEntry entry = array[ids.get("Kotlin/kotlinc/build.txt")];
    //  }
    //
    //  @Benchmark
    //  public void getEntry() {
    //    zip.getEntry("/Kotlin/kotlinc/build.txt");
    //  }

    //  @Benchmark
    //  public void filterEntry() {
    //    ZipEntry result;
    //    Enumeration<? extends ZipEntry> entries = zip.entries();
    //    while (entries.hasMoreElements()) {
    //      ZipEntry entry = entries.nextElement();
    //      if ("Kotlin/kotlinc/build.txt".equals(entry.getName())) {
    //        result = entry;
    //        break;
    //      }
    //    }
    //  }

    @TearDown
    public void finish() throws IOException {
        zip.close();
    }
}
