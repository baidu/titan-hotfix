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

package com.baidu.titan.core.patch.light.plum;

import com.baidu.titan.core.patch.light.diff.ChangedClassDiffMarker;
import com.baidu.titan.core.patch.light.diff.ClassPoolDiffMarker;
import com.baidu.titan.core.patch.light.diff.DiffContext;
import com.baidu.titan.core.patch.light.diff.DiffMode;
import com.baidu.titan.core.patch.light.diff.DiffStatus;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexFileVersion;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexAnnotationVisitor;
import com.baidu.titan.dex.visitor.DexAnnotationVisitorInfo;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitorInfo;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;
import com.baidu.titan.dex.writer.DexFileWriter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对匿名内部类进行对比和标记
 *
 * @author shanghuibo
 * @since 2018/01/27
 */
public class AnonymousClassDiffMarker {

    /** 标记类需要被重写*/
    public static final String ANONYMOUS_CLASS_REWRITE = "anonymous_class_rewrite";
    /** 标记类需要remap*/
    public static final String ANONYMOUS_CLASS_MAP = "anonymous_class_map";
    /** 类需要重命名*/
    public static final String EXTRA_KEY_CLASS_RENAME = "class_rename";

    /**
     * 对Outer class进行对比，递归对比其中的匿名内部类
     *
     * @param diffContext DiffContext
     * @param oldOuterDcn old outer class dcn
     * @param newOuterDcn new outer class dcn
     */
    public void diff(DiffContext diffContext,
                      DexClassNode oldOuterDcn,
                      DexClassNode newOuterDcn) {

        HashSet<DexType> oldAnonyTypes = AnonymousClassMarker.getAnonymousClassType(oldOuterDcn);
        HashSet<DexType> newAnonyTypes = AnonymousClassMarker.getAnonymousClassType(newOuterDcn);

        if (oldAnonyTypes == null) {
            // 原类无匿名内部类，将所有新匿名内部类标记为新增
            if (newAnonyTypes != null) {
                newAnonyTypes.stream()
                        .map(type -> diffContext.newOrgAppPool.findClassFromAll(type))
                        .forEach(dcn -> {
                            ClassPoolDiffMarker.getClassDiffMode(dcn).markAdded();
                            dcn.setExtraInfo(EXTRA_KEY_CLASS_RENAME, true);

                            if (oldOuterDcn != null) {
                                ClassPoolDiffMarker.getClassDiffMode(oldOuterDcn).markChanged();
                                oldOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);
                            }

                            ClassPoolDiffMarker.getClassDiffMode(newOuterDcn).markChanged();
                            newOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);

                            // 递归对两个类的匿名内部类进行diff, oldOuterDcn传空，会走到将所有匿名内部类标记为新增的逻辑
                            diff(diffContext, null, dcn);
                        });
            }
            return;
        }

        if (newAnonyTypes == null) {
            return;
        }

        // 对匿名内部类按类名进行排序
        List<DexClassNode> sortedOldAnonyDcnList = oldAnonyTypes.stream()
                .sorted(Comparator.comparing(DexType::toTypeDescriptor))
                .map(type -> diffContext.oldOrgAppPool.findClassFromAll(type))
                .collect(Collectors.toList());

        List<DexClassNode> sortedNewAnonyDcnList = newAnonyTypes.stream()
                .sorted(Comparator.comparing(DexType::toTypeDescriptor))
                .map(type -> diffContext.newOrgAppPool.findClassFromAll(type))
                .collect(Collectors.toList());

        AnonymousClassNodeCache cache = new AnonymousClassNodeCache();

        // 检查是否有同名且完全一致的类
        sortedOldAnonyDcnList.forEach(dcn -> {
            byte[] dcnBytes = cache.getClassNodeBytes(dcn);
            DexClassNode newAnonyDcn = sortedNewAnonyDcnList.stream()
                    .filter(newDcn -> newDcn.type.equals(dcn.type))
                    .findFirst().orElse(null);
            if (newAnonyDcn != null) {
                byte[] newDcnBytes = cache.getClassNodeBytes(newAnonyDcn);
                if (Arrays.equals(dcnBytes, newDcnBytes)) {
                    ClassPoolDiffMarker.getClassDiffMode(dcn).markUnChanged();
                    ClassPoolDiffMarker.getClassDiffMode(newAnonyDcn).markUnChanged();
                    // 递归对两个类的匿名内部类进行diff
                    diff(diffContext, dcn, newAnonyDcn);
                }
            }
        });

        // 检查是否有名字不同但内容归一之后完全一致的类
        sortedOldAnonyDcnList.stream()
                .filter(dcn -> !ClassPoolDiffMarker.getClassDiffMode(dcn).isUnChanged())
                .sorted(Comparator.comparing(dcn -> dcn.type.toTypeDescriptor()))
                .forEach(dcn -> {
                    // 将old dex class node归一化
                    byte[] normalizedOldDcnBytes = cache.getNormalizedClassNodeBytes(diffContext.oldOrgAppPool,
                            diffContext.dexItemFactory, dcn);

                    for (DexClassNode newDcn : sortedNewAnonyDcnList) {
                        DiffMode diffMode = ClassPoolDiffMarker.getClassDiffMode(newDcn);
                        if (diffMode.isUnChanged() || diffMode.isChanged()) {
                            continue;
                        }
                        // 将 new dex class node归一化
                        byte[] normalizedNewDcnBytes = cache.getNormalizedClassNodeBytes(diffContext.newOrgAppPool,
                                diffContext.dexItemFactory, newDcn);

                        if (Arrays.equals(normalizedOldDcnBytes, normalizedNewDcnBytes)) {
                            ClassPoolDiffMarker.getClassDiffMode(dcn).markChanged();
                            diffMode.markChanged();
                            dcn.setExtraInfo(ANONYMOUS_CLASS_MAP, newDcn);
                            newDcn.setExtraInfo(ANONYMOUS_CLASS_MAP, dcn);

                            ClassPoolDiffMarker.getClassDiffMode(oldOuterDcn).markChanged();
                            ClassPoolDiffMarker.getClassDiffMode(newOuterDcn).markChanged();
                            oldOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);
                            newOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);

                            // 递归对两个类的匿名内部类进行diff
                            diff(diffContext, dcn, newDcn);

                            break;
                        }
                    }
                });

        // 在剩下的类中，筛选是否可以被认为是发生修改的类
        sortedOldAnonyDcnList.stream()
                .filter(dcn -> !ClassPoolDiffMarker.getClassDiffMode(dcn).isUnChanged()
                        && !ClassPoolDiffMarker.getClassDiffMode(dcn).isChanged())
                .forEach(dcn -> {
                    // 将old dex class node归一化
                    DexClassNode normalizedOldDcn = cache.getNormalizedClassNode(diffContext.oldOrgAppPool,
                            diffContext.dexItemFactory, dcn);

                    DexClassNode oldInstrumentClass = diffContext.oldInstrumentAppPool.findClassFromAll(dcn.type);

                    // 检查是否有可以认为是发生变化的类
                    for (DexClassNode newDcn : sortedNewAnonyDcnList) {// 将 new dex class node归一化
                        DiffMode diffMode = ClassPoolDiffMarker.getClassDiffMode(newDcn);
                        if (diffMode.isUnChanged() || diffMode.isChanged()) {
                            continue;
                        }
                        DexClassNode normalizedNewDcn = cache.getNormalizedClassNode(diffContext.newOrgAppPool,
                                diffContext.dexItemFactory, newDcn);

                        // 对比两个归一类的差异
                        ChangedClassDiffMarker changedClassDiffMarker = new ChangedClassDiffMarker(diffContext,
                                normalizedNewDcn, normalizedOldDcn, oldInstrumentClass, true);
                        DiffStatus classDiff = changedClassDiffMarker.diff();
                        if (classDiff == DiffStatus.CHANGED_COMPATIBLE
                                && !changedClassDiffMarker.shouldCopyClass()) {
                            ClassPoolDiffMarker.getClassDiffMode(dcn).markChanged();
                            diffMode.markChanged();
                            dcn.setExtraInfo(ANONYMOUS_CLASS_MAP, newDcn);
                            newDcn.setExtraInfo(ANONYMOUS_CLASS_MAP, dcn);

                            ClassPoolDiffMarker.getClassDiffMode(oldOuterDcn).markChanged();
                            ClassPoolDiffMarker.getClassDiffMode(newOuterDcn).markChanged();
                            oldOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);
                            newOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);

                            diff(diffContext, dcn, newDcn);
                            break;
                        }
                    }
                });

        // 如果new anony dcn list中还有未标记的类，全部认为是新增类
        sortedNewAnonyDcnList.stream()
                .filter(dcn -> !ClassPoolDiffMarker.getClassDiffMode(dcn).isUnChanged())
                .filter(dcn -> !ClassPoolDiffMarker.getClassDiffMode(dcn).isChanged())
                .forEach(dcn -> {
                    ClassPoolDiffMarker.getClassDiffMode(dcn).markAdded();
                    dcn.setExtraInfo(EXTRA_KEY_CLASS_RENAME, true);

                    ClassPoolDiffMarker.getClassDiffMode(oldOuterDcn).markChanged();
                    ClassPoolDiffMarker.getClassDiffMode(newOuterDcn).markChanged();
                    oldOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);
                    newOuterDcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);

                    // 递归对两个类的匿名内部类进行diff, oldOuterDcn传空，会走到将所有匿名内部类标记为新增的逻辑
                    diff(diffContext, null, dcn);
                });
    }

    /**
     * 将dexclass node 转为字节，用于进行快速对比
     *
     * @param dcn dexclassnode
     * @param ignoreDebugInfo 是否忽略debug info
     * @return class的字节数据
     */
    static byte[] getClassNodeBytes(DexClassNode dcn, boolean ignoreDebugInfo) {
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
                return super.visitAnnotation(annotationInfo);
            }

            @Override
            public DexFieldVisitor visitField(DexFieldVisitorInfo fieldInfo) {
                return super.visitField(fieldInfo);
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
                        return super.visitAnnotation(annotationInfo);
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

    public static DexType getRewriteType(DiffContext diffContext, DexClassNode dcn) {
        if (shouldRename(dcn)) {
            return getClassRenameType(diffContext.dexItemFactory, dcn);
        }

        if (hasMapClass(dcn)) {
            return getClassMapType(dcn);
        }

        return dcn.type;
    }

    public static DexType getRewriteType(DiffContext diffContext, DexType type) {
        DexClassNode dcn = diffContext.newOrgAppPool.findClassFromAll(type);
        if (dcn == null) {
            return type;
        }
        return getRewriteType(diffContext, dcn);
    }

    private static DexType getClassMapType(DexClassNode dcn) {
        DexClassNode mapDcn = dcn.getExtraInfo(ANONYMOUS_CLASS_MAP, dcn);
        return mapDcn.type;
    }

    private static DexType getClassMapType(ApplicationDexPool classPool, DexType type) {
        DexClassNode dcn = classPool.findClassFromAll(type);
        DexClassNode mapDcn = dcn.getExtraInfo(ANONYMOUS_CLASS_MAP, dcn);
        return mapDcn.type;
    }

    public static boolean shouldRewrite(DexClassNode outerDcn) {
        return outerDcn.getExtraInfo(ANONYMOUS_CLASS_REWRITE, false);
    }

    public static boolean shouldRename(DexClassNode dcn) {
        return dcn.getExtraInfo(EXTRA_KEY_CLASS_RENAME, false);
    }

    public static boolean hasMapClass(DexClassNode dcn) {
        return dcn.getExtraInfo(ANONYMOUS_CLASS_MAP, null) != null;
    }

    public static void markShouldRewrite(DexClassNode dcn) {
        dcn.setExtraInfo(ANONYMOUS_CLASS_REWRITE, true);
    }

    private static DexType getClassRenameType(DexItemFactory dexItemFactory, DexClassNode dcn) {
        return getClassRenameType(dexItemFactory, dcn.type);
    }

    private static DexType getClassRenameType(DexItemFactory dexItemFactory, DexType type) {
        return dexItemFactory.createType(type.toTypeDescriptor().replace(";", "$copy;"));
    }
}
