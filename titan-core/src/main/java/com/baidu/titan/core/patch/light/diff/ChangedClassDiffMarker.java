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

import com.baidu.titan.core.Constant;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.core.util.ListDiffer;
import com.baidu.titan.core.util.TitanLogger;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexFileVersion;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.SmaliWriter;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedFieldProtoNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexAnnotationVisitor;
import com.baidu.titan.dex.visitor.DexAnnotationVisitorInfo;
import com.baidu.titan.dex.visitor.DexClassNodeVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitorInfo;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;
import com.baidu.titan.dex.writer.DexFileWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * 比较类属性以及成员的差异
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/5/1
 */
public class ChangedClassDiffMarker implements DexClassNodeVisitor {

    private static final String TAG = "ChangedClassDiffMarker";

    private static final String EXTRA_KEY_FIELD_DIFF_MODE = "_extra_field_diff_mode";

    private static final String EXTRA_KEY_METHOD_DIFF_MODE = "_extra_method_diff_mode";

    private DexClassNode mOldOrgClass;

    private DexClassNode mNewOrgClass;

    private DiffContext mDiffContext;

    private int mVisitMode;

    private boolean mIgnoreDebugInfo;

    private static final int VISIT_MODE_NEW_CLASS_NODE = 0;

    private static final int VISIT_MODE_OLD_CLASS_NODE = 1;

    public DiffStatus mDiffStatus;

    private int mDiffFlags;

    public static final int DIFF_CHANGED_INCOMPATIBLE_CLASS_SUPER = 1 << 0;

    public static final int DIFF_CHANGED_INCOMPATIBLE_CLASS_INTERFACES = 1 << 2;

    public static final int DIFF_CHANGED_INCOMPATIBLE_CLASS_ANNOTATIONS = 1 << 5;

    public static final int DIFF_CHANGED_COMPATIBLE_CLASS_METHOD = 1 << 6;

    public static final int DIFF_CHANGED_INCOMPATIBLE_CLASS_NOT_PUBLIC = 1 << 7;

    private Map<DexNamedProtoNode, ChangedMethodDiffMarker> mIncompatibleChangedMethods
            = new HashMap<>();

    private Map<DexNamedProtoNode, ChangedMethodDiffMarker> mCompatibleChangedMethods
            = new HashMap<>();

    private Map<DexNamedProtoNode, AddedMethodDiffMarker> mInCompatibleAddedMethods
            = new HashMap<>();

    private Map<DexNamedProtoNode, AddedMethodDiffMarker> mCompatibleAddedMethods
            = new HashMap<>();

    private Map<DexNamedProtoNode, RemovedMethodDiffMarker> mCompatibleRemovedMethods
            = new HashMap<>();

    private Map<DexNamedFieldProtoNode, AddedFieldDiffMarker> mIncompatibleAddedFields
            = new HashMap<>();

    private Map<DexNamedFieldProtoNode, AddedFieldDiffMarker> mCompatibleAddedFields
            = new HashMap<>();

    private Map<DexNamedFieldProtoNode, ChangedFieldDiffMarker> mIncompatibleChangedFields
            = new HashMap<>();

    private Map<DexNamedFieldProtoNode, ChangedFieldDiffMarker> mCompatibleChangedFields
            = new HashMap<>();

    private Set<DexMethodNode> mChangedInitMethods = new HashSet<>();

    public ChangedClassDiffMarker(DiffContext diffContext,
                                  DexClassNode newClass, DexClassNode oldClass,
                                  boolean ignoreDebugInfo) {
        this.mNewOrgClass = newClass;
        this.mOldOrgClass = oldClass;
        this.mIgnoreDebugInfo = ignoreDebugInfo;
        this.mDiffContext = diffContext;
    }

    private static boolean compareSuperType(DexClassNode oldClass, DexClassNode newClass) {
        if (!oldClass.superType.equals(newClass.superType)) {
            return false;
        }
        return true;
    }

    private static boolean compareInterfaces(DexClassNode oldClass, DexClassNode newClass) {

        int interfaceDiff = new ListDiffer<DexType>(
                Arrays.asList(oldClass.interfaces.types()),
                Arrays.asList(newClass.interfaces.types())) {

            @Override
            protected boolean canCompare(DexType first, DexType second) {
                return areEqual(first, second);
            }

            @Override
            protected boolean areEqual(DexType first, DexType second) {
                return first.equals(second);
            }

        }.diff();

        return interfaceDiff == ListDiffer.DIFF_NONE;
    }

    // TODO
    private static boolean compareClassAnnotations(DexClassNode oldClass, DexClassNode newClass) {

        int annotationDiff = new ListDiffer<DexAnnotationNode>(
                oldClass.getClassAnnotations(),
                newClass.getClassAnnotations()) {

            @Override
            protected boolean canCompare(DexAnnotationNode first, DexAnnotationNode second) {
                return false;
            }

            @Override
            protected boolean areEqual(DexAnnotationNode first, DexAnnotationNode second) {
                return false;
            }
        }.diff();

        return annotationDiff == ListDiffer.DIFF_NONE;
    }

    private static boolean diffQuickCompare(DexClassNode oldClass, DexClassNode newClass,
                                         boolean ignoreDebugInfo) {
        byte[] oldClassBytes = getClassNodeBytes(oldClass, ignoreDebugInfo);
        byte[] newClassBytes = getClassNodeBytes(newClass, ignoreDebugInfo);

        return Arrays.equals(oldClassBytes, newClassBytes);
    }

    public DiffStatus diff() {
        boolean unchanged = diffQuickCompare(mNewOrgClass, mOldOrgClass, mIgnoreDebugInfo);
        if (unchanged) {
            mDiffStatus = DiffStatus.UNCHANGED;
            markAllUnchanged();
            return this.mDiffStatus;
        }

        DexAccessFlags instramentAccessFlags = mOldOrgClass
                .getExtraInfo(Constant.EXTRA_KEY_INSTRUMENT_ACCESS_FLAGS, null);
        if (instramentAccessFlags != null && instramentAccessFlags.containsNoneOf(DexAccessFlags.ACC_PUBLIC)) {
            mDiffFlags = DIFF_CHANGED_INCOMPATIBLE_CLASS_NOT_PUBLIC;
        }

        // check super type
        if (!compareSuperType(mNewOrgClass, mOldOrgClass)) {
            mDiffFlags |= DIFF_CHANGED_INCOMPATIBLE_CLASS_SUPER;
        }
        // compare interfaces
        if (!compareInterfaces(mNewOrgClass, mOldOrgClass)) {
            mDiffFlags |= DIFF_CHANGED_INCOMPATIBLE_CLASS_INTERFACES;
        }
        // compare class annotation
        if (!compareClassAnnotations(mNewOrgClass, mOldOrgClass)) {
            //TODO: 后续处理注解
//            mDiffFlags |= DIFF_CHANGED_INCOMPATIBLE_CLASS_ANNOTATIONS;
        }

        AddedMethodDiffMarker.clearDiffFlags(mNewOrgClass);
        ChangedMethodDiffMarker.clearDiffFlags(mNewOrgClass);
        RemovedMethodDiffMarker.clearDiffFlags(mOldOrgClass);

        // compare members
        this.mVisitMode = VISIT_MODE_NEW_CLASS_NODE;
        this.mNewOrgClass.accept(this);
        this.mVisitMode = VISIT_MODE_OLD_CLASS_NODE;
        this.mOldOrgClass.accept(this);


        if (AddedMethodDiffMarker.shouldCopyClass(mNewOrgClass)
                || ChangedMethodDiffMarker.shouldCopyClass(mNewOrgClass)
                || RemovedMethodDiffMarker.shouldCopyClass(mOldOrgClass)) {
            mDiffFlags |= DIFF_CHANGED_COMPATIBLE_CLASS_METHOD;
        }

        if (isIncompatiable()) {
            mDiffStatus = DiffStatus.CHANGED_INCOMPATIBLE;
        } else {
            if (mIncompatibleChangedMethods.size() > 0
                    || mInCompatibleAddedMethods.size() > 0) {
                mDiffStatus = DiffStatus.CHANGED_INCOMPATIBLE;
            } else if (mCompatibleAddedMethods.isEmpty() && mCompatibleAddedFields.isEmpty()
                    && mCompatibleChangedFields.isEmpty() && mCompatibleChangedMethods.isEmpty()){
                mDiffStatus = DiffStatus.UNCHANGED;
            } else {
                mDiffStatus = DiffStatus.CHANGED_COMPATIBLE;
            }
        }
        return mDiffStatus;
    }

    /**
     * 是否有不兼容的修改
     *
     * @return
     */
    private boolean isIncompatiable() {
        return (mDiffFlags & (DIFF_CHANGED_INCOMPATIBLE_CLASS_SUPER
                | DIFF_CHANGED_INCOMPATIBLE_CLASS_INTERFACES
                | DIFF_CHANGED_INCOMPATIBLE_CLASS_NOT_PUBLIC
                | DIFF_CHANGED_INCOMPATIBLE_CLASS_ANNOTATIONS)) != 0;
    }

    public boolean shouldCopyClass() {
        if (isIncompatiable()) {
            return false;
        }
        if (mDiffFlags != 0 &&
                (mDiffFlags & ~DIFF_CHANGED_COMPATIBLE_CLASS_METHOD) == 0) {
            return true;
        }
        return false;
    }

    private void markAllUnchanged() {
        mNewOrgClass.getMethods()
                .forEach(dmn -> getMethodDiffMode(dmn).markUnChanged());
        mOldOrgClass.getMethods()
                .forEach(dmn -> getMethodDiffMode(dmn).markUnChanged());

        mNewOrgClass.getFields()
                .forEach(dfn -> getFieldDiffMode(dfn).markUnChanged());
        mOldOrgClass.getFields()
                .forEach(dfn -> getFieldDiffMode(dfn).markUnChanged());
    }

    public void printDiffStatus(TitanLogger logger) {
        logger.i(TAG, String.format(".class %s status: %s",
                mNewOrgClass.type.toTypeDescriptor(), mDiffStatus.toString()));

        if ((mDiffFlags & DIFF_CHANGED_INCOMPATIBLE_CLASS_SUPER) != 0) {
            logger.i(TAG, "super type changed");
        }

        if ((mDiffFlags & DIFF_CHANGED_INCOMPATIBLE_CLASS_INTERFACES) != 0) {
            logger.i(TAG, "interface list changed");
        }

        // TODO annotations

        int incompatibleChangedMethodCount = mIncompatibleChangedMethods.size();
        int compatibleChangedMethodCount = mCompatibleChangedMethods.size();
        int compatibleAddedMethodCount = mCompatibleAddedMethods.size();
        int incompatibleAddedMethodCount = mInCompatibleAddedMethods.size();
        int compatibleRemovedMethodCount = mCompatibleRemovedMethods.size();

        logger.incIndent();
        if (incompatibleChangedMethodCount != 0) {
            logger.i(TAG, String.format("%d incompatible changed methods",
                    incompatibleChangedMethodCount));
            this.mIncompatibleChangedMethods.forEach((method, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        if (compatibleChangedMethodCount != 0) {
            logger.i(TAG, String.format("%d compatible changed methods",
                    compatibleChangedMethodCount));
            this.mCompatibleChangedMethods.forEach((method, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        if (incompatibleAddedMethodCount != 0) {
            logger.i(TAG, String.format("%d incompatible added methods",
                    incompatibleAddedMethodCount));
            this.mInCompatibleAddedMethods.forEach((method, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        if (compatibleAddedMethodCount != 0) {
            logger.i(TAG, String.format("%d compatible added methods",
                    compatibleAddedMethodCount));
            this.mCompatibleAddedMethods.forEach((method, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }

        if (compatibleRemovedMethodCount != 0) {
            logger.i(TAG, String.format("%d incompatible removed methods",
                    compatibleRemovedMethodCount));
            this.mCompatibleRemovedMethods.forEach((method, marker) -> {
                logger.incIndent();
                marker.printDiffStatus(logger);
                logger.decIndent();
            });
        }
        logger.decIndent();

    }

    public static DiffMode getFieldDiffMode(DexFieldNode dfn) {
        DiffMode diffMode = dfn.getExtraInfo(EXTRA_KEY_FIELD_DIFF_MODE, null);
        if (diffMode == null) {
            diffMode = new DiffMode();
            dfn.setExtraInfo(EXTRA_KEY_FIELD_DIFF_MODE, diffMode);
        }
        return diffMode;
    }

    public static DiffMode getMethodDiffMode(DexMethodNode dmn) {
        DiffMode diffMode = dmn.getExtraInfo(EXTRA_KEY_METHOD_DIFF_MODE, null);
        if (diffMode == null) {
            diffMode = new DiffMode();
            dmn.setExtraInfo(EXTRA_KEY_METHOD_DIFF_MODE, diffMode);
        }
        return diffMode;
    }

    // *****dex class visit methods begin********

    @Override
    public void visitClassAnnotation(DexAnnotationNode dan) {

    }

    @Override
    public void visitMethod(DexMethodNode dmn) {
        switch (mVisitMode) {
            case VISIT_MODE_NEW_CLASS_NODE: {
                visitMethodNodeFromNewClass(dmn);
                break;
            }
            case VISIT_MODE_OLD_CLASS_NODE: {
                visitMethodNodeFromOldClass(dmn);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void visitMethodNodeFromNewClass(DexMethodNode newMethodNode) {
        DexNamedProtoNode dnp = new DexNamedProtoNode(newMethodNode);
        List<DexMethodNode> oldMethodNodes = this.mOldOrgClass.getMethods().stream()
                .filter(m -> new DexNamedProtoNode(m).equals(dnp))
                .collect(Collectors.toList());

        DexMethodNode oldMethodNode = oldMethodNodes.size() > 0 ? oldMethodNodes.get(0) : null;

        if (oldMethodNode == null) {
            // new added method
            getMethodDiffMode(newMethodNode).markAdded();
            AddedMethodDiffMarker addedMethodDiffMarker =
                    new AddedMethodDiffMarker(mDiffContext, mNewOrgClass, newMethodNode);
            DiffStatus addedMethodDiff = addedMethodDiffMarker.diff();
            switch (addedMethodDiff) {
                case CHANGED_COMPATIBLE: {
                    mCompatibleAddedMethods.put(new DexNamedProtoNode(newMethodNode),
                            addedMethodDiffMarker);
                    break;
                }
                case CHANGED_INCOMPATIBLE: {
                    mInCompatibleAddedMethods.put(new DexNamedProtoNode(newMethodNode),
                            addedMethodDiffMarker);
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }

        } else {
            ChangedMethodDiffMarker changedMethodDiffMarker =
                    new ChangedMethodDiffMarker(mDiffContext, mNewOrgClass, mOldOrgClass, newMethodNode,
                            oldMethodNode);

            DiffStatus changeDiff = changedMethodDiffMarker.diff();

            switch (changeDiff) {
                case CHANGED_COMPATIBLE: {
                    mCompatibleChangedMethods.put(new DexNamedProtoNode(newMethodNode),
                            changedMethodDiffMarker);
                    if (newMethodNode.isInstanceInitMethod() || newMethodNode.isStaticInitMethod()) {
                        mChangedInitMethods.add(newMethodNode);
                    }
                    getMethodDiffMode(newMethodNode).markChanged();
                    getMethodDiffMode(oldMethodNode).markChanged();
                    break;
                }
                case UNCHANGED: {
                    getMethodDiffMode(newMethodNode).markUnChanged();
                    getMethodDiffMode(oldMethodNode).markUnChanged();
                    break;
                }
                case CHANGED_INCOMPATIBLE: {
                    mIncompatibleChangedMethods.put(new DexNamedProtoNode(newMethodNode),
                            changedMethodDiffMarker);
                    getMethodDiffMode(newMethodNode).markChanged();
                    getMethodDiffMode(oldMethodNode).markChanged();
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private void visitMethodNodeFromOldClass(DexMethodNode oldMethodNode) {
        DiffMode oldMethodDiffMode = getMethodDiffMode(oldMethodNode);
        if (!oldMethodDiffMode.isUnChanged() && !oldMethodDiffMode.isChanged()) {
            oldMethodDiffMode.markRemoved();
            RemovedMethodDiffMarker removedMethodDiffMarker =
                    new RemovedMethodDiffMarker(mDiffContext, mOldOrgClass, oldMethodNode);
            DiffStatus removedMethodDiff = removedMethodDiffMarker.diff();
            switch (removedMethodDiff) {
                case CHANGED_COMPATIBLE:
                    mCompatibleRemovedMethods.put(new DexNamedProtoNode(oldMethodNode),
                            removedMethodDiffMarker);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void visitField(DexFieldNode dfn) {
        switch (mVisitMode) {
            case VISIT_MODE_NEW_CLASS_NODE: {
                visitNewClassFieldNode(dfn);
                break;
            }
            case VISIT_MODE_OLD_CLASS_NODE: {
                visitOldClassFieldNode(dfn);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void visitNewClassFieldNode(DexFieldNode newFieldNode) {
        List<DexFieldNode> oldFieldNodes = this.mOldOrgClass.getFields().stream()
                .filter(f -> newFieldNode.name.equals(f.name) && newFieldNode.type.equals(f.type))
                .collect(Collectors.toList());

        DexFieldNode oldFieldNode = oldFieldNodes.isEmpty() ? null : oldFieldNodes.get(0);

        if (oldFieldNode == null) {
            // new add field
            getFieldDiffMode(newFieldNode).markAdded();

            AddedFieldDiffMarker addedFieldDiff =
                    new AddedFieldDiffMarker(this.mNewOrgClass, newFieldNode);
            DiffStatus addedFieldDiffStatus = addedFieldDiff.diff();
            switch (addedFieldDiffStatus) {
                case CHANGED_COMPATIBLE: {
                    mCompatibleAddedFields.put(
                            new DexNamedFieldProtoNode(newFieldNode), addedFieldDiff);
                    break;
                }
                case CHANGED_INCOMPATIBLE: {
                    mIncompatibleAddedFields.put(
                            new DexNamedFieldProtoNode(newFieldNode), addedFieldDiff);
                    break;
                }
                default:
                    break;
            }

        } else {
            // same name & field descriptor
            int fieldDiff = FieldDiffMarker.diff(mNewOrgClass, mOldOrgClass, newFieldNode, oldFieldNode);
            switch (fieldDiff) {
                case FieldDiffMarker.DIFF_UNCHANGED: {
                    getFieldDiffMode(newFieldNode).markUnChanged();
                    getFieldDiffMode(oldFieldNode).markUnChanged();
                    break;
                }
                case FieldDiffMarker.DIFF_CHANGED_COMPATIBLE_STATIC_FINAL_VALUE_CHANGED: {
                    if (mDiffContext.isSupportFinalFieldChange()) {
                        getFieldDiffMode(newFieldNode).markChanged(DiffMode.REASON_FINAL_FIELD_AFFECTED);
                        getFieldDiffMode(oldFieldNode).markChanged(DiffMode.REASON_FINAL_FIELD_AFFECTED);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private void visitOldClassFieldNode(DexFieldNode oldFieldNode) {
        DiffMode oldFieldDiffMode = getFieldDiffMode(oldFieldNode);
        if (!oldFieldDiffMode.isChanged() && !oldFieldDiffMode.isUnChanged()) {
            oldFieldDiffMode.markRemoved();
        }
    }

    @Override
    public void visitClassNodeEnd() {
//        if (mDiffContext.isSupportFinalFieldChange() && mChangedInitMethods.size() > 0) {
//            AffectedFinalFieldMarker marker = new AffectedFinalFieldMarker(mChangedInitMethods,
//                    mNewOrgClass, mOldOrgClass);
//            marker.mark();
//        }
    }



    // *****dex class visit methods end********


    private static byte[] getClassNodeBytes(DexClassNode dcn, boolean ignoreDebugInfo) {
        DexFileWriter dfw = new DexFileWriter();
        dfw.visitBegin();
        dfw.visitDexVersion(DexFileVersion.LATEST_VERSION);
        DexClassVisitor dcv = dfw.visitClass(
                new DexClassVisitorInfo(
                        dcn.type,
                        dcn.superType,
                        dcn.interfaces,
                        new DexAccessFlags(dcn.accessFlags)));

        dcn.accept(new DexClassVisitor(dcv) {

            @Override
            public void visitBegin() {
                super.visitBegin();
            }

            @Override
            public void visitSourceFile(DexString sourceFile) {
                if (!ignoreDebugInfo) {
                    super.visitSourceFile(sourceFile);
                }
            }

            @Override
            public DexAnnotationVisitor visitAnnotation(DexAnnotationVisitorInfo annotationInfo) {
//                return super.visitAnnotation(annotationInfo);
                return null;
            }

            @Override
            public DexFieldVisitor visitField(DexFieldVisitorInfo fieldInfo) {
                DexFieldVisitor delegateDfv =  super.visitField(fieldInfo);
                return new DexFieldVisitor(delegateDfv) {
                    @Override
                    public DexAnnotationVisitor visitAnnotation(DexAnnotationVisitorInfo annotation) {
                        return null;
                    }
                };
            }

            @Override
            public DexMethodVisitor visitMethod(DexMethodVisitorInfo methodInfo) {
                DexMethodVisitor delegateDmv = super.visitMethod(methodInfo);

                return new DexMethodVisitor(delegateDmv) {

                    @Override
                    public void visitBegin() {
                        super.visitBegin();
                    }

                    @Override
                    public DexAnnotationVisitor visitAnnotationDefault() {
                        return super.visitAnnotationDefault();
                    }

                    @Override
                    public DexAnnotationVisitor visitAnnotation(
                            DexAnnotationVisitorInfo annotationInfo) {
//                        return super.visitAnnotation(annotationInfo);
                        return null;
                    }


                    @Override
                    public DexAnnotationVisitor visitParameterAnnotation(int parameter,
                                                         DexAnnotationVisitorInfo annotationInfo) {
                        return super.visitParameterAnnotation(parameter, annotationInfo);
                    }

                    @Override
                    public DexCodeVisitor visitCode() {
                        DexCodeVisitor delegateDcv = super.visitCode();

                        return new DexCodeVisitor(delegateDcv) {

                            @Override
                            public void visitBegin() {
                                super.visitBegin();
                            }

                            @Override
                            public void visitRegisters(int localRegCount, int parameterRegCount) {
                                super.visitRegisters(localRegCount, parameterRegCount);
                            }

                            @Override
                            public void visitTryCatch(DexLabel start, DexLabel end,
                                                      DexTypeList types, DexLabel[] handlers,
                                                      DexLabel catchAllHandler) {
                                super.visitTryCatch(start, end, types, handlers, catchAllHandler);
                            }

                            @Override
                            public void visitLabel(DexLabel label) {
                                super.visitLabel(label);
                            }

                            @Override
                            public void visitConstInsn(
                                    int op, DexRegisterList regs, DexConst dexConst) {
                                if (op == Dops.CONST_STRING) {
                                    op = Dops.CONST_STRING_JUMBO;
                                }
                                super.visitConstInsn(op, regs, dexConst);
                            }

                            @Override
                            public void visitTargetInsn(int op, DexRegisterList regs,
                                                        DexLabel label) {
                                super.visitTargetInsn(op, regs, label);
                            }

                            @Override
                            public void visitSimpleInsn(int op, DexRegisterList regs) {
                                super.visitSimpleInsn(op, regs);
                            }

                            @Override
                            public void visitSwitch(int op, DexRegisterList regs, int[] keys,
                                                    DexLabel[] targets) {
                                super.visitSwitch(op, regs, keys, targets);
                            }

                            @Override
                            public void visitParameters(DexString[] parameters) {
                                if (!ignoreDebugInfo) {
                                    super.visitParameters(parameters);
                                }
                            }

                            @Override
                            public void visitLocal(int reg, DexString name, DexType type,
                                                   DexString signature, DexLabel start,
                                                   DexLabel end) {
                                if (!ignoreDebugInfo) {
                                    super.visitLocal(reg, name, type, signature, start, end);
                                }
                            }

                            @Override
                            public void visitLineNumber(int line, DexLabel start) {
                                if (!ignoreDebugInfo) {
                                    super.visitLineNumber(line, start);
                                }
                            }

                            @Override
                            public void visitEnd() {
                                super.visitEnd();
                            }
                        };
                    }

                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                    }
                };
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
            }
        });

        dfw.visitEnd();

        return dfw.toByteArray();
    }

    /**
     * 将发生修改的类输出到smali文件
     *
     * @param outputDir 要输出的目录
     */
    public void toSmaliFile(File outputDir) throws IOException {
        File oldDir = new File(outputDir, "old");
        File newDir = new File(outputDir, "new");
        oldDir.mkdirs();
        newDir.mkdirs();

        String type = mOldOrgClass.type.toTypeDescriptor();
        String fileName = type.substring(1, type.length() - 1) + ".smali";
        File oldFile = new File(oldDir, fileName);
        File newFile = new File(newDir, fileName);
        oldFile.getParentFile().mkdirs();
        newFile.getParentFile().mkdirs();

        FileWriter fw = null;
        try {
           fw = new FileWriter(oldFile);
           mOldOrgClass.smaliTo(new SmaliWriter(fw));
        } finally {
            PatchUtils.closeQuiet(fw);
        }

        try {
            fw = new FileWriter(newFile);
            mNewOrgClass.smaliTo(new SmaliWriter(fw));
        } finally {
            PatchUtils.closeQuiet(fw);
        }
    }

}
