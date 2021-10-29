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

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ZipFileBenchmark {

    @Param({"src/main/resources/kt-plugin.zip", "src/main/resources/small.zip"})
    private String path;

    private final ZipFileRuntime runtime = new ZipFileRuntime();

    @Setup
    public void setup() {
        runtime.setup(path);
    }

    @Benchmark
    public void useZipFileWhile() {
        runtime.useZipFileWhile();
    }

    @Benchmark
    public void useZipFileSequence() {
        runtime.useZipFileSequence();
    }

    @Benchmark
    public void useZipFileForEach() {
        runtime.useZipFileForEach();
    }

    @Benchmark
    public void useZipFileForEachByIndex() {
        runtime.useZipFileForEachByIndex();
    }

    @TearDown
    public void finish() {
        runtime.finish();
    }
}
