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

package com.baidu.titan.core.patch.light.diff;

import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.util.TitanLogger;
import com.baidu.titan.dex.SmaliWriter;
import com.baidu.titan.dex.node.DexClassNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/10/19
 */
public class AddedClassDiffMarker {

    private static final String TAG = "AddedClassDiffMarker";

    private DexClassNode mNewOrgClassNode;

    private DiffStatus mDiffStatus;

    public AddedClassDiffMarker(DexClassNode newOrgClassNode) {
        this.mNewOrgClassNode = newOrgClassNode;
    }

    public DiffStatus diff() {
        if (mDiffStatus != null) {
            return mDiffStatus;
        }

        mDiffStatus = DiffStatus.CHANGED_COMPATIBLE;

        return mDiffStatus;
    }

    public void printDiffStatus(TitanLogger logger) {
        logger.i(TAG, String.format(".class %s status: %s",
                mNewOrgClassNode.type.toTypeDescriptor(), mDiffStatus.toString()));
    }

    /**
     * 将新增的类输出到smali文件
     *
     * @param outputDir 要输出的目录
     */
    public void toSmaliFile(File outputDir) throws IOException {
        File newDir = new File(outputDir, "new");
        newDir.mkdirs();

        String type = mNewOrgClassNode.type.toTypeDescriptor();
        String fileName = type.substring(1, type.length() - 1) + ".smali";
        File newFile = new File(newDir, fileName);
        newFile.getParentFile().mkdirs();

        FileWriter fw = null;
        try {
            fw = new FileWriter(newFile);
            mNewOrgClassNode.smaliTo(new SmaliWriter(fw));
        } finally {
            PatchUtils.closeQuiet(fw);
        }
    }

}
