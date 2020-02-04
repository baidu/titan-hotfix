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

/**
 * @author zhangdi07@baidu.com
 * @since 2018/10/19
 */
public enum DiffStatus {

    /** 未变化 */
    UNCHANGED,
    /** 兼容的变化 */
    CHANGED_COMPATIBLE,
    /** 不兼容的变化 */
    CHANGED_INCOMPATIBLE;

    @Override
    public String toString() {
        switch (this) {
            case UNCHANGED: {
                return "unchanged";
            }
            case CHANGED_COMPATIBLE: {
                return "changed-compatible";
            }
            case CHANGED_INCOMPATIBLE: {
                return "changed_incompatible";
            }
            default: {
                return "unknown";
            }
        }
    }

}
