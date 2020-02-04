/*
 * Copyright (C) Baidu Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.titan.core.util;

import java.io.IOException;
import java.io.Writer;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/10/18
 */
public class TitanLogger {
    /** indent level*/
    private int indentLevel = 0;
    /** 是否显示tag*/
    private boolean showTag;
    /** 用于输入日志的writer*/
    private Writer writer;

    /**
     * constructor
     *
     * @param writer 用于输入日志的writer
     * @param showTag 是否显示tag
     */
    public TitanLogger(Writer writer, boolean showTag) {
        this.writer = writer;
        this.showTag = showTag;
    }

    /**
     * 输出缩进
     */
    private void writeIndent() {
        for (int i = 0; i < indentLevel; i++) {
            safeWrite("    ");
        }
    }

    /**
     * 换行
     */
    private void newLine() {
        safeWrite("\n");
    }

    /**
     * 写字符串到writer并catch exception
     *
     * @param str 要输出的字符串
     */
    private void safeWrite(String str) {
        try {
            writer.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void i(String tag, String msg) {
        if (showTag) {
            safeWrite("I " + tag + " :");
        }
        writeIndent();
        safeWrite(msg);
        newLine();
    }

    public void e(String tag, String msg) {
        if (showTag) {
            safeWrite("E " + tag + " :");
        }
        writeIndent();
        safeWrite(msg);
        newLine();
    }

    public void d(String tag, String msg) {
        if (showTag) {
            safeWrite("D " + tag + " :");
        }
        writeIndent();
        safeWrite(msg);
        newLine();
    }

    /**
     * 增加缩进
     */
    public void incIndent() {
        indentLevel ++;
    }

    /**
     * 减少缩进
     */
    public void decIndent() {
        indentLevel --;
        if (indentLevel < 0) {
            indentLevel = 0;
        }
    }
}
