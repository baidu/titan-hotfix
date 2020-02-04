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
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.sdk.common.TitanConstant;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 新增方法
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/9/9
 */
public class AddedMethodDiffMarker extends DexCodeVisitor {

    private static final String TAG = "AddedMethodDiffMarker";

    /** 兼容的新增方法-新增<init>方法 */
    public static final int DIFF_ADDED_INCOMPATIBLE_INSTANCE_INIT_METHOD = 1 << 0;
    /** 支持新增抽象方法 */
    public static final int DIFF_ADDED_INCOMPATIBLE_ABSTRACT_METHOD = 1 << 2;
    /** 兼容的新增方法-新增Override方法*/
    public static final int DIFF_ADDED_INCOMPATIBLE_OVERRIDE_METHOD = 1 << 3;
    /** just-in-time加载模式下新增了clinit方法，不兼容*/
    public static final int DIFF_ADDED_INCOMPATIBLE_CLINIT_IN_TIME_POLICY = 1 << 4;
    /** 记录到dexclassnode中的diff flags key*/
    private static final String ADDED_METHOD_DIFF_FLAGS = "added_method_diff_flags";

    private DexClassNode mNewOrgClassNode;

    private DexMethodNode mNewMethodNode;

    private DiffStatus mDiffStatus;

    private int mAddedDiffFlags;
    /** diff context*/
    private DiffContext mDiffContext;

    public AddedMethodDiffMarker(DiffContext diffContext,
                                 DexClassNode newClassNode, DexMethodNode newMethodNode) {
        this.mDiffContext = diffContext;
        this.mNewOrgClassNode = newClassNode;
        this.mNewMethodNode = newMethodNode;
    }

    public DiffStatus diff() {
//        // <clinit>
        if (mNewMethodNode.isStaticInitMethod()
                && mDiffContext.loadPolicy == TitanConstant.PATCH_LOAD_POLICY_JUST_IN_TIME) {
            mAddedDiffFlags |= DIFF_ADDED_INCOMPATIBLE_CLINIT_IN_TIME_POLICY;
        }

        // <init>
        if (mNewMethodNode.isInstanceInitMethod()) {
            mAddedDiffFlags |= DIFF_ADDED_INCOMPATIBLE_INSTANCE_INIT_METHOD;
        }

        // abstract method
        if (mNewMethodNode.accessFlags.containsOneOf(DexAccessFlags.ACC_ABSTRACT)) {
            mAddedDiffFlags |= DIFF_ADDED_INCOMPATIBLE_ABSTRACT_METHOD;
        }

        if (mAddedDiffFlags == 0 && mNewMethodNode.isVirtualMethod()) {
            DexClassNode superClass = this.mNewOrgClassNode;
            DexNamedProtoNode dnp = new DexNamedProtoNode(mNewMethodNode);
            while ((superClass = DexSuperClassHierarchyFiller.getSuperClass(superClass)) != null) {

                List<DexMethodNode> superVirtualMethods = superClass.getMethods().stream()
                        .filter(m -> new DexNamedProtoNode(m).equals(dnp))
                        .collect(Collectors.toList());
                if (superVirtualMethods.size() > 0) {
                    mAddedDiffFlags |= DIFF_ADDED_INCOMPATIBLE_OVERRIDE_METHOD;
                    break;
                }
            }
        }

        if (mNewMethodNode.accessFlags.containsNoneOf(DexAccessFlags.ACC_ABSTRACT)) {
            this.mNewMethodNode.getCode().accept(this);
        }

        if ((mAddedDiffFlags & (DIFF_ADDED_INCOMPATIBLE_CLINIT_IN_TIME_POLICY
                | DIFF_ADDED_INCOMPATIBLE_INSTANCE_INIT_METHOD
                | DIFF_ADDED_INCOMPATIBLE_ABSTRACT_METHOD
                | DIFF_ADDED_INCOMPATIBLE_OVERRIDE_METHOD)) == 0) {
            mDiffStatus = DiffStatus.CHANGED_COMPATIBLE;
            int savedDiffFlags = mNewOrgClassNode.getExtraInfo(ADDED_METHOD_DIFF_FLAGS, 0);
            mNewOrgClassNode.setExtraInfo(ADDED_METHOD_DIFF_FLAGS, savedDiffFlags | mAddedDiffFlags);
        } else {
            mDiffStatus = DiffStatus.CHANGED_INCOMPATIBLE;
        }

        return mDiffStatus;
    }

    /**
     * 是否需要生成class的副本
     *
     * @param dcn dexclassnode
     * @return 是否需要生成class的副本
     */
    static boolean shouldCopyClass(DexClassNode dcn) {
        int addedDiffFlags = dcn.getExtraInfo(ADDED_METHOD_DIFF_FLAGS, 0);
        return shouldCopyClass(addedDiffFlags);
    }

    /**
     * 清空diff flags
     * 
     * @param dcn dexclassnode
     */
    static void clearDiffFlags(DexClassNode dcn) {
        dcn.setExtraInfo(ADDED_METHOD_DIFF_FLAGS, 0);
    }

    /**
     * 是否需要生成class的副本
     *
     * @param addedDiffFlags diff之后记录的flag
     * @return 是否需要生成class的副本
     */
    private static boolean shouldCopyClass(int addedDiffFlags) {
//        return addedDiffFlags != 0 &&
//                (addedDiffFlags & ~(DIFF_ADDED_INCOMPATIBLE_INSTANCE_INIT_METHOD
//                        | DIFF_ADDED_INCOMPATIBLE_ABSTRACT_METHOD
//                        | DIFF_ADDED_INCOMPATIBLE_OVERRIDE_METHOD)) == 0;
        return false;
    }

    public void printDiffStatus(TitanLogger logger) {
        logger.i(TAG, String.format("method %s status: %s",
                new DexNamedProtoNode(mNewMethodNode).toString(),
                mDiffStatus.toString()));
        logger.incIndent();
        if ((mAddedDiffFlags & DIFF_ADDED_INCOMPATIBLE_CLINIT_IN_TIME_POLICY) != 0) {
            logger.i(TAG, "<clinit> added when load policy is just-in-time");
        }

        if ((mAddedDiffFlags & DIFF_ADDED_INCOMPATIBLE_INSTANCE_INIT_METHOD) != 0) {
            logger.i(TAG, "unsupport add <init> method");
        }

        if ((mAddedDiffFlags & DIFF_ADDED_INCOMPATIBLE_ABSTRACT_METHOD) != 0) {
            logger.i(TAG, "unsupport add abstract method");
        }

        if ((mAddedDiffFlags & DIFF_ADDED_INCOMPATIBLE_OVERRIDE_METHOD) != 0) {
            logger.i(TAG, "unsupport add override method");
        }

//        if ((mAddedDiffFlags & DIFF_ADDED_INCOMPATIBLE_STATIC_INIT_METHOD) != 0) {
//            logger.i(TAG, "unsupport add <clinit> method");
//        }

//        if ((mAddedDiffFlags & DIFF_ADDED_COMPATIBLE_INSTANCE_INIT_METHOD) != 0) {
//            logger.i(TAG, "unsupport add <init> method");
//        }



//        int incompatibleChangedMethodCount = mIncompatibleChangedMethods.size();
//        int compatibleChangedMethodCount = mCompatibleChangedMethods.size();
//        int compatibleAddedMethodCount = mCompatibleAddedMethods.size();
//        int incompatibleAddedMethodCount = mInCompatibleAddedMethods.size();
//
//        if (incompatibleChangedMethodCount != 0) {
//            logger.i(TAG, String.format("%d incompatible changed methods",
//                    incompatibleChangedMethodCount));
//            this.mIncompatibleChangedMethods.forEach((method, marker) -> {
//                logger.i(TAG, String.format("method %s :", method.toString()));
//                marker.printDiffStatus(logger);
//            });
//        }
        logger.decIndent();
    }


}
