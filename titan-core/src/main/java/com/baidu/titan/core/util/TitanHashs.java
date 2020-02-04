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

package com.baidu.titan.core.util;

import com.baidu.titan.dex.DexType;

/**
 * Titan相关的Hash函数工具类
 *
 * @author zhangdi07@baidu.com
 * @since 2018/12/14
 */
public class TitanHashs {

    /**
     *
     * 计算跟定类型的Hash值，如果修改Hash实现，需要做好instrumented apk与patch tool的版本兼容。
     *
     * @param type
     * @return
     */
    public static int type2HashCode(DexType type) {
        String typeDesc = type.toTypeDescriptor();
        int hash = 0;
        if (typeDesc.length() > 0) {
            char[] value = typeDesc.toCharArray();
            for (int i = 0; i < value.length; i++) {
                hash = 31 * hash + value[i];
            }
        }
        return hash;
    }

}
