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

package com.baidu.titan.core.filters;

import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

/**
 *
 *
 * @author zhangdi07@baidu.com
 * @since 2018/8/21
 */
public class ComponentClassFilter implements DexClassPoolNodeVisitor {

    private DexClassPoolNodeVisitor mDelegate;

    public ComponentClassFilter(DexClassPoolNodeVisitor delegate) {
        this.mDelegate = delegate;
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        AndroidComponentFlag componentFlag = AndroidComponentMarker.getComponentFlag(dcn);
        if (componentFlag != null) {
            mDelegate.visitClass(dcn);
        }

    }

    @Override
    public void classPoolVisitEnd() {
        mDelegate.classPoolVisitEnd();
    }
}
