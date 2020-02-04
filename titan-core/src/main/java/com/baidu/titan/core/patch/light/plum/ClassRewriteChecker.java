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

import com.baidu.titan.core.patch.light.diff.DiffContext;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 * 用于检查是否要对类进行重写
 *
 * @author shanghuibo
 * @since 2018/01/25
 */
public class ClassRewriteChecker {

    /**
     * 检查是否需要重写类
     *
     * @param diffContext DiffContext
     * @param outerDcn 需要检查的类
     * @return
     */
    public static boolean checkRewrite(DiffContext diffContext, DexClassNode outerDcn) {

        outerDcn.accept(new DexClassVisitor() {
            @Override
            public DexFieldVisitor visitField(DexFieldVisitorInfo fieldInfo) {
                DexType remapType = AnonymousClassDiffMarker.getRewriteType(diffContext, fieldInfo.type);
                if (!remapType.equals(fieldInfo.type)) {
                    AnonymousClassDiffMarker.markShouldRewrite(outerDcn);
                }
                return super.visitField(fieldInfo);
            }

            @Override
            public DexMethodVisitor visitMethod(DexMethodVisitorInfo methodInfo) {
                DexType newReturnType = AnonymousClassDiffMarker.getRewriteType(diffContext, methodInfo.returnType);
                if (!newReturnType.equals(methodInfo.returnType)) {
                    AnonymousClassDiffMarker.markShouldRewrite(outerDcn);
                    return super.visitMethod(methodInfo);
                }
                for (DexType type : methodInfo.parameters) {
                    DexType newParaType = AnonymousClassDiffMarker.getRewriteType(diffContext, type);
                    if (!newParaType.equals(type)) {
                        AnonymousClassDiffMarker.markShouldRewrite(outerDcn);
                        return super.visitMethod(methodInfo);
                    }
                }
                return new DexMethodVisitor(super.visitMethod(methodInfo)) {
                    @Override
                    public DexCodeVisitor visitCode() {
                        return new DexCodeVisitor(super.visitCode()) {
                            @Override
                            public void visitConstInsn(int op, DexRegisterList regs, DexConst dexConst) {
                                Dop dop = Dops.dopFor(op);
                                if (dop.isFieldAccessKind()) {
                                    DexConst.ConstFieldRef fieldRef = (DexConst.ConstFieldRef) dexConst;
                                    DexType remapOwner = AnonymousClassDiffMarker.getRewriteType(diffContext,
                                            fieldRef.getOwner());
                                    DexType remapType = AnonymousClassDiffMarker.getRewriteType(diffContext,
                                            fieldRef.getType());
                                    if (!remapType.equals(fieldRef.getType())
                                            || !remapOwner.equals(fieldRef.getOwner())) {
                                        AnonymousClassDiffMarker.markShouldRewrite(outerDcn);
                                    }
                                } else if (dop.isInvokeKind()) {
                                    DexConst.ConstMethodRef methodRef = (DexConst.ConstMethodRef) dexConst;
                                    DexType remapOwner = AnonymousClassDiffMarker.getRewriteType(diffContext,
                                            methodRef.getOwner());
                                    DexType remapReturnType = AnonymousClassDiffMarker.getRewriteType(diffContext,
                                            methodRef.getReturnType());
                                    if (!remapOwner.equals(methodRef.getOwner())
                                            || !remapReturnType.equals(methodRef.getReturnType())) {
                                        AnonymousClassDiffMarker.markShouldRewrite(outerDcn);
                                    }
                                    for (DexType type : methodRef.getParameterTypes()) {
                                        DexType newParaType = AnonymousClassDiffMarker
                                                .getRewriteType(diffContext, type);
                                        if (!newParaType.equals(type)) {
                                            AnonymousClassDiffMarker.markShouldRewrite(outerDcn);
                                            break;
                                        }
                                    }

                                } else if (dexConst instanceof DexConst.ConstType) {
                                    DexConst.ConstType constType = (DexConst.ConstType) dexConst;
                                    DexType type = constType.value();
                                    DexType remapType = AnonymousClassDiffMarker.getRewriteType(diffContext, type);
                                    if (!remapType.equals(type)) {
                                        AnonymousClassDiffMarker.markShouldRewrite(outerDcn);
                                    }
                                }
                                super.visitConstInsn(op, regs, dexConst);
                            }
                        };
                    }
                };
            }
        });
        return AnonymousClassDiffMarker.shouldRewrite(outerDcn);
    }
}
