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

package com.baidu.titan.core.transforms;

import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;

/**
 * 对于所有Component class,都插入一个<clinit>方法，作用是做到拦截器(interceptor初始化的懒加载)，
 * 如果之前不存在的话
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/9
 */
public class StaticInitMethodFiller implements DexClassPoolNodeVisitor {

    private DexItemFactory mFactory;

    public StaticInitMethodFiller(DexItemFactory factory) {
        this.mFactory = factory;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        boolean containsClinit = false;
        for (DexMethodNode dmn : dcn.getMethods()) {
            if (dmn.isStaticInitMethod()) {
                containsClinit = true;
            }
        }

        if (!containsClinit) {
            DexMethodNode clinitMethod = new DexMethodNode(
                    mFactory.methods.staticInitMethodName,
                    dcn.type,
                    DexTypeList.empty(),
                    mFactory.voidClass.primitiveType,
                    new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_STATIC
                            | DexAccessFlags.ACC_CONSTRUCTOR));

            DexMethodVisitor methodVisitor = clinitMethod.asVisitor();

            methodVisitor.visitBegin();

            DexCodeRegisterCalculator clinitCodeVisitor =
                    new DexCodeRegisterCalculator(
                            true,
                            DexTypeList.empty(),
                            new DexCodeFormatVerifier(methodVisitor.visitCode()));

            clinitCodeVisitor.visitBegin();
            clinitCodeVisitor.visitSimpleInsn(
                    Dops.RETURN_VOID,
                    DexRegisterList.EMPTY);

            clinitCodeVisitor.fillRegisterCount();

            clinitCodeVisitor.visitEnd();

            methodVisitor.visitEnd();

            dcn.addMethod(clinitMethod);

        }


    }

    @Override
    public void classPoolVisitEnd() {

    }
}
