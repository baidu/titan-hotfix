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

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchArgument;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.linker.ClassLinker;
import com.baidu.titan.dex.linker.DexClassLoader;
import com.baidu.titan.dex.node.DexClassNode;

/**
 * Diff上下文相关
 *
 * @author shanghuibo
 * @since 2018/12/18
 */
public class DiffContext {

    public ApplicationDexPool newOrgAppPool;

    public ApplicationDexPool oldOrgAppPool;

    public ApplicationDexPool oldInstrumentAppPool;

    public ApplicationDexPool rewriteClassPool;

    public DexClassLoader classLoaderFromNewPool;

    public DexClassLoader classLoaderFromOldPool;

    public DexClassLoader classLoaderFromOldInstrumentPool;

    public TitanDexItemFactory dexItemFactory;

    public ClassLinker linker;

    private boolean supportFinalFieldChange;

    /** 不需要patch的类过滤器 */
    public PatchArgument.ClassPatchFilter classPatchFilter;
    /** patch 加载策略 */
    public int loadPolicy;

    public DiffContext(ApplicationDexPool newOrgAppPool,
                       ApplicationDexPool oldOrgAppPool,
                       ApplicationDexPool oldInstrumentAppPool,
                       ApplicationDexPool rewriteClassPool,
                       TitanDexItemFactory dexItemFactory, boolean supportFinalFieldChange,
                       PatchArgument.ClassPatchFilter classPatchFilter,
                       int loadPolicy) {
        this.dexItemFactory = dexItemFactory;
        this.newOrgAppPool = newOrgAppPool;
        this.oldOrgAppPool = oldOrgAppPool;
        this.oldInstrumentAppPool = oldInstrumentAppPool;
        this.rewriteClassPool = rewriteClassPool;
        this.classLoaderFromNewPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                DexClassNode rewriteDcn = rewriteClassPool.findClassFromAll(type);
                if (rewriteDcn != null) {
                    return rewriteDcn;
                }
                return newOrgAppPool.findClassFromAll(type);
            }
        };
        this.classLoaderFromOldPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return oldOrgAppPool.findClassFromAll(type);
            }
        };
        this.classLoaderFromOldInstrumentPool = new DexClassLoader() {
            @Override
            public DexClassNode findClass(DexType type) {
                return oldInstrumentAppPool.findClassFromAll(type);
            }
        };
        linker = new ClassLinker(dexItemFactory);
        this.supportFinalFieldChange = supportFinalFieldChange;

        this.classPatchFilter = classPatchFilter;

        this.loadPolicy = loadPolicy;
    }

    public boolean isSupportFinalFieldChange() {
        return supportFinalFieldChange;
    }
}
