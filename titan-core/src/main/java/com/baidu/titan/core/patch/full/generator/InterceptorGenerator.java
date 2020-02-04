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

package com.baidu.titan.core.patch.full.generator;

import com.baidu.titan.sdk.runtime.InteceptParameters;
import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.extensions.MethodIdAssigner;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.node.DexNamedProtoNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * 用于生成Interceptor。
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/18
 */
public abstract class InterceptorGenerator {

    public DexType orgType;

    public DexType interceptorType;

    public TitanDexItemFactory factory;

    protected DexClassNode instrumentedClassNode;

    protected Set<DexNamedProtoNode> methods = new HashSet<>();

    public InterceptorGenerator(DexType orgType, DexType interceptorType,
                                TitanDexItemFactory factory, DexClassNode instrumentedClassNode) {
        this.orgType = orgType;
        this.interceptorType = interceptorType;
        this.factory = factory;
        this.instrumentedClassNode = instrumentedClassNode;
    }

    public void addInterceptMethod(DexNamedProtoNode method) {
        this.methods.add(method);
    }

    public static class InvokeMethod {

        public static final String INVOKE_KIND_COMMON_NAME = "Common";

        public static final String INVOKE_KIND_INIT_NAME = "Init";

        public static final int INVOKE_KIND_COMMON = 0;

        public static final int INVOKE_KIND_SPECIAL = 1;

        public static final int INVOKE_KIND_INIT = 2;

        public final String invokeKindName;

        public final List<InterceptedMethod> interceptedMethods;

        private TitanDexItemFactory mFactory;

        public InvokeMethod(String invokeKind,
                            List<InterceptedMethod> methods,
                            TitanDexItemFactory factory) {
            this.invokeKindName = invokeKind;
            interceptedMethods = methods;
            this.mFactory = factory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InvokeMethod)) return false;

            InvokeMethod that = (InvokeMethod) o;

            return invokeKindName.equals(that.invokeKindName);
        }

        public DexTypeList getInvokeParameters() {
            switch (getInvokeKind()) {
                case INVOKE_KIND_COMMON: {
                    return mFactory.interceptableClass.invokeCommonMethod.getParameterTypes();
                }
                case INVOKE_KIND_SPECIAL: {
                    return mFactory.interceptableClass.getInvokeSpecialMethod(this.invokeKindName)
                            .getParameterTypes();
                }
                default: {
                    return null;
                }
            }
        }

        public int getInvokeKind() {

            if (INVOKE_KIND_COMMON_NAME.equals(this.invokeKindName)) {
                return INVOKE_KIND_COMMON;
            } else if (INVOKE_KIND_INIT_NAME.equals(this.invokeKindName)) {
                return INVOKE_KIND_INIT;
            } else {
                return INVOKE_KIND_SPECIAL;
            }
        }

        public String getInvokeMethodName() {
            return "invoke" + invokeKindName;
        }

        @Override
        public int hashCode() {
            return invokeKindName.hashCode();
        }
    }

    public class InterceptedMethod {

        public final DexNamedProtoNode method;

        private DexItemFactory mFactory;

        public InterceptedMethod(DexNamedProtoNode method,
                                 DexItemFactory factory) {
            this.method = method;
            this.mFactory = factory;
        }

        public String getInvokeKind() {
            if (method.name.toString().equals("<init>")) {
                return InvokeMethod.INVOKE_KIND_INIT_NAME;
            }

            DexTypeList parameterTypes = method.parameters;
            StringBuilder paraBuilder = new StringBuilder();
            if (parameterTypes.count() == 0) {
                paraBuilder.append("V");
            } else {
                for (DexType type: parameterTypes.types()) {
                    char shortType = type.toTypeDescriptor().charAt(0);
                    if (shortType == '[') {
                        shortType = 'L';
                    }
                    paraBuilder.append(shortType);
                }
            }
            String invokeKind = paraBuilder.toString();

            for (String p : InteceptParameters.SPECIAL_PARAMETERS) {
                if (p.equals(invokeKind)) {
                    return invokeKind;
                }
            }
            return InvokeMethod.INVOKE_KIND_COMMON_NAME;
        }

        public int getMethodId() {
            List<DexMethodNode> candidateMethods =
                    InterceptorGenerator.this.instrumentedClassNode.getMethods().stream()
                    .filter(dmn -> new DexNamedProtoNode(dmn).equals(this.method))
                    .collect(Collectors.toList());

            if (candidateMethods.size() != 1) {
                throw new IllegalStateException();
            }

            return MethodIdAssigner.getMethodId(candidateMethods.get(0));
        }

    }


    public DexClassNode generate() {

        MethodIdAssigner.assignMethodId(this.instrumentedClassNode);

        DexClassNode interceptorClassNode = new DexClassNode(
                this.interceptorType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC),
                factory.simpleInterceptorClass.type,
                DexTypeList.empty());

        DexClassVisitor classVisitor = interceptorClassNode.asVisitor();

        classVisitor.visitBegin();

        generateForInit(interceptorClassNode, classVisitor);

        List<InvokeMethod> invokeMethods = methods.stream()
                .map(m -> new InterceptedMethod(m, factory))
                .collect(Collectors.groupingBy(InterceptedMethod::getInvokeKind))
                .entrySet().stream()
                .map(e -> new InvokeMethod(e.getKey(), e.getValue(), factory))
                .collect(Collectors.toList());

        invokeMethods.forEach(im -> {
            switch (im.getInvokeKind()) {
                case InvokeMethod.INVOKE_KIND_COMMON: {
                    generateForInvokeCommon(interceptorClassNode, classVisitor, im);
                    break;
                }
                case InvokeMethod.INVOKE_KIND_SPECIAL: {
                    generateForInvokeSpecial(interceptorClassNode, classVisitor, im);
                    break;
                }
                case InvokeMethod.INVOKE_KIND_INIT: {
                    generateForInvokeInit(interceptorClassNode, classVisitor, im);
                }

            }
        });

        classVisitor.visitEnd();
        return interceptorClassNode;
    }

    protected void generateForInit(DexClassNode interceptorClassNode,
                                                     DexClassVisitor classVisitor) {
        DexMethodVisitorInfo methodVisitorInfo = new DexMethodVisitorInfo(
                interceptorType,
                factory.createString("<init>"),
                DexTypeList.empty(),
                factory.voidClass.primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_CONSTRUCTOR));

        DexMethodVisitor methodVisitor = classVisitor.visitMethod(methodVisitorInfo);

        methodVisitor.visitBegin();

        DexCodeRegisterCalculator codeVisitor = new DexCodeRegisterCalculator(
                false,
                methodVisitorInfo.parameters,
                new DexCodeFormatVerifier(methodVisitor.visitCode()));

        codeVisitor.visitBegin();
        codeVisitor.visitRegisters(0, 1);

        int pThisObj = 0;

        codeVisitor.visitConstInsn(
                Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeParameterReg(pThisObj)),
                DexConst.ConstMethodRef.make(
                        factory.simpleInterceptorClass.type,
                        factory.methods.initMethodName,
                        factory.voidClass.primitiveType,
                        DexTypeList.empty()));

        codeVisitor.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.EMPTY);

        codeVisitor.fillRegisterCount();

        codeVisitor.visitEnd();

        methodVisitor.visitEnd();

    }

    protected abstract void generateForInvokeInit(DexClassNode interceptorClassNode,
                                         DexClassVisitor classVisitor,
                                         InvokeMethod invokeMethod);

    protected abstract void generateForInvokeSpecial(DexClassNode interceptorClassNode,
                                            DexClassVisitor classVisitor,
                                            InvokeMethod invokeMethod);

    protected abstract void generateForInvokeCommon(DexClassNode interceptorClassNode,
                                           DexClassVisitor classVisitor,
                                           InvokeMethod invokeMethod);


}
