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
 * 在<init>方法中，由于super.<init>只能在<init>方法中调用，所以不能直接将<init>方法中代码copy到$chg类中，
 * 在生成patch时，需要将<init>方法分为三部分：
 * 1. super.<init>前的代码，复制到preInit方法中
 * 2. super.<init>方法调用，仍在<init>方法中调用
 * 3. super.<init>后的代码，复制到initBody方法中
 *
 * 调用preInit方法时需要传入InitContext, 在preInit执行完成后，应将寄存器状态等保存到InitContext中
 * 调用super.<init>时，根据InitContext中保存的callArgs对传入super的参数进行修改
 * 调用initBody时，首先根据InitContext中保存的数据恢复寄存器状态，再执行后续逻辑
 *
 * @author zhangdi07
 * @since 2017/9/21
 */

public class InitContext {
    /** 当前<init>方法的参数 */
    public Object[] initArgs;
    /** 调用this.<init> 或 super.<init>方法的参数 */
    public Object[] callArgs;
    /** preInit阶段的locals参数 */
    public Object[] locals;
    /** 调用this.<init>或super.<init>后的this引用 */
    public Object thisArg;
    /** flag */
    public int flag;

}
