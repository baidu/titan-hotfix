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
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.node.DexClassNode;

import java.util.HashMap;

/**
 * 对归一化后的类进行缓存，用以避免对比时重复归一
 *
 * @author shanghuibo
 * @since 2019/01/27
 */
public class AnonymousClassNodeCache {

    private HashMap<DexClassNode, byte[]> classNodeBytesCache = new HashMap<>();
    private HashMap<DexClassNode, byte[]> normalizedClassNodeBytesCache = new HashMap<>();
    private HashMap<DexClassNode, DexClassNode> normalizedClassNodeCache = new HashMap<>();

    public byte[] getClassNodeBytes(DexClassNode dcn) {
        byte[] dcnBytes = classNodeBytesCache.getOrDefault(dcn,
                AnonymousClassDiffMarker.getClassNodeBytes(dcn, true));
        classNodeBytesCache.putIfAbsent(dcn, dcnBytes);
        return dcnBytes;
    }

    public byte[] getNormalizedClassNodeBytes(ApplicationDexPool dexPool,
                                              TitanDexItemFactory dexItemFactory, DexClassNode dcn) {
        DexClassNode normalizedDcn = getNormalizedClassNode(dexPool, dexItemFactory, dcn);
        byte[] normalizedDcnBytes = normalizedClassNodeBytesCache.getOrDefault(normalizedDcn,
                AnonymousClassDiffMarker.getClassNodeBytes(normalizedDcn, true));
        normalizedClassNodeBytesCache.putIfAbsent(dcn, normalizedDcnBytes);
        return normalizedDcnBytes;
    }

    public DexClassNode getNormalizedClassNode(ApplicationDexPool dexPool,
                                               TitanDexItemFactory dexItemFactory, DexClassNode dcn) {
        DexClassNode normalizedDcn = normalizedClassNodeCache.getOrDefault(dcn,
                AnonymousClassNormalizer.normalizeAnonymousClass(dexPool, dexItemFactory, dcn));
        normalizedClassNodeCache.putIfAbsent(dcn, normalizedDcn);
        return normalizedDcn;
    }
}
