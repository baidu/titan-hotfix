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

import com.baidu.titan.core.util.TitanLogger;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 对移除的方法进行标记，如果是virtual method且为Override方法，移除操作无法生效，标记为不兼容
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/9/9
 */
public class RemovedMethodDiffMarker {
    private static final String TAG = "AddedMethodDiffMarker";
    public static final String REMOVED_METHOD_DIFF_FLAGS = "removed_method_diff_flags";
    private final DexClassNode mOldOrgClassNode;
    private final DexMethodNode mOldMethodNode;
    private final DiffContext mDiffContext;
    private DiffStatus mDiffStatus = DiffStatus.UNCHANGED;
    private int mDiffStatusFlags;

    public static final int DIFF_REMOVED_INCOMPATIBLE_VIRTUAL = 1;

    public RemovedMethodDiffMarker(DiffContext diffContext, DexClassNode oldClassNode, DexMethodNode oldMethodNode) {
        this.mOldOrgClassNode = oldClassNode;
        this.mOldMethodNode = oldMethodNode;
        this.mDiffContext = diffContext;
    }


    public DiffStatus diff() {
        if (mOldMethodNode.isVirtualMethod()) {
            DexClassNode superClass = this.mOldOrgClassNode;
            DexNamedProtoNode dnp = new DexNamedProtoNode(mOldMethodNode);
            while ((superClass = DexSuperClassHierarchyFiller.getSuperClass(superClass)) != null) {

                List<DexMethodNode> superVirtualMethods = superClass.getMethods().stream()
                        .filter(m -> new DexNamedProtoNode(m).equals(dnp))
                        .collect(Collectors.toList());
                if (superVirtualMethods.size() > 0) {
                    mDiffStatusFlags |= DIFF_REMOVED_INCOMPATIBLE_VIRTUAL;
                }
            }
        }

        int savedDiffFlags = mOldOrgClassNode.getExtraInfo(REMOVED_METHOD_DIFF_FLAGS, 0);
        mOldOrgClassNode.setExtraInfo(REMOVED_METHOD_DIFF_FLAGS, savedDiffFlags | mDiffStatusFlags);

        if ((mDiffStatusFlags & DIFF_REMOVED_INCOMPATIBLE_VIRTUAL) != 0) {
            return DiffStatus.CHANGED_INCOMPATIBLE;
        }
        return DiffStatus.CHANGED_COMPATIBLE;
    }

    public void printDiffStatus(TitanLogger logger) {
        logger.i(TAG, String.format("method %s status: %s",
                new DexNamedProtoNode(mOldMethodNode).toString(),
                mDiffStatus.toString()));
    }

    /**
     * 是否需要生成class的副本
     *
     * @param dcn dexclassnode
     * @return 是否需要生成class的副本
     */
    static boolean shouldCopyClass(DexClassNode dcn) {
        int removedDiffFlags = dcn.getExtraInfo(REMOVED_METHOD_DIFF_FLAGS, 0);
        return shouldCopyClass(removedDiffFlags);
    }

    /**
     * 是否需要生成class的副本
     *
     * @param diffStatusFlags diff之后记录的flag
     * @return 是否需要生成class的副本
     */
    private static boolean shouldCopyClass(int diffStatusFlags) {
//        return diffStatusFlags != 0 &&
//                (diffStatusFlags & ~DIFF_REMOVED_COMPATIBLE_VIRTUAL) == 0;
        return false;
    }

    /**
     * 清空diff flags
     *
     * @param dcn dexclassnode
     */
    static void clearDiffFlags(DexClassNode dcn) {
        dcn.setExtraInfo(REMOVED_METHOD_DIFF_FLAGS, 0);
    }
}
