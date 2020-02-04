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

package com.baidu.titan.apktool.tests;

import com.baidu.titan.apktool.Apktool;

import org.junit.Test;

import java.io.File;

/**
 * 对apk tool进行测试
 *
 * @author zhangdi
 * @since 2017/8/23
 */

public class ApktoolTest {

    @Test
    public void decodeApk() {
        Apktool.decodeApk(new File("D:\\titan\\apktool\\org.apk"),
                new File("D:\\titan\\apktool\\output2"), Apktool.FLAG_NO_SOURCE);
    }
}
