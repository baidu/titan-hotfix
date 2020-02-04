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

import com.baidu.titan.core.instrument.DisableInterceptCollector;
import com.baidu.titan.core.instrument.DisableInterceptMarker;
import com.baidu.titan.core.patch.light.plum.AnonymousClassDiffMarker;
import com.baidu.titan.core.patch.light.plum.AnonymousClassMarker;
import com.baidu.titan.core.patch.light.plum.ClassRewriteChecker;
import com.baidu.titan.core.patch.light.plum.PlumPlanter;
import com.baidu.titan.core.util.TitanLogger;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * 用于比较两个DexPool差异化
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/4/30
 */
public class ClassPoolDiffMarker implements DexClassPoolNodeVisitor {

    private static final String TAG = "ClassPoolDiff";

    private static final String EXTRA_KEY_METHOD_DIFF_MODE = "_extra_method_diff_mode";

    private static final String EXTRA_KEY_REWRITE_CLASS = "rewrite_class";

    private DiffContext mDiffContext;

    private static final int VISIT_MODE_NEW_CLASS_POOL = 1;

    private static final int VISIT_MODE_OLD_CLASS_POOL = 2;

    private static final int VISIT_MODE_NEW_CLASS_POOL_CHECK_FINAL_FIELD = 3;

    private static final int VISIT_MODE_NEW_CLASS_POOL_REWRITE_CLASS= 4;

    private static final int VISIT_MODE_REWRITE_CLASS_POOL= 5;


    private int mVisitMode;

    private DiffStatus mDiffStatus;

    private Map<DexType, ChangedClassDiffMarker> mCompatibleChangedClasses = new HashMap<>();

    private Map<DexType, ChangedClassDiffMarker> mInCompatibleChangedClasses = new HashMap<>();

    private Map<DexType, AddedClassDiffMarker> mCompatibleAddedClasses = new HashMap<>();

    private Map<DexType, AddedClassDiffMarker> mIncompatibleAddedClasses = new HashMap<>();

    private Map<DexType, DexClassNode> mRemovedClasses = new HashMap<>();

    public ClassPoolDiffMarker(DiffContext diffContext) {
        this.mDiffContext = diffContext;
    }

    public DiffStatus diff() {
        if (mDiffStatus != null) {
            return mDiffStatus;
        }

        // collect package-info annotations
        DisableInterceptCollector disableCollector = new DisableInterceptCollector(this.mDiffContext.dexItemFactory);
        this.mDiffContext.newOrgAppPool.acceptProgram(disableCollector);
        // mark disable intercept flag
        // TODO: 增加对filter的支持
        this.mDiffContext.newOrgAppPool.acceptProgram(new DisableInterceptMarker(disableCollector,
                null, this.mDiffContext.dexItemFactory));

        AnonymousClassMarker oldMarker = new AnonymousClassMarker(this.mDiffContext.oldOrgAppPool,
                this.mDiffContext.dexItemFactory);
        oldMarker.mark();

        AnonymousClassMarker newMarker = new AnonymousClassMarker(this.mDiffContext.newOrgAppPool,
                this.mDiffContext.dexItemFactory);
        newMarker.mark();

        this.mVisitMode = VISIT_MODE_NEW_CLASS_POOL;
        this.mDiffContext.newOrgAppPool.getProgramClassPool().accept(this);

        if (mDiffContext.isSupportFinalFieldChange()) {
            this.mVisitMode = VISIT_MODE_NEW_CLASS_POOL_CHECK_FINAL_FIELD;
            this.mDiffContext.newOrgAppPool.getProgramClassPool().accept(this);
        }

        this.mVisitMode = VISIT_MODE_OLD_CLASS_POOL;
        this.mDiffContext.oldOrgAppPool.getProgramClassPool().accept(this);

        this.mVisitMode = VISIT_MODE_NEW_CLASS_POOL_REWRITE_CLASS;
        this.mDiffContext.newOrgAppPool.getProgramClassPool().accept(this);

        this.mVisitMode = VISIT_MODE_REWRITE_CLASS_POOL;
        this.mDiffContext.rewriteClassPool.getProgramClassPool().accept(this);

        int incompatibleChangedClassCount = this.mInCompatibleChangedClasses.size();
        int compatibleChangedClassCount = this.mCompatibleChangedClasses.size();
        int incompatibleAddedClassCount = this.mIncompatibleAddedClasses.size();
        int compatibleAddedClassCount = this.mCompatibleAddedClasses.size();


        int removedClassCount = this.mRemovedClasses.size();

        if ((incompatibleChangedClassCount + compatibleChangedClassCount
                + incompatibleAddedClassCount + compatibleAddedClassCount) == 0) {
            mDiffStatus = DiffStatus.UNCHANGED;
        } else if (incompatibleChangedClassCount != 0) {
            mDiffStatus = DiffStatus.CHANGED_INCOMPATIBLE;
        } else {
            mDiffStatus = DiffStatus.CHANGED_COMPATIBLE;
        }
        return mDiffStatus;
    }

    public void printDiffStatus(TitanLogger logger) {
        logger.i(TAG, String.format(".classpool status: %s", mDiffStatus.toString()));

        int incompatibleChangedClassCount = this.mInCompatibleChangedClasses.size();
        int compatibleChangedClassCount = this.mCompatibleChangedClasses.size();

        int incompatibleAddedClassCount = this.mIncompatibleAddedClasses.size();
        int compatibleAddedClassCount = this.mCompatibleAddedClasses.size();

        int removedClassCount = this.mRemovedClasses.size();

        logger.incIndent();
        if (incompatibleChangedClassCount != 0) {
            logger.i(TAG, String.format("%d incompatible changed classes",
                    incompatibleChangedClassCount));
            this.mInCompatibleChangedClasses.forEach((type, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        if (compatibleChangedClassCount != 0) {
            logger.i(TAG, String.format("%d compatible changed classes",
                    compatibleChangedClassCount));
            this.mCompatibleChangedClasses.forEach((type, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        if (incompatibleAddedClassCount != 0) {
            logger.i(TAG, String.format("%d incompatible added classes",
                    incompatibleAddedClassCount));
            this.mIncompatibleAddedClasses.forEach((type, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        if (compatibleAddedClassCount != 0) {
            logger.i(TAG, String.format("%d compatible added classes",
                    compatibleAddedClassCount));
            this.mCompatibleAddedClasses.forEach((type, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        logger.decIndent();

    }

    @Override
    public void visitClass(DexClassNode dcn) {
        switch (mVisitMode) {
            case VISIT_MODE_NEW_CLASS_POOL: {
                visitClassFromNewPool(dcn);
                break;
            }
            case VISIT_MODE_OLD_CLASS_POOL: {
                visitClassFromOldPool(dcn);
                break;
            }
            case VISIT_MODE_NEW_CLASS_POOL_CHECK_FINAL_FIELD: {
                visitClassFromNewPoolCheckFinalField(dcn);
                break;
            }
            case VISIT_MODE_NEW_CLASS_POOL_REWRITE_CLASS: {
                visitClassFromNewPoolAndRewriteClass(dcn);
                break;
            }
            case VISIT_MODE_REWRITE_CLASS_POOL: {
                visitClassFromRewritePool(dcn);
                visitClassFromNewPoolCheckFinalField(dcn);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void visitClassFromNewPoolAndRewriteClass(DexClassNode dcn) {
        if (AnonymousClassDiffMarker.shouldRewrite(dcn)
                || AnonymousClassDiffMarker.shouldRename(dcn)
                || AnonymousClassDiffMarker.hasMapClass(dcn)
                || ClassRewriteChecker.checkRewrite(mDiffContext, dcn)) {
            DexClassNode rewriteClassNode = PlumPlanter.rewrite(mDiffContext, dcn);
            mDiffContext.rewriteClassPool.addProgramClass(rewriteClassNode);
            dcn.setExtraInfo(EXTRA_KEY_REWRITE_CLASS, rewriteClassNode);
        }
    }

    /**
     * 对rewrite的class进行diff，将其中的method进行标记
     *
     * @param rewriteNode 在rewrite class pool 中的class node
     */
    private void visitClassFromRewritePool(DexClassNode rewriteNode) {
        DexClassNode oldOrgNode = mDiffContext.oldOrgAppPool.getProgramClassPool().getClass(rewriteNode.type);
        if (oldOrgNode == null) {
            diffAddedClass(rewriteNode);
        } else {
            diffChangedClass(oldOrgNode, rewriteNode);
        }

    }

    private void visitClassFromNewPoolCheckFinalField(DexClassNode newOrgNode) {
        AffectedFinalFieldMarker.markAccessFinalField(mDiffContext, newOrgNode);
    }

    private void visitClassFromNewPool(DexClassNode newOrgNode) {
        // 匿名内部类不在这里进行diff, 而是在outer class中进行递归的diff
        if (AnonymousClassMarker.isAnonymousClass(mDiffContext.dexItemFactory, newOrgNode)) {
            return;
        }
        DexClassNode oldOrgClass = mDiffContext.oldOrgAppPool.getProgramClassPool().getClass(newOrgNode.type);
        if (oldOrgClass == null) {
            diffAddedClass(newOrgNode);
        } else {
            diffChangedClass(oldOrgClass, newOrgNode);
        }

        // 对匿名内部类进行diff
        AnonymousClassDiffMarker anonyDiffMarker = new AnonymousClassDiffMarker();
        anonyDiffMarker.diff(mDiffContext, oldOrgClass, newOrgNode);
    }

    private void diffAddedClass(DexClassNode newOrgNode) {
        getClassDiffMode(newOrgNode).markAdded();
        AddedClassDiffMarker addedClassDiffMarker = new AddedClassDiffMarker(newOrgNode);
        DiffStatus addedDiff = addedClassDiffMarker.diff();
        switch (addedDiff) {
            case CHANGED_INCOMPATIBLE: {
                mIncompatibleAddedClasses.put(newOrgNode.type, addedClassDiffMarker);
                break;
            }
            case CHANGED_COMPATIBLE: {
                mCompatibleAddedClasses.put(newOrgNode.type, addedClassDiffMarker);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void diffChangedClass(DexClassNode oldOrgClass, DexClassNode newOrgNode) {

        if (mDiffContext.classPatchFilter != null) {
            if (mDiffContext.classPatchFilter.skipPatch(oldOrgClass.type.toTypeDescriptor())) {
                getClassDiffMode(newOrgNode).markUnChanged();
                getClassDiffMode(oldOrgClass).markUnChanged();
                return;
            }
        }

        DexClassNode oldInstrumentClass = mDiffContext.oldInstrumentAppPool.findClassFromAll(oldOrgClass.type);
        ChangedClassDiffMarker changedClassDiffMarker = new ChangedClassDiffMarker(mDiffContext,
                newOrgNode, oldOrgClass, oldInstrumentClass, true);
        DiffStatus classDiff = changedClassDiffMarker.diff();
        switch (classDiff) {
            case UNCHANGED: {
                getClassDiffMode(newOrgNode).markUnChanged();
                getClassDiffMode(oldOrgClass).markUnChanged();
                break;
            }
            case CHANGED_COMPATIBLE: {
                if (changedClassDiffMarker.shouldCopyClass()) {
                    diffAddedClass(newOrgNode);
                    newOrgNode.setExtraInfo(AnonymousClassDiffMarker.EXTRA_KEY_CLASS_RENAME, true);
                } else {
                    getClassDiffMode(newOrgNode).markChanged();
                    getClassDiffMode(oldOrgClass).markChanged();
                    mCompatibleChangedClasses.put(newOrgNode.type, changedClassDiffMarker);
                }
                break;
            }
            case CHANGED_INCOMPATIBLE: {
                mInCompatibleChangedClasses.put(newOrgNode.type, changedClassDiffMarker);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void visitClassFromOldPool(DexClassNode oldOrgNode) {
        DiffMode diffMode = getClassDiffMode(oldOrgNode);
        if (!diffMode.isChanged() && !diffMode.isUnChanged()) {
            diffMode.markRemoved();
            mRemovedClasses.put(oldOrgNode.type, oldOrgNode);
        }
    }

    public static DexClassNode getRewriteClass(DexClassNode dcn) {
        return dcn.getExtraInfo(EXTRA_KEY_REWRITE_CLASS, null);
    }

    public static DiffMode getClassDiffMode(DexClassNode dcn) {
        DiffMode diffMode = dcn.getExtraInfo(EXTRA_KEY_METHOD_DIFF_MODE, null);
        if (diffMode == null) {
            diffMode = new DiffMode();
            dcn.setExtraInfo(EXTRA_KEY_METHOD_DIFF_MODE, diffMode);
        }
        return diffMode;
    }

    @Override
    public void classPoolVisitEnd() {

    }

    /**
     * 将变更文件转换为smali输出
     *
     * @param outputDir smali输出目录
     */
    public void toSmaliFile(File outputDir) throws IOException {
        int incompatibleChangedClassCount = this.mInCompatibleChangedClasses.size();
        int compatibleChangedClassCount = this.mCompatibleChangedClasses.size();

        int incompatibleAddedClassCount = this.mIncompatibleAddedClasses.size();
        int compatibleAddedClassCount = this.mCompatibleAddedClasses.size();

        if (incompatibleChangedClassCount != 0) {
            for (ChangedClassDiffMarker marker : mInCompatibleChangedClasses.values()) {
                marker.toSmaliFile(outputDir);
            }
        }

        if (compatibleChangedClassCount != 0) {
            for (ChangedClassDiffMarker marker : mCompatibleChangedClasses.values()) {
                marker.toSmaliFile(outputDir);
            }
        }

        if (incompatibleAddedClassCount != 0) {
            for (AddedClassDiffMarker marker : mIncompatibleAddedClasses.values()) {
                marker.toSmaliFile(outputDir);
            }
        }

        if (compatibleAddedClassCount != 0) {
            for (AddedClassDiffMarker marker : mCompatibleAddedClasses.values()) {
                marker.toSmaliFile(outputDir);
            }
        }

    }

}
