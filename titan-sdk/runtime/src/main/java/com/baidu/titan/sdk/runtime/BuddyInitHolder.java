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
 *
 * 用于Buddy类继承链构造函数的参数和本地变量
 *
 * @author zhangdi07@baidu.com
 * @since 2017/12/4
 */
public class BuddyInitHolder {

    public BuddyInitHolder(int local, int para) {
        this.locals = new Object[local];
        this.paras = new Object[para];
    }

    public int methodId;

    public Object[] paras;

    public Object[] locals;
    /** 链表 */
    public BuddyInitHolder next;

}
