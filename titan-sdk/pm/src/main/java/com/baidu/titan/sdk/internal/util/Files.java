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

package com.baidu.titan.sdk.internal.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by zhangdi on 2017/5/10.
 */

public class Files {

    public static String getAssetFileContent(Context c, String name) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(c.getAssets().open(name));
            int len;
            char[] buf = new char[10 * 1024];
            CharArrayWriter caw = new CharArrayWriter();
            while ((len = reader.read(buf)) > 0) {
                caw.write(buf, 0, len);
            }
            String content = caw.toString();
            return content;
        } catch (Exception e) {
            // ignore
        } finally {
            Closes.closeQuiet(reader);
        }
        return null;
    }

    public static String getFileStringContent(File f) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(f));
            CharArrayWriter caw = new CharArrayWriter();
            char[] buf = new char[8 * 1024];
            int len;
            while ((len = reader.read(buf)) > 0) {
                caw.write(buf, 0, len);
            }
            return caw.toString();
        } catch (IOException e) {
            // ignore
        } finally {
            Closes.closeQuiet(reader);
        }
        return null;
    }


}
