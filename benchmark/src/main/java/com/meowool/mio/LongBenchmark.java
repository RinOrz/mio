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

import com.meowool.mio.internal.AVLTreeList;
import com.meowool.mio.internal.TreeList;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@Fork(2)
@Warmup(iterations = 2)
@Measurement(iterations = 2)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LongBenchmark {
    static final int CAPACITY = 100;

    public LongBenchmark() {
        acceptTreeList();
        acceptAVLTreeList();
    }

    /////////////////////////////////////////////
    ////            TreeList            ////
    /////////////////////////////////////////////

    TreeList<Object> mTreeList = new TreeList<>();

    public void acceptTreeList() {
        if (mTreeList.getSize() > CAPACITY * 3) return;
        for (long i = 0; i < CAPACITY; i++) mTreeList.add(i);
    }

    @Benchmark
    public void addFirst_TreeList() {
        mTreeList.addAt(0, 20);
    }

    @Benchmark
    public void addMiddle_TreeList() {
        mTreeList.addAt((mTreeList.getSize() - 1) / 2, 20);
    }

    @Benchmark
    public void addLast_TreeList() {
        mTreeList.addAt(0, 20);
    }

    @Benchmark
    public void getFirst_TreeList() {
        Object i = mTreeList.get(0);
        assert i != null;
    }

    @Benchmark
    public void getMiddle_TreeList() {
        Object i = mTreeList.get((mTreeList.getSize() - 1) / 2);
        assert i != null;
    }

    @Benchmark
    public void getLastTreeList() {
        Object i = mTreeList.get(mTreeList.getSize() - 1);
        assert i != null;
    }

    @Benchmark
    public void removeIndexTreeList() {
        mTreeList.removeAt(0);
        mTreeList.removeAt(0);
        mTreeList.removeAt(mTreeList.getSize() / 2);
        mTreeList.removeAt(mTreeList.getSize() - 1);
    }

    @Benchmark
    public void forEachTreeList() {
        mTreeList.forEach(
                o -> {
                    Object o1 = o;
                    return null;
                });
    }

    /////////////////////////////////////////////
    ////            AVLTreeList            ////
    /////////////////////////////////////////////

    AVLTreeList<Object> mAVLTreeList = new AVLTreeList<>();

    public void acceptAVLTreeList() {
        if (mAVLTreeList.size() > CAPACITY * 3) return;
        for (long i = 0; i < CAPACITY; i++) mAVLTreeList.add(i);
    }

    @Benchmark
    public void addFirst_AVLTreeList() {
        mAVLTreeList.add(0, 20);
    }

    @Benchmark
    public void addMiddle_AVLTreeList() {
        mAVLTreeList.add((mAVLTreeList.size() - 1) / 2, 20);
    }

    @Benchmark
    public void addLast_AVLTreeList() {
        mAVLTreeList.add(0, 20);
    }

    @Benchmark
    public void getFirst_AVLTreeList() {
        Object i = mAVLTreeList.get(0);
        assert i != null;
    }

    @Benchmark
    public void getMiddle_AVLTreeList() {
        Object i = mAVLTreeList.get((mAVLTreeList.size() - 1) / 2);
        assert i != null;
    }

    @Benchmark
    public void getLastAVLTreeList() {
        Object i = mAVLTreeList.get(mAVLTreeList.size() - 1);
        assert i != null;
    }

    @Benchmark
    public void removeIndexAVLTreeList() {
        mAVLTreeList.remove(0);
        mAVLTreeList.remove(0);
        mAVLTreeList.remove(mAVLTreeList.size() / 2);
        mAVLTreeList.remove(mAVLTreeList.size() - 1);
    }

    @Benchmark
    public void forEachAVLTreeList() {
        for (Object o : mAVLTreeList) {
            Object o1 = o;
        }
    }
}
