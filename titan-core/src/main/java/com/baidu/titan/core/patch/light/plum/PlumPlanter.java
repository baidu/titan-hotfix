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
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitorInfo;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 * 《乐府诗集·鸡鸣》
 * 桃在露井上，李树在桃旁，虫在啮桃根，李树代桃僵。树木身相代，兄弟还相忘！
 *
 * 对类进行重写，重写后的类替代原来的类与old class进行diff并生成patch
 *
 * 用于对类进行重写，被重写的内容包括：
 * 1. 类型属于被重写的field
 * 2. 返回值、任一参数必于被重写类型的method
 * 3. method中调用的field access指令，被调用的Field的owner或type属于被重写类型的
 * 4. method中调用的invoke指令，被调用的method的owner, return type， 任一参数类型属于被重写类型的
 * 5. 操作码中的const为ConstType类型，且ConstType的类型为被重写类型
 *
 * @author shanghuibo
 * @since 2019/01/27
 */
public class PlumPlanter {
    public static DexClassNode rewrite(DiffContext diffContext, DexClassNode outerDcn) {
        DexClassNode rewriteClassNode = new DexClassNode(
                new DexClassVisitorInfo(
                        AnonymousClassDiffMarker.getRewriteType(diffContext, outerDcn),
                        outerDcn.superType,
                        outerDcn.interfaces,
                        outerDcn.accessFlags
                ));

        outerDcn.accept(new DexClassVisitor(rewriteClassNode.asVisitor()) {
            @Override
            public DexFieldVisitor visitField(DexFieldVisitorInfo fieldInfo) {
                DexType remapType = AnonymousClassDiffMarker.getRewriteType(diffContext, fieldInfo.type);
                // if (remapType == fieldInfo.type) {
                //     return super.visitField(fieldInfo);
                // } else {
                    DexFieldVisitorInfo newFieldInfo = new DexFieldVisitorInfo(rewriteClassNode.type,
                            fieldInfo.name,
                            remapType,
                            fieldInfo.accessFlags);
                    return super.visitField(newFieldInfo);
                // }
            }

            @Override
            public DexMethodVisitor visitMethod(DexMethodVisitorInfo methodInfo) {
                DexType newOwner = AnonymousClassDiffMarker.getRewriteType(diffContext, outerDcn);
                DexType newReturnType = AnonymousClassDiffMarker.getRewriteType(diffContext, methodInfo.returnType);
                DexTypeList.Builder builder = DexTypeList.newBuilder();
                methodInfo.parameters.forEach(type -> {
                    DexType newParaType = AnonymousClassDiffMarker.getRewriteType(diffContext, type);
                    builder.addType(newParaType);
                });
                DexTypeList newParameters = builder.build();
                return new DexMethodVisitor(super.visitMethod(new DexMethodVisitorInfo(newOwner, methodInfo.name,
                        newParameters,
                        newReturnType,
                        methodInfo.accessFlags))) {
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

                                    DexConst.ConstFieldRef newFieldRef = DexConst.ConstFieldRef.make(
                                            remapOwner,
                                            remapType,
                                            fieldRef.getName());
                                    super.visitConstInsn(op, regs, newFieldRef);
                                    return;
                                } else if (dop.isInvokeKind()) {
                                    DexConst.ConstMethodRef methodRef = (DexConst.ConstMethodRef) dexConst;
                                    DexType remapOwner = AnonymousClassDiffMarker.getRewriteType(diffContext,
                                            methodRef.getOwner());
                                    DexType remapReturnType = AnonymousClassDiffMarker.getRewriteType(diffContext,
                                            methodRef.getReturnType());
                                    DexTypeList.Builder builder = DexTypeList.newBuilder();
                                    methodRef.getParameterTypes().forEach(type -> {
                                        builder.addType(AnonymousClassDiffMarker.getRewriteType(diffContext, type));
                                    });

                                    DexConst.ConstMethodRef newMethodRef = DexConst.ConstMethodRef.make(
                                            remapOwner,
                                            methodRef.getName(),
                                            remapReturnType,
                                            builder.build());
                                    super.visitConstInsn(op, regs, newMethodRef);
                                    return;
                                } else if (dexConst instanceof DexConst.ConstType) {
                                    DexConst.ConstType constType = (DexConst.ConstType) dexConst;
                                    DexType type = constType.value();
                                    DexType remapType = AnonymousClassDiffMarker.getRewriteType(diffContext, type);
                                    DexConst.ConstType newConstType = DexConst.ConstType.make(remapType);
                                    super.visitConstInsn(op, regs, newConstType);
                                    return;
                                }
                                super.visitConstInsn(op, regs, dexConst);
                            }
                        };
                    }
                };
            }
        });
        return rewriteClassNode;
    }
}
