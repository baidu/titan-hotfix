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

import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexType;

/**
 *
 * 组件类名映射关系为一一对应
 *
 * @author zhangdi07@baidu.com
 * @since 2017/12/7
 */
public class DirectComponentMapper extends ComponentMapper {

    public DexItemFactory factory;

    public DirectComponentMapper(DexItemFactory factory) {
        this.factory = factory;
    }

    @Override
    public DexType map(DexType old) {
        return old;
    }


}
