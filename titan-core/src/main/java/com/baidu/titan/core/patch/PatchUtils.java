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

package com.baidu.titan.core.patch;

import com.baidu.titan.core.util.Utils;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;

import java.io.Closeable;
import java.io.IOException;

/**
 * 工具类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/18
 */

public class PatchUtils {

    public static int calculateParameterRegCount(String[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return 0;
        }
        int paraRegCount = 0;
        for (String paraType : parameterTypes) {
            char st = paraType.charAt(0);
            switch (st) {
                case 'D':
                case 'J': {
                    paraRegCount += 2;
                    break;
                }
                default: {
                    paraRegCount++;
                }
            }
        }
        return paraRegCount;
    }

    public static int calculateParameterRegCount(DexTypeList paraTypes) {
        int paraRegCount = 0;
        for (DexType paraType : paraTypes.types()) {
            char st = paraType.toTypeDescriptor().charAt(0);
            switch (st) {
                case 'D':
                case 'J': {
                    paraRegCount += 2;
                    break;
                }
                default: {
                    paraRegCount++;
                }
            }
        }
        return paraRegCount;
    }

    public static String typeToDesc(String type) {
        return "L" + type.replace(".", "/") + ";";
    }

    /**
     * transform type description to class name
     *
     * @param desc type description
     * @return class name
     */
    public static String descToType(String desc) {
        String type = desc.replace("/", ".");
        type = type.substring(1, type.length() - 1);
        return type;
    }

    @Deprecated
    public static DexType getInterceptorType(DexType orgType, DexItemFactory factory) {
        return Utils.appendTypeSuffix(orgType, "$iter", factory);
    }

    @Deprecated
    public static DexType getFieldHolderType(DexType orgType, DexItemFactory factory) {
        return Utils.appendTypeSuffix(orgType, "$fdh", factory);
    }

    public static DexType getOrgTypeFromInterceptorType(
            DexType interceptorType, DexItemFactory factory) {
        String interceptorTypeDesc = interceptorType.toTypeDescriptor();
        return factory.createType(
                interceptorTypeDesc.substring(0,
                        interceptorTypeDesc.length() - "$iter;".length()) + ";");
    }

    public static DexType getBuddyType(DexType orgType, DexItemFactory factory) {
        return Utils.appendTypeSuffix(orgType, "$buddy", factory);
    }

    public static DexType getGenesisType(DexType orgType, DexItemFactory factory) {
        return Utils.appendTypeSuffix(orgType, "$genesis", factory);
    }

    public static DexType getChangeType(DexType orgType, DexItemFactory factory) {
        return Utils.appendTypeSuffix(orgType, "$chg", factory);
    }

    /**
     * 关闭closeable 对象
     *
     * @param closeable 可关闭的对象
     * @return 关闭是否成功
     */
    public static boolean closeQuiet(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
