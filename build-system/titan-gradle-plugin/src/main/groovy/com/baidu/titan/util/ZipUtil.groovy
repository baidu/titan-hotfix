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

package com.baidu.titan.util

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream;

/**
 * Zip工具类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/5
 */
public class ZipUtil {

    public static boolean isEntryExist(File zipPath, String entryName) {
        ZipFile zipFile = null
        try {
            zipFile = new ZipFile(zipPath)
            ZipEntry zipEntry = zipFile.getEntry(entryName);
            return zipEntry != null
        }
        finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (Exception e) {

                }
            }
        }
        return false
    }

    public static byte[] getZipEntryContent(String entryName, File zipPath) {
        ZipFile zipFile = null
        try {
            zipFile = new ZipFile(zipPath)
            ZipEntry zipEntry = zipFile.getEntry(entryName);
            return getZipEntryContent(zipEntry, zipFile)
        }
        finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (Exception e) {

                }
            }
        }
        return null;
    }

    public static byte[] getZipEntryContent(ZipEntry zipEntry, ZipFile zipFile) {
        InputStream is = null
        try {
            is = zipFile.getInputStream(zipEntry)
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            byte[] buf = new byte[16 * 1024]
            int len
            while ((len = is.read(buf)) > 0) {
                baos.write(buf, 0, len)
            }
            return baos.toByteArray();

        } finally {
            if (is != null) {
                try {
                    is.close()
                } catch (Exception e) {
                }
            }
        }
    }




}