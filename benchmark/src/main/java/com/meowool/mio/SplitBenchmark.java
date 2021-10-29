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

import com.meowool.mio.internal.LongHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@Fork(2)
@Warmup(iterations = 2)
@Measurement(iterations = 2)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SplitBenchmark {
    static final int CAPACITY = 100088;
    ConcurrentHashMap<Long, Long> map = new ConcurrentHashMap<>(CAPACITY);

    @Benchmark
    public void a1SetAll() {
        map.clear();
        for (long i = 0; i < CAPACITY; i++) {
            map.put(i, i);
        }
    }

    @Benchmark
    public void forEach() {
        long index = 0;
        for (Long key : map.keySet()) {
            index = key;
            // use key and value
        }
    }

    @Benchmark
    public void getIndex() {
        long i1 = map.get(CAPACITY / 2L);
        long i2 = map.get(map.size() / 2L) + 1;
    }

    @Benchmark
    public void removeIndex() {
        map.remove(0L);
        map.remove(0L);
        map.remove(CAPACITY / 2L);
        map.remove(CAPACITY / 2L);
    }

    @Benchmark
    public void addIndex() {
        map.put(map.size() + 1L, 20L);
        map.put(map.size() - 50L, 20L);
    }

    /** Fast-HashMap */
    LongHashMap klm = new LongHashMap(CAPACITY);

    @Benchmark
    public void a1SetAllKFast() {
        klm.clear();
        for (long i = 0; i < CAPACITY; i++) {
            klm.set(i, i);
        }
    }

    @Benchmark
    public void getIndexKFast() {
        long i1 = klm.get(CAPACITY / 2L);
        long i2 = klm.get(klm.size() / 2L) + 1;
    }

    @Benchmark
    public void removeIndexKFast() {
        klm.remove(0L);
        klm.remove(0L);
        klm.remove(CAPACITY / 2L);
        klm.remove(CAPACITY / 2L);
    }

    @Benchmark
    public void addIndexKFast() {
        klm.set(klm.size() + 1L, 20L);
        klm.set(klm.size() - 50L, 20L);
    }

    //        Benchmark                   Mode  Cnt         Score        Error  Units
    //        SplitBenchmark.addIndex     avgt    6   4547588.299 ± 183574.929  ns/op
    //        SplitBenchmark.getIndex     avgt    6         1.897 ±      0.039  ns/op
    //        SplitBenchmark.removeIndex  avgt    6  17222210.940 ± 852222.807  ns/op
}
