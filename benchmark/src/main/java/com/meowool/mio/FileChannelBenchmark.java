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

@Fork(2)
@Warmup(iterations = 3)
@Measurement(iterations = 2)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FileChannelBenchmark extends BaseFileChannelBenchmark {
    @Benchmark
    @Override
    public void readRange() {
        super.readRange();
    }

    @Benchmark
    @Override
    public void readRangeRaf() {
        super.readRangeRaf();
    }

    @Benchmark
    @Override
    public void readAll() {
        super.readAll();
    }

    @Benchmark
    @Override
    public void readAllRaf() {
        super.readAllRaf();
    }

    @Benchmark
    @Override
    public void channel() {
        super.channel();
    }

    @Benchmark
    @Override
    public void randomAccessFile() {
        super.randomAccessFile();
    }
}
