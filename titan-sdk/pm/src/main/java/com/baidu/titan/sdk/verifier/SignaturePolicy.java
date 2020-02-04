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

package com.baidu.titan.sdk.verifier;

/**
 * 签名校验策略
 *
 * @author shanghuibo
 */
public enum SignaturePolicy {
    /** 不校验签名*/
    NO_SIGNATURE,
    /** 只校验V1版本签名*/
    V1_ONLY,
    /** 只校验V2版本签名*/
    V2_ONLY,
    /** 优先校验V2版本签名，如果V2版本签名不存在，校验V1签名*/
    V2_FIRST
}
