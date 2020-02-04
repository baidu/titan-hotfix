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

import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.linker.ClassLinker;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexCodeNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.insn.DexConstInsnNode;
import com.baidu.titan.dex.visitor.DexCodeVisitor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 用于标记因<init>方法被修改而受到影响的final字段
 *
 * @author shanghuibo
 * @since 2018/12/26
 */
class AffectedFinalFieldMarker extends DexCodeVisitor {
    private DexClassNode mNewClassNode;
    private DexClassNode mOldClassNode;
    private List<DexFieldNode> mFinalFieldList;
    private Set<DexMethodNode> mChangedOrAddedInitMethods;

    AffectedFinalFieldMarker(Set<DexMethodNode> changedOrAddedInitMethods, DexClassNode newClassNode, DexClassNode oldClassNode) {
        mNewClassNode = newClassNode;
        mOldClassNode = oldClassNode;
        mChangedOrAddedInitMethods = changedOrAddedInitMethods;
        mFinalFieldList = Stream.of(mNewClassNode, mOldClassNode)
                .flatMap(dcn -> dcn.getFields().stream())
                .filter(dfn -> dfn.accessFlags.containsOneOf(DexAccessFlags.ACC_FINAL))
                .collect(Collectors.toList());
    }

    @Override
    public void visitConstInsn(int op, DexRegisterList regs, DexConst dexConst) {
        Dop dop = Dops.dopFor(op);
        if (dop.isFieldInstancePut() || dop.isFieldStaticPut()) {
            DexConst.ConstFieldRef fieldRef = (DexConst.ConstFieldRef) dexConst;
            mFinalFieldList.stream()
                    .filter(dfn -> fieldRef.getName().equals(dfn.name)
                            && fieldRef.getType().equals(dfn.type))
                    .map(ChangedClassDiffMarker::getFieldDiffMode)
                    .filter(diffMode -> diffMode.isUnChanged() || diffMode.isAdded())
                    // 将所有符合中要求的filed标记为AFFECT_FINAL_FIELD
                    .forEach(diffMode -> diffMode.markChanged(DiffMode.REASON_FINAL_FIELD_AFFECTED));
        }
        super.visitConstInsn(op, regs, dexConst);
    }

    /**
     * 开始进行标记
     */
    public void mark() {
        mChangedOrAddedInitMethods.stream()
                .map(DexMethodNode::getCode)
                .forEach(dcn -> dcn.accept(this));
        markUnchangedInitMethod();
    }

    /**
     * final field标记完成后，查找没有修改过的<init>方法，如果其中也有对final字段的操作，也将该<init>方法标记为受影响
     */
    private void markUnchangedInitMethod() {
        List<DexFieldNode> finalFieldList =
                Stream.of(mNewClassNode, mOldClassNode).flatMap(dcn -> dcn.getFields().stream())
                        .filter(dfn -> dfn.accessFlags.containsOneOf(DexAccessFlags.ACC_FINAL))
                        .collect(Collectors.toList());

        Stream.of(mNewClassNode, mOldClassNode)
                .flatMap(dcn -> dcn.getMethods().stream())
                .filter(DexMethodNode::isInstanceInitMethod)
                .filter(dmn -> ChangedClassDiffMarker.getMethodDiffMode(dmn).isUnChanged())
                .forEach(dmn -> {
                    DexCodeNode codeNode = dmn.getCode();
                    codeNode.getInsns().stream()
                            .filter(node -> node instanceof DexConstInsnNode)
                            .map(node -> (DexConstInsnNode) node)
                            .filter(node -> Dops.dopFor(node.getOpcode()).isFieldInstancePut())
                            .map(node -> (DexConst.ConstFieldRef) node.getConst())
                            .flatMap(fieldRef -> finalFieldList.stream()
                                    .filter(f -> fieldRef.getName().equals(f.name)
                                            && fieldRef.getType().equals(f.type))
                                    .map(ChangedClassDiffMarker::getFieldDiffMode))
                            .filter(DiffMode::isAffectFinalField)
                            .findAny()
                            .ifPresent( diffMode -> {
                                DiffMode methodDiffMode = ChangedClassDiffMarker.getMethodDiffMode(dmn);
                                methodDiffMode.markChanged(DiffMode.REASON_FINAL_FIELD_AFFECTED);
                            });
                });
    }

    /**
     * 对类中所有方法进行检查，判断是否有方法受到final field值变化的影响
     *
     * @param diffContext diff中使用到的一些上下文相关环境
     * @param newClassNode 类结点
     */
    public static void markAccessFinalField(DiffContext diffContext, DexClassNode newClassNode) {
        markAccessFinalField(newClassNode, diffContext.linker, diffContext.classLoaderFromNewPool);
        DexClassNode oldOrgClass = diffContext.oldOrgAppPool.getProgramClassPool().getClass(newClassNode.type);
        if (oldOrgClass != null) {
            markAccessFinalField(oldOrgClass, diffContext.linker, diffContext.classLoaderFromOldPool);
        }
    }

    private static void markAccessFinalField(DexClassNode classNode, ClassLinker linker, DexClassLoader loader) {
        classNode.getMethods().stream()
                .filter(dmn -> dmn.accessFlags.containsNoneOf(DexAccessFlags.ACC_ABSTRACT
                        | DexAccessFlags.ACC_NATIVE))
                .forEach(dmn -> {
                    DexCodeNode codeNode = dmn.getCode();
                    if (codeNode != null && codeNode.getInsns() != null) {
                        codeNode.getInsns().stream()
                                .filter(node -> node instanceof DexConstInsnNode)
                                .map(node -> (DexConstInsnNode) node)
                                .filter(node -> Dops.dopFor(node.getOpcode()).isFieldAccessKind())
                                .map(node -> (DexConst.ConstFieldRef) node.getConst())
                                .map(fieldRef -> {
                                    DexFieldNode dfn = linker.resolveFieldJLS(loader, fieldRef);
                                    if (dfn == null) {
                                        dfn = new DexFieldNode(fieldRef.getName(),
                                                fieldRef.getType(),
                                                fieldRef.getOwner(),
                                                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC));
                                    }
                                    return dfn;
                                })
                                .map(ChangedClassDiffMarker::getFieldDiffMode)
                                .filter(DiffMode::isAffectFinalField)
                                .findAny()
                                .ifPresent(diffMode -> {
                                    DiffMode methodDiffMode = ChangedClassDiffMarker.getMethodDiffMode(dmn);
                                    methodDiffMode.markChanged(DiffMode.REASON_FINAL_FIELD_AFFECTED);
                                    ClassPoolDiffMarker.getClassDiffMode(classNode)
                                            .markChanged(DiffMode.REASON_FINAL_FIELD_AFFECTED);
                                });
                    }
                });
    }


}
