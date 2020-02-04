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

/**
 *
 * Patch生成策略枚举类
 *
 * @author zhangdi07@baidu.com
 * @since 2018/8/21
 */
public enum PatchPolicy {
    /**
     * 轻量Patch，方法级热修复
     */
    PATCH_POLICY_LIGHT_ONLY,
    /**
     * 全量Patch，整体热更新
     */
    PATCH_POLICY_FULL_ONLY,
    /**
     * 先尝试轻量热修复，在尝试全量热更新
     */
    PATCH_POLICY_LIGHT_THEN_FULL

}
