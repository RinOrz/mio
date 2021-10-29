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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@Fork(2)
// @Warmup(iterations = 2)
@Measurement(iterations = 3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ByteBufferBenchmark {
    Path path =
            Paths.get(
                    "/Users/rin/Documents/Develop/Projects/meowool/toolkit/mio/benchmark/src/main/java/com/meowool/mio/BaseZipFileIterateBenchmark.kt");

    //    @Benchmark
    //    public void normalizeByPath() {
    //        try {
    //            ByteBuffer buffer = ByteBuffer.allocate(6);
    //            FileChannel.open(path).read(buffer);
    //            System.out.println(new String(buffer.array()));
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        Files.newInputStream()
    //    }
    //
    //    @Benchmark
    //    public void normalizeByPath() {
    //        try {
    //            ByteBuffer buffer = ByteBuffer.allocate(6);
    //            FileChannel.open(path).read(buffer);
    //            System.out.println(new String(buffer.array()));
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //    }
}
