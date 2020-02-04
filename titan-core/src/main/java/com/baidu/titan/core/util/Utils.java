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

import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;

/**
 * @author zhangdi07
 * @since 2017/10/16
 */

public class Utils {

    public static int calParameterRegCount(String[] types) {
        if (types == null || types.length == 0) {
            return 0;
        }
        int count = 0;
        for (String type : types) {
            char shortType = type.charAt(0);
            switch (shortType) {
                case 'J':
                case 'D': {
                    count += 2;
                    break;
                }
                default: {
                    count += 1;
                    break;
                }
            }
        }
        return count;
    }

    public static int calParameterRegCount(DexTypeList types) {
        if (types.count() == 0) {
            return 0;
        }
        int count = 0;
        for (DexType type : types.types()) {
            char shortType = type.toTypeDescriptor().charAt(0);
            switch (shortType) {
                case 'J':
                case 'D': {
                    count += 2;
                    break;
                }
                default: {
                    count += 1;
                    break;
                }
            }
        }
        return count;
    }

    public static int calParameterRegCount(DexTypeList types, boolean staticMethod) {
        int count = staticMethod ? 0 : 1;
        if (types.count() == 0) {
            return count;
        }

        for (DexType type : types.types()) {
            char shortType = type.toTypeDescriptor().charAt(0);
            switch (shortType) {
                case 'J':
                case 'D': {
                    count += 2;
                    break;
                }
                default: {
                    count += 1;
                    break;
                }
            }
        }
        return count;
    }


    public static int calReturnTypeRegCount(String returnType) {
        char shortType = returnType.charAt(0);
        switch (shortType) {
            case 'J':
            case 'D': {
                return 2;
            }
            case 'V': {
                return 0;
            }
        }
        return 1;
    }

    public static int calReturnTypeRegCount(DexType returnType) {
        char shortType = returnType.toTypeDescriptor().charAt(0);
        switch (shortType) {
            case 'J':
            case 'D': {
                return 2;
            }
            case 'V': {
                return 0;
            }
        }
        return 1;
    }

    public static String appendTypeDescSuffix(String type, String suffix) {
        return type.substring(0, type.length() - 1) + suffix + ";";
    }

    public static DexType appendTypeSuffix(DexType type, String suffix, DexItemFactory factory) {
        String curTypeDesc = type.toTypeDescriptor();
        return factory.createType(
                curTypeDesc.substring(0, curTypeDesc.length() - 1) + suffix + ";");
    }

    public static String classNameToTypeDesc(String className) {
        return "L" + className.replace('.', '/') + ";";
    }

}
