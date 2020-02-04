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
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.node.DexMethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/4/16
 */
public class BuddyClassInitMethodSplitterTransformation {

    private final ApplicationDexPool mNewAppPool;

    private final ApplicationDexPool mFullPatchPool;

    private final TitanDexItemFactory mFactory;

    private PatchBuddyClassHierarchy mBuddyClassHierarchy;

    public BuddyClassInitMethodSplitterTransformation(ApplicationDexPool newAppPool,
                                                  ApplicationDexPool fullPatchPool,
                                                  TitanDexItemFactory factory,
                                                  PatchBuddyClassHierarchy buddyClassHierarchy) {
        this.mNewAppPool = newAppPool;
        this.mFullPatchPool = fullPatchPool;
        this.mFactory = factory;
        this.mBuddyClassHierarchy = buddyClassHierarchy;
    }


    public void spliteAll() {
        mBuddyClassHierarchy.componentBuddyMap.forEach((componentType, buddySet) -> {
            buddySet.buddyClasses.forEach((buddyType, buddyHolder) -> {

                List<DexMethodNode> newMethods = new ArrayList<>();

                buddyHolder.buddyClassNode.getMethods().stream()
                        .forEach(m -> {
                            if (m.isInstanceInitMethod()) {
                                BuddyInitMethodSplitter splitter = new BuddyInitMethodSplitter(
                                        buddyHolder.buddyClassNode,
                                        m,
                                        mFactory,
                                        buddyHolder);
                                splitter.doSplit();
                                newMethods.add(splitter.getPreInitMethod());
                                newMethods.add(splitter.getInitBodyMethod());

                            } else {
                                newMethods.add(m);
                            }
                        });

                buddyHolder.buddyClassNode.setMethods(newMethods);

            });

        });
    }


}
