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

package com.baidu.titan.core.component;

import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexClassNodeVisitor;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.List;

/**
 * mark component info
 *
 * @author zhangdi07
 * @since 2017/9/6
 */

public class AndroidComponentMarker implements DexClassPoolNodeVisitor, DexClassNodeVisitor {

    private List<String> mComponents;

    private int mComponentType;

    private static final String KEY_EXTRA_COMPONET = "key_extra_component";

    public AndroidComponentMarker(List<String> components, int componentType) {
        this.mComponents = components;
        this.mComponentType = componentType;
    }

    public static AndroidComponentFlag getComponentFlag(DexClassNode dcn) {
        return dcn.getExtraInfo(KEY_EXTRA_COMPONET, null);
    }

    public static void setFlag(DexClassNode dcn, int componentType, int flag) {
        AndroidComponentFlag cf = dcn.getExtraInfo(KEY_EXTRA_COMPONET, null);
        if (cf == null) {
            cf = new AndroidComponentFlag();
            dcn.setExtraInfo(KEY_EXTRA_COMPONET, cf);
        }
        cf.setFlag(componentType, flag);
    }

    @Override
    public void visitClass(DexClassNode currentClassNode) {
        if (mComponents == null) {
            return;
        }
        if (mComponents.remove(currentClassNode.type.toTypeDescriptor())) {
            setFlag(currentClassNode, mComponentType, AndroidComponentFlag.FLAG_DIRECT);
            while (true) {
                DexClassNode superClassNode = currentClassNode.getExtraInfo(
                        DexSuperClassHierarchyFiller.EXTRA_KEY_SUPERCLASSES, null);
                if (superClassNode == null) {
                    // setFlag(currentClassNode, AndroidComponentFlag.FLAG_SUPER_TOP);
                    break;
                }
                currentClassNode = superClassNode;
                setFlag(superClassNode, mComponentType, AndroidComponentFlag.FLAG_SUPER);
            }
        }
    }

    @Override
    public void classPoolVisitEnd() {
        if (mComponents != null && !mComponents.isEmpty()) {
            return;
        }
    }

    // ===
    @Override
    public void visitClassAnnotation(DexAnnotationNode dan) {

    }

    @Override
    public void visitMethod(DexMethodNode dmn) {

    }

    @Override
    public void visitField(DexFieldNode dfn) {

    }

    @Override
    public void visitClassNodeEnd() {

    }

}
