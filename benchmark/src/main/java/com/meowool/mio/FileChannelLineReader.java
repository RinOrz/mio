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
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 一行一行的读取文件，类似BufferedReader.readLine方法 <note>该类是线程非安全的，应尽量保证不在多线程环境下使用该类</note>
 *
 * @author qiuyj
 * @since 2017/12/6
 */
public class FileChannelLineReader {
    private static final String LINE_END_FLAG = "\r\n";
    private final SeekableByteChannel inChannel; // 要读取的文件的输入Channel
    private boolean isEOF; // 是否是文件的结尾
    private final Charset UTF8 = Charset.forName("UTF-8");
    private long position; // 当前读取文件的位置
    private final ByteBuffer temp = ByteBuffer.allocate(1);
    private final ByteBuffer currLineData = ByteBuffer.allocate(8192);

    public FileChannelLineReader(SeekableByteChannel inChannel) {
        Objects.requireNonNull(inChannel);
        ensureChannelOpen(inChannel);
        this.inChannel = inChannel;
    }

    private void ensureChannelOpen(SeekableByteChannel channel) {
        if (!channel.isOpen()) throw new IllegalStateException("Closed channel");
    }

    public boolean hasNextLine() {
        return !isEOF;
    }

    public String readLine() {
        String line = null;
        temp.clear();
        currLineData.clear();
        boolean isLine = false;
        try {
            while (!isEOF && !isLine) {
                int idx = inChannel.read(temp);
                if (idx == -1) {
                    // 读到了文件尾
                    isEOF = true;
                    continue;
                }
                temp.flip();
                position += idx;
                currLineData.put(temp);
                currLineData.flip();
                line = UTF8.decode(currLineData).toString();
                int lineIdx;
                if ((lineIdx = line.lastIndexOf(LINE_END_FLAG)) > -1) {
                    isLine = true;
                    line = line.substring(0, lineIdx);
                    continue;
                }
                temp.clear();
                currLineData.position(currLineData.limit());
                currLineData.limit(currLineData.capacity());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while read file");
        }
        try {
            inChannel.position(position);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error when setting the position of the file next reading");
        }
        return line;
    }

    public long position() {
        return position;
    }
}
