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
package com.baidu.titan.sdk.runtime;

/**
 * patch加载时，使用delegate机制，以支持patch异步加载并在要修复的类初始化时等待patch加载完成
 *
 * @author shanghuibo
 */
public abstract class ClassClinitInterceptorDelegate implements ClassClinitInterceptable {

    public volatile ClassClinitInterceptable delegate;

    /**
     * 等待patch加载完成
     *
     * @param hashCode 进行初始化的类的typeDesc对应的hashcode
     * @param typeDesc 进行初始化的类的类名描述
     * @return 是否执行了等待操作
     */
    public abstract boolean waitLoad(int hashCode, String typeDesc);

    @Override
    public InterceptResult invokeClinit(int hashCode, String typeDesc) {
        if (delegate == null) {
            waitLoad(hashCode, typeDesc);
        }
        if (delegate == null) {
            return null;
        }
        return delegate.invokeClinit(hashCode, typeDesc);
    }

    @Override
    public InterceptResult invokePostClinit(int hashCode, String typeDesc) {
        if (delegate == null) {
            waitLoad(hashCode, typeDesc);
        }
        if (delegate == null) {
            return null;
        }
        return delegate.invokePostClinit(hashCode, typeDesc);
    }
}
