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

package com.baidu.titan.sdk.loader;

import dalvik.system.DexClassLoader;

/**
 * 具有委派功能的ClassLoader
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/26
 */

public class DelegateClassLoader extends DexClassLoader {

    private final ClassLoader mDelegateClassLoader;

    public DelegateClassLoader(String dexPath, String optimizedDirectory, String libraryPath,
                               ClassLoader parent, ClassLoader delegate) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
        this.mDelegateClassLoader = delegate;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        ClassLoader delegate = mDelegateClassLoader;
        if (delegate != null) {
            return delegate.loadClass(name);
        }
        throw new ClassNotFoundException("can not find class " + name);
    }
}
