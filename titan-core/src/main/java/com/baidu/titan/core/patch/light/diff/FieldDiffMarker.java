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

import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;

/**
 *
 * field differ
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/5/4
 */
public class FieldDiffMarker {

    public static final int DIFF_UNCHANGED = 1;

    public static final int DIFF_CHANGED_COMPATIBLE_STATIC_VALUE_CHANGED = 2;

    public static final int DIFF_CHANGED_COMPATIBLE_STATIC_FINAL_VALUE_CHANGED = 2;

    public static final int DIFF_CHANGED_INCOMPATIBLE_STATIC_ACCESS_CHANGED = -1;

    public static final int DIFF_CHANGED_INCOMPATIBLE_STATIC_VALUE_CHANGED = -2;


    public static int diff(DexClassNode newClassNode,
                           DexClassNode oldClassNode,
                           DexFieldNode newFieldNode,
                           DexFieldNode oldFieldNode) {
        if (newFieldNode.isStaticFinal() != oldFieldNode.isStaticFinal()) {
            return DIFF_CHANGED_INCOMPATIBLE_STATIC_ACCESS_CHANGED;
        } else if (newFieldNode.isStatic() != oldFieldNode.isStatic()) {
            return DIFF_CHANGED_INCOMPATIBLE_STATIC_ACCESS_CHANGED;
        } else if (newFieldNode.isStaticFinal() && oldFieldNode.isStaticFinal()) {
            Object newFieldStaticValue = newFieldNode.staticValue;
            Object oldFieldStaticValue = oldFieldNode.staticValue;
            if (newFieldStaticValue != null
                    && !newFieldStaticValue.equals(oldFieldStaticValue)) {
                return DIFF_CHANGED_COMPATIBLE_STATIC_FINAL_VALUE_CHANGED;
            }
        } else if (newFieldNode.isStatic() && oldFieldNode.isStatic()) {
            Object newFieldStaticValue = newFieldNode.staticValue;
            Object oldFieldStaticValue = oldFieldNode.staticValue;
            if (newFieldStaticValue != null
                    && !newFieldStaticValue.equals(oldFieldStaticValue)) {
                return DIFF_CHANGED_COMPATIBLE_STATIC_VALUE_CHANGED;
            }
        }
        return DIFF_UNCHANGED;
    }

}
