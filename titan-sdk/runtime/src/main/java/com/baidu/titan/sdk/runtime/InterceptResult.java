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


import com.baidu.titan.sdk.runtime.annotation.DisableIntercept;

/**
 * 用于表示方法返回值，为了提高性能，这里包含了所有基本类型，和Object引用类型
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/6
 */
@DisableIntercept
public class InterceptResult {

    public boolean booleanValue;

    public byte byteValue;

    public short shortValue;

    public char charValue;

    public int intValue;

    public long longValue;

    public float floatValue;

    public double doubleValue;

    public Object objValue;

    public Interceptable interceptor;

    public int flags;

    private static final int MAX_POOL_SIZE = 50;

    private InterceptResult next;

    private static final Object sPoolSync = new Object();

    private static InterceptResult sPool;

    private static int sPoolSize = 0;

    public static InterceptResult obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                InterceptResult r = sPool;
                sPool = r.next;
                r.next = null;
                sPoolSize--;
                return r;
            }
        }
        return new InterceptResult();
    }

    public void recycle() {
        // reset
        this.objValue = null;
        this.interceptor = null;

        // recycle
        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                this.next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

}
