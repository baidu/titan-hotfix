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

package com.baidu.titan.core.instrument;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexClassNodeVisitor;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.List;

/**
 * 对带有DisableIntercept注解的类进行标记
 *
 * @author zhangdi07
 * @since 2017/9/14
 */

public class DisableInterceptMarker implements DexClassPoolNodeVisitor, DexClassNodeVisitor {

    private DisableInterceptCollector mDisableInterceptCollector;

    private InstrumentFilter mFilter;

    private DexClassNode mCurrentDexClassNode;

    private static final String EXTRA_KEY_DISABLE_INTERCEPT = "disable-intercept";

    private TitanDexItemFactory mFactory;

    public DisableInterceptMarker(DisableInterceptCollector collector,
                                  InstrumentFilter filter,
                                  TitanDexItemFactory factory) {
        this.mDisableInterceptCollector = collector;
        this.mFilter = filter;
        this.mFactory = factory;
    }


    @Override
    public void visitClass(DexClassNode dcn) {
        mCurrentDexClassNode = dcn;

        boolean skipIntercept = dcn.type.toTypeDescriptor().startsWith("Lcom/baidu/titan/sdk/");

        if (!skipIntercept) {
            skipIntercept = (dcn.accessFlags.containsOneOf(DexAccessFlags.ACC_INTERFACE));
        }

        String typeDesc = dcn.type.toTypeDescriptor();

        // skip check in class level
        if (!skipIntercept) {
            List<DexAnnotationNode> dans = dcn.getClassAnnotations();
            if (dans != null && dans.size() > 0) {
                for (DexAnnotationNode dan : dans) {
                    if (mFactory.annotationClasses.disableInterceptAnnotation.type.equals(
                            dan.getType())) {
                        skipIntercept = true;
                    }
                }
            }
        }

        // skip check in package level

        if (!skipIntercept && mDisableInterceptCollector != null) {

            String simpleName = typeDesc.substring(1, typeDesc.length() - 1);
            int lastSp = simpleName.lastIndexOf("/");

            String packageInfoTypeDesc;
            if (lastSp < 0) {
                packageInfoTypeDesc = "Lpackage-info;";
            } else {
                packageInfoTypeDesc = "L" + simpleName.substring(0, lastSp) + "/package-info;";
            }

            if (mDisableInterceptCollector.contains(packageInfoTypeDesc)) {
                skipIntercept = true;
            }
        }

        if (!skipIntercept) {
            if (mFilter != null && !mFilter.acceptClass(typeDesc)) {
                skipIntercept = true;
            }
        }

        if (!skipIntercept) {
            if (MeizuPushMarker.isMeizuPush(dcn)) {
                skipIntercept = true;
            }
        }

        if (skipIntercept) {
            setInterceptDisable(dcn, true);
        } else {
            dcn.accept(this);
        }

    }

    @Override
    public void classPoolVisitEnd() {

    }
    // //////DexClassNodeVisitor BEGIN //////////////////////////////

    @Override
    public void visitClassAnnotation(DexAnnotationNode dan) {

    }

    @Override
    public void visitMethod(DexMethodNode dmn) {

        boolean skipMethod = dmn.accessFlags.containsOneOf(
                DexAccessFlags.ACC_INTERFACE | DexAccessFlags.ACC_ABSTRACT
                        | DexAccessFlags.ACC_SYNTHETIC | DexAccessFlags.ACC_NATIVE);

        if (!skipMethod) {
            List<DexAnnotationNode> mdans = dmn.getMethodAnnotations();
            if (mdans != null && mdans.size() > 0) {
                for (DexAnnotationNode mdan : mdans) {
                    if (mFactory.annotationClasses.disableInterceptAnnotation.type.equals(
                            mdan.getType())) {
                        skipMethod = true;
                    }
                }
            }
        }

        if (!skipMethod) {
            if (mFilter != null && !mFilter.acceptMethod(
                    // TODO
                    null)) {
                skipMethod = true;
            }
        }

        if (skipMethod) {
            setInterceptDisable(dmn, true);
        }
    }

    @Override
    public void visitField(DexFieldNode dfn) {

    }

    @Override
    public void visitClassNodeEnd() {

    }

    // //////DexClassNodeVisitor END //////////////////////////////

    public static void setInterceptDisable(DexClassNode dcn, boolean disable) {
        dcn.setExtraInfo(EXTRA_KEY_DISABLE_INTERCEPT, disable);
    }


    public static void setInterceptDisable(DexMethodNode dmn, boolean disable) {
        dmn.setExtraInfo(EXTRA_KEY_DISABLE_INTERCEPT, disable);
    }

    public static boolean getInterceptDisable(DexClassNode dcn) {
        return dcn.getExtraInfo(EXTRA_KEY_DISABLE_INTERCEPT, false);
    }

    public static boolean getInterceptDisable(DexMethodNode dmn) {
        return dmn.getExtraInfo(EXTRA_KEY_DISABLE_INTERCEPT, false);
    }



}
