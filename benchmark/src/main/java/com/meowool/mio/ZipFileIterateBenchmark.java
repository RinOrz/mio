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
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;

@Fork(2)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ZipFileIterateBenchmark extends BaseZipFileIterateBenchmark {

    @Param({"src/main/resources/kt-plugin.zip", "src/main/resources/small.zip"})
    private String path;

    @Setup
    @Override
    public void setup() {
        super.setup();
    }

    @Benchmark
    @Override
    public void forEachValues() {
        super.forEachValues();
    }

    @Benchmark
    @Override
    public void forEachValueParallel() {
        super.forEachValueParallel();
    }

    @Benchmark
    @Override
    public void forEachValueParallelMax() {
        super.forEachValueParallelMax();
    }

    @TearDown
    @Override
    public void finish() {
        super.finish();
    }

    @NotNull
    @Override
    public String getPath() {
        return path;
    }
}
