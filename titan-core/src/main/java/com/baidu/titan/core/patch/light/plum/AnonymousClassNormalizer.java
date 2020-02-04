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

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.instrument.DisableInterceptMarker;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dop;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexAnnotationVisitor;
import com.baidu.titan.dex.visitor.DexAnnotationVisitorInfo;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitorInfo;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

/**
 * 对匿名内部类进行归一化
 *
 * @author shanghuibo
 * @since 2019/01/27
 */
public class AnonymousClassNormalizer {

    /**
     * 对匿名内部类进行归一
     *
     * @param classPool
     * @param dexItemFactory
     * @param anonymousClassNode
     * @return
     */
    public static DexClassNode normalizeAnonymousClass(ApplicationDexPool classPool,
                                                       TitanDexItemFactory dexItemFactory,
                                                       DexClassNode anonymousClassNode) {

        if (AnonymousClassMarker.isAnonymousClass(dexItemFactory, anonymousClassNode)) {
            DexType normalizedClassType = getNormalizedType(classPool, dexItemFactory, anonymousClassNode.type);
            DexClassNode normalizedClassNode = new DexClassNode(
                    new DexClassVisitorInfo(
                            normalizedClassType,
                            anonymousClassNode.superType,
                            anonymousClassNode.interfaces,
                            anonymousClassNode.accessFlags)
            );

            anonymousClassNode.accept(new DexClassVisitor(normalizedClassNode.asVisitor()) {
                @Override
                public void visitBegin() {
                    super.visitBegin();
                }

                @Override
                public void visitSourceFile(DexString sourceFile) {
//                    super.visitSourceFile(sourceFile);
                }

                @Override
                public DexAnnotationVisitor visitAnnotation(DexAnnotationVisitorInfo annotationInfo) {
                    return new DexAnnotationVisitor(super.visitAnnotation(annotationInfo)) {
                        @Override
                        public void visitMethod(DexString name, DexConst.ConstMethodRef methodRef) {
                            DexConst.ConstMethodRef newMethodRef = null;
                            if (methodRef.getOwner().equals(anonymousClassNode.type)) {
                                newMethodRef = DexConst.ConstMethodRef.make(
                                        normalizedClassType,
                                        methodRef.getName(),
                                        getNormalizedType(classPool,
                                                dexItemFactory,
                                                methodRef.getReturnType()),
                                        getNormalizedTypeList(classPool,
                                                dexItemFactory,
                                                methodRef.getParameterTypes()));
                            } else if (AnonymousClassMarker.isAnonymousClass(classPool,
                                    dexItemFactory, methodRef.getOwner())) {

                                DexType outerType = AnonymousClassMarker.getOutClassType(classPool,
                                        dexItemFactory, anonymousClassNode.type);
                                if (outerType.equals(methodRef.getOwner())) {
                                    DexType newMethodOwnerType = getNormalizedType(classPool,
                                            dexItemFactory, methodRef.getOwner());
                                    newMethodRef = DexConst.ConstMethodRef.make(
                                            newMethodOwnerType,
                                            methodRef.getName(),
                                            getNormalizedType(classPool, dexItemFactory, methodRef.getReturnType()),
                                            getNormalizedTypeList(classPool,
                                                    dexItemFactory, methodRef.getParameterTypes()));
                                }
                            }
                            if (newMethodRef == null) {
                                newMethodRef = methodRef;
                            }
                            super.visitMethod(name, newMethodRef);
                        }

                        @Override
                        public void visitType(DexString name, DexType type) {
                            DexType newType = null;
                            if (AnonymousClassMarker.isAnonymousClass(classPool, dexItemFactory, type)) {
                                if (type.equals(anonymousClassNode.type)) {
                                    newType = normalizedClassType;
                                } else {
                                    DexType outerType = AnonymousClassMarker.getOutClassType(classPool,
                                            dexItemFactory, anonymousClassNode.type);
                                    if (outerType.equals(type)) {
                                        newType = getNormalizedType(classPool, dexItemFactory, type);
                                    }
                                }
                            }
                            if (newType == null) {
                                newType = type;
                            }
                            super.visitType(name, newType);
                        }

                        @Override
                        public void visitField(DexString name, DexConst.ConstFieldRef fieldRef) {
                            DexConst.ConstFieldRef newFieldRef = null;
                            if (fieldRef.getOwner().equals(anonymousClassNode.type)) {
                                newFieldRef = DexConst.ConstFieldRef.make(
                                        normalizedClassType,
                                        fieldRef.getType(),
                                        fieldRef.getName());
                            } else if (AnonymousClassMarker.isAnonymousClass(classPool,
                                    dexItemFactory, fieldRef.getOwner())) {
                                DexType outerType = AnonymousClassMarker.getOutClassType(classPool,
                                        dexItemFactory, anonymousClassNode.type);
                                if (outerType.equals(fieldRef.getOwner())) {
                                    DexType newFieldOwnerType = getNormalizedType(classPool,
                                            dexItemFactory, fieldRef.getOwner());
                                    newFieldRef = DexConst.ConstFieldRef.make(
                                            newFieldOwnerType,
                                            fieldRef.getType(),
                                            fieldRef.getName());
                                }
                            }
                            if (newFieldRef == null) {
                                newFieldRef = fieldRef;
                            }
                            super.visitField(name, newFieldRef);
                        }
                    };
                }

                @Override
                public DexFieldVisitor visitField(DexFieldVisitorInfo fieldInfo) {
                    DexFieldVisitorInfo newFieldInfo = new DexFieldVisitorInfo(normalizedClassNode.type,
                            fieldInfo.name,
                            getNormalizedType(classPool, dexItemFactory, fieldInfo.type),
                            fieldInfo.accessFlags);
                    return super.visitField(newFieldInfo);
                }

                @Override
                public DexMethodVisitor visitMethod(DexMethodVisitorInfo methodInfo) {
                    DexMethodVisitorInfo newMethodInfo = new DexMethodVisitorInfo(normalizedClassNode.type,
                            methodInfo.name,
                            getNormalizedTypeList(classPool, dexItemFactory, methodInfo.parameters),
                            getNormalizedType(classPool, dexItemFactory, methodInfo.returnType),
                            methodInfo.accessFlags);
                    return new DexMethodVisitor(super.visitMethod(newMethodInfo)) {
                        @Override
                        public DexCodeVisitor visitCode() {
                            return new DexCodeVisitor(super.visitCode()) {
                                @Override
                                public void visitConstInsn(int op, DexRegisterList regs, DexConst dexConst) {
                                    Dop dop = Dops.dopFor(op);
                                    if (dop.isFieldAccessKind()) {
                                        DexConst.ConstFieldRef fieldRef = (DexConst.ConstFieldRef) dexConst;
                                        DexConst.ConstFieldRef newFieldRef = null;
                                        if (fieldRef.getOwner().equals(anonymousClassNode.type)) {
                                            newFieldRef = DexConst.ConstFieldRef.make(
                                                    normalizedClassType,
                                                    getNormalizedType(classPool, dexItemFactory, fieldRef.getType()),
                                                    fieldRef.getName());
                                        } else if (AnonymousClassMarker.isAnonymousClass(classPool,
                                                dexItemFactory, fieldRef.getOwner())) {
                                            DexType outerType = AnonymousClassMarker.getOutClassType(classPool,
                                                    dexItemFactory, fieldRef.getOwner());
                                            if (outerType.equals(anonymousClassNode.type)) {
                                                String fieldOwnerType = fieldRef.getOwner().toTypeDescriptor();
                                                String newFieldOwner = normalizedClassType.toTypeDescriptor()
                                                        .replace(";", fieldOwnerType
                                                                .substring(fieldOwnerType.lastIndexOf("$")));
                                                newFieldRef = DexConst.ConstFieldRef.make(
                                                        dexItemFactory.createType(newFieldOwner),
                                                        getNormalizedType(classPool, dexItemFactory,
                                                                fieldRef.getType()),
                                                        fieldRef.getName());
                                            }
                                        }
                                        if (newFieldRef == null) {
                                            newFieldRef = fieldRef;
                                        }
                                        super.visitConstInsn(op, regs, newFieldRef);
                                        return;
                                    } else if (dop.isInvokeKind()) {
                                        DexConst.ConstMethodRef methodRef = (DexConst.ConstMethodRef) dexConst;
                                        DexConst.ConstMethodRef newMethodRef = null;
                                        if (methodRef.getOwner().equals(anonymousClassNode.type)) {
                                            newMethodRef = DexConst.ConstMethodRef.make(
                                                    normalizedClassType,
                                                    methodRef.getName(),
                                                    getNormalizedType(classPool, dexItemFactory,
                                                            methodRef.getReturnType()),
                                                    getNormalizedTypeList(classPool, dexItemFactory,
                                                            methodRef.getParameterTypes()));
                                        } else if (AnonymousClassMarker.isAnonymousClass(classPool, dexItemFactory,
                                                methodRef.getOwner())) {
                                            DexType outerType = AnonymousClassMarker.getOutClassType(classPool,
                                                    dexItemFactory, methodRef.getOwner());
                                            if (outerType.equals(anonymousClassNode.type)) {
                                                String methodOwnerType = methodRef.getOwner().toTypeDescriptor();
                                                String newMethodOwner = normalizedClassType.toTypeDescriptor()
                                                        .replace(";", methodOwnerType
                                                                .substring(methodOwnerType.lastIndexOf("$")));
                                                newMethodRef = DexConst.ConstMethodRef.make(
                                                        dexItemFactory.createType(newMethodOwner),
                                                        methodRef.getName(),
                                                        getNormalizedType(classPool, dexItemFactory,
                                                                methodRef.getReturnType()),
                                                        getNormalizedTypeList(classPool, dexItemFactory,
                                                                methodRef.getParameterTypes()));
                                            }
                                        }
                                        if (newMethodRef == null) {
                                            newMethodRef = methodRef;
                                        }
                                        super.visitConstInsn(op, regs, newMethodRef);
                                        return;
                                    } else if (dexConst instanceof DexConst.ConstType) {
                                        DexConst.ConstType constType = (DexConst.ConstType) dexConst;
                                        DexType type = constType.value();
                                        DexConst.ConstType newConstType = null;
                                        if (AnonymousClassMarker.isAnonymousClass(classPool, dexItemFactory, type)) {
                                            if (type.equals(anonymousClassNode.type)) {
                                                newConstType = DexConst.ConstType.make(normalizedClassType);
                                            } else {
                                                DexType outerType = AnonymousClassMarker.getOutClassType(classPool,
                                                        dexItemFactory, type);
                                                if (outerType.equals(anonymousClassNode.type)) {
                                                    String typeDesc = type.toTypeDescriptor();
                                                    String newTypeDesc = normalizedClassType.toTypeDescriptor()
                                                            .replace(";", typeDesc
                                                                    .substring(typeDesc.lastIndexOf("$")));
                                                    DexType newType = dexItemFactory.createType(newTypeDesc);
                                                    newConstType = DexConst.ConstType.make(newType);
                                                }
                                            }
                                        }
                                        if (newConstType == null) {
                                            newConstType = constType;
                                        }
                                        super.visitConstInsn(op, regs, newConstType);
                                        return;
                                    }
                                    super.visitConstInsn(op, regs, dexConst);
                                }
                            };
                        }
                    };
                }

                @Override
                public void visitEnd() {
                    super.visitEnd();
                }
            });
            DisableInterceptMarker.setInterceptDisable(normalizedClassNode,
                    DisableInterceptMarker.getInterceptDisable(anonymousClassNode));

            DisableInterceptMarker disableInterceptMarker = new DisableInterceptMarker(null,
                    null, dexItemFactory);
            normalizedClassNode.accept(disableInterceptMarker);
            return normalizedClassNode;
        }
        return anonymousClassNode;
    }

    private static DexTypeList getNormalizedTypeList(ApplicationDexPool classPool,
                                                     TitanDexItemFactory dexItemFactory,
                                                     DexTypeList typeList) {

        DexTypeList.Builder builder = DexTypeList.newBuilder();

        for (DexType type : typeList) {
            builder.addType(getNormalizedType(classPool, dexItemFactory, type));
        }
        return builder.build();

    }

    private static DexType getNormalizedType(ApplicationDexPool classPool,
                                             TitanDexItemFactory dexItemFactory,
                                             DexType type) {
        if (AnonymousClassMarker.isAnonymousClass(classPool, dexItemFactory, type)) {
            DexType outerType = AnonymousClassMarker.getOutClassType(classPool, dexItemFactory, type);
            DexType outerNormalizedType = getNormalizedType(classPool, dexItemFactory, outerType);
            String normalizedTypeString = outerNormalizedType.toTypeDescriptor().replace(";", "$Anonymous;");
            return dexItemFactory.createType(normalizedTypeString);
        }
        return type;
    }
}
