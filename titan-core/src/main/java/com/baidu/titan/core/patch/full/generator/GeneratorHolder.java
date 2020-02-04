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

/**
 *
 *  Generator相关信息
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/18
 */
public class GeneratorHolder {

    public final DexItemFactory factory;

    public final DexType type;

    public InterceptorGenerator interceptorGenerator;

    public ChangedClassGenerator changedClassGenerator;

    public BuddyGenerator buddyGenerator;

    public GeneratorHolder(DexType type, DexItemFactory factory) {
        this.type = type;
        this.factory = factory;
    }

}
