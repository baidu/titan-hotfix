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

import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexType;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于保存type到generator的映射
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/19
 */
public class GenesisGeneratorHolder {

    public final DexType genesisType;

    public DexItemFactory factory;

    public final GeneratorHolder generatorHolder;

    public Map<DexType, GeneratorHolder> components = new HashMap<>();

    public GenesisGeneratorHolder(DexType genesisType, DexItemFactory factory) {
        this.genesisType = genesisType;
        this.factory = factory;
        this.generatorHolder = new GeneratorHolder(genesisType, factory);
    }

}
