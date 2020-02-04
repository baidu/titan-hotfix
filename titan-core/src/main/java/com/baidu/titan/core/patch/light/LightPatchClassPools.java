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

package com.baidu.titan.core.patch.light;

import com.baidu.titan.core.pool.ApplicationDexPool;

/**
 *
 * Patch Pools
 *
 * @author zhangdi07@baidu.com
 * @since 2018/9/27
 */
public class LightPatchClassPools {

    public ApplicationDexPool oldOrgClassPool;

    public ApplicationDexPool oldInstrumentedClassPool;
    /**  */
    public ApplicationDexPool newOrgClassPool;
    /** 拦截器类池 */
    public ApplicationDexPool interceptorClassPool;
    /** 变化类 */
    public ApplicationDexPool changedClassPool;
    /** 新增field */
    public ApplicationDexPool fieldHolderClassPool;
    /** Patch新增类 */
    public ApplicationDexPool addedClassPool;
    /** Patch Loader */
    public ApplicationDexPool patchLoaderClassPool;
    /** clinit拦截器 */
    public ApplicationDexPool clinitIntercepotroClassPool;

    public ApplicationDexPool rewriteClassPool;
    /** 可以进行懒加载的类池*/
    public ApplicationDexPool lazyInitClassPool;



}
