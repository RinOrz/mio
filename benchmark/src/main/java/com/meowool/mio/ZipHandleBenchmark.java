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

@Threads(16)
@Fork(2)
@Warmup(iterations = 3)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ZipHandleBenchmark {
    private final ZipFileRuntime runtime = new ZipFileRuntime();

    @Param({"src/main/resources/kt-plugin.zip", "src/main/resources/small.zip"})
    private String path;

    @Setup
    public void setup() {
        runtime.setup(path);
    }

    //  @Benchmark
    //  public void readAllNew() {
    //    runtime.readAllNew();
    //  }
    //
    //  @Benchmark
    //  public void readAllOld() {
    //    runtime.readAllOld();
    //  }
    //
    //  @Benchmark
    //  public void readAllApache() {
    //    runtime.readAllApache();
    //  }

    //  @Benchmark
    //  public void writeNew() {
    //    runtime.writeNew();
    //  }
    //
    //  @Benchmark
    //  public void writeOld() {
    //    runtime.writeOld();
    //  }

    //  @Benchmark
    //  public void writeApache() {
    //    runtime.writeApache();
    //  }
    //
    //  @Benchmark
    //  public void writeApacheParallel() {
    //    runtime.writeApacheParallel();
    //  }
    @Benchmark
    public void readSpecifiedApache() {
        runtime.readSpecifiedApache();
    }

    @Benchmark
    public void readSpecifiedOld() {
        runtime.readSpecifiedOld();
    }

    @Benchmark
    public void readSpecifiedNew() {
        runtime.readSpecifiedNew();
    }

    @TearDown
    public void finish() {
        runtime.finish();
    }
}
