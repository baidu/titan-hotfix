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

package com.baidu.titan.core.patch.full;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.extensions.DexSuperClassHierarchyFiller;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/4/12
 */
public class ComponentInitMethodSplitTransformation implements DexClassPoolNodeVisitor {

    private final ApplicationDexPool mNewAppPool;

    private final ApplicationDexPool mFullPatchPool;

    private final TitanDexItemFactory mFactory;

    private DexClassNode mCurrentClassNode;

    private boolean mIsSuperClassBuddyClass;

    private PatchBuddyClassHierarchy mBuddyClassHierarchy;

    public ComponentInitMethodSplitTransformation(ApplicationDexPool newAppPool,
                                   ApplicationDexPool fullPatchPool,
                                   TitanDexItemFactory factory,
                                   PatchBuddyClassHierarchy buddyClassHierarchy) {
        this.mNewAppPool = newAppPool;
        this.mFullPatchPool = fullPatchPool;
        this.mFactory = factory;
        this.mBuddyClassHierarchy = buddyClassHierarchy;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        // reset
        this.mCurrentClassNode = dcn;
        this.mIsSuperClassBuddyClass = false;


        DexClassLoader classLoader = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return mNewAppPool.findClassFromAll(type);
            }
        };

        PatchBuddyClassHierarchy.ComponentClassInfo componentClassInfo =
                mBuddyClassHierarchy.allComponents.get(dcn.type);

        if (componentClassInfo != null) {
            AndroidComponentFlag componentFlag = componentClassInfo.componentFlag;

            boolean isBuddyClass = componentFlag.hasFlags(
                    componentClassInfo.componetType, AndroidComponentFlag.FLAG_BUDDY);

            if (isBuddyClass) {
                return;
            }

            DexClassNode superClassNode = DexSuperClassHierarchyFiller.getSuperClass(dcn);
            AndroidComponentFlag superComponentFlag =
                    AndroidComponentMarker.getComponentFlag(superClassNode);

            if (superComponentFlag == null) {
                throw new IllegalStateException();
            }

            boolean isSuperClassBuddyClass = superComponentFlag.hasFlags(
                    componentClassInfo.componetType, AndroidComponentFlag.FLAG_BUDDY);


            List<DexMethodNode> newMethods = new ArrayList<>();


            dcn.getMethods().forEach(m -> {
                boolean isInitMethod = m.isInstanceInitMethod();

                if (componentClassInfo != null && isInitMethod) {
                    InitMethodSplitter splitter = new InitMethodSplitter(dcn, m, classLoader,
                            mFactory, mIsSuperClassBuddyClass);
                    int res = splitter.doSplit();

                    DexMethodNode initBodyMethod = splitter.getInitBodyMethod();

                    DexMethodNode preInitMethod = splitter.getPreInitMethod();

                    newMethods.add(initBodyMethod);

                    newMethods.add(preInitMethod);

                } else {
                    newMethods.add(m);
                }

            });

            dcn.setMethods(newMethods);
        }


    }

    @Override
    public void classPoolVisitEnd() {

    }
}
