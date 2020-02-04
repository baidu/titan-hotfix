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
 * 工具列，用于对比两个列表的差异性
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/18
 */

public class PatchException extends RuntimeException {

    public PatchException() {
        super();
    }

    public PatchException(String detail) {
        super(detail);
    }

    public PatchException(Throwable cause) {
        super(cause);
    }

    public PatchException(String detail, Throwable cause) {
        super(detail, cause);
    }

}
