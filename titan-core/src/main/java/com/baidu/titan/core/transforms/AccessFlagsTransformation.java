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

package com.baidu.titan.core.transforms;

import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.node.DexMethodNode;
import com.baidu.titan.dex.visitor.DexClassNodeVisitor;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

/**
 *
 * 修改类、方法、字段的access flags。
 * 对于类：
 * 根据makeClassToPublic参数决定是否统一改成public
 * 对于方法：
 * <li>所有的 (private | package | protected | public) *静态* 方法，根据makeStaticMethodToPublic参数统一修改为public；</li>
 * <li>所有的 private *实例* 方法，不进行修改，否则就会有direct方法变为virtual方法，影响执行效率</li>
 * <li>所有的 (package | protected | public) instance *实例* 方法，根据 makeVirtualMethodToPublic参数，来控制是否改为public</li>
 * 对于字段：
 * 所有的access访问级别的字段，根据 makeFieldToPublic 参数决定是否变为public
 *
 *
 * @author zhangdi07
 * @author shanghuibo
 * @since 2017/9/5
 */

public class AccessFlagsTransformation implements DexClassPoolNodeVisitor, DexClassNodeVisitor {

    private boolean mMakeVirtualMethodToPublic = false;

    /**
     *
     * @param makeVirtualMethodToPublic 是否将所有虚方法设置为Public
     */
    public AccessFlagsTransformation(boolean makeClassToPublic,
                                     boolean makeVirtualMethodToPublic,
                                     boolean makeStaticMethodToPublic,
                                     boolean makeFieldToPublic) {
        this.mMakeVirtualMethodToPublic = makeVirtualMethodToPublic;
    }

    /**
     * access flag
     */
    protected enum AccessRight {

        PRIVATE, PACKAGE_PRIVATE, PROTECTED, PUBLIC;

        static AccessRight fromAccessFlag(int accessFlags) {
            if ((accessFlags & DexAccessFlags.ACC_PRIVATE) != 0) {
                return PRIVATE;
            }
            if ((accessFlags & DexAccessFlags.ACC_PROTECTED) != 0) {
                return PROTECTED;
            }
            if ((accessFlags & DexAccessFlags.ACC_PUBLIC) != 0) {
                return PUBLIC;
            }
            return PACKAGE_PRIVATE;
        }
    }


    @Override
    public void visitClass(DexClassNode dcn) {

        int access = dcn.accessFlags.getFlags();
        int newAccess = transformClassAccess(access);
        if (access != newAccess) {
            dcn.accessFlags.setFlags(newAccess);
        }

        dcn.accept(this);
    }

    @Override
    public void classPoolVisitEnd() {

    }

    /**
     * modify class access flags
     *
     * @param access
     * @return
     */
    private int transformClassAccess(int access) {
        AccessRight accessRight = AccessRight.fromAccessFlag(access);
        int fixedVisibility = access;
        if (accessRight == AccessRight.PACKAGE_PRIVATE) {
            fixedVisibility |= DexAccessFlags.ACC_PUBLIC;
        } else if (accessRight == AccessRight.PROTECTED) {
            fixedVisibility &= ~DexAccessFlags.ACC_PROTECTED;
            fixedVisibility |= DexAccessFlags.ACC_PUBLIC;
        } else if (accessRight == AccessRight.PRIVATE) {
            // for class access flag, private flag is illegal
            throw new IllegalStateException("private access for class is illegal!");
        }
        return fixedVisibility;
    }

    /**
     * modify method access flag
     *
     * 对于所有静态方法，access flag全部改为public，不会有运行效率问题
     * 对于实例方法，private方法是no-virtual的，为了保持执行效率，不做改动，
     * 对于protected和package-private方法，改为public,不会影响效率
     *
     * @param access
     * @return
     */
    private int transformMethodAccess(int access) {
        int fixedVisibility = access;
        boolean staticMethod = (access & DexAccessFlags.ACC_STATIC) != 0;
        if (staticMethod) {
            fixedVisibility &= ~DexAccessFlags.ACC_PRIVATE;
            fixedVisibility &= ~DexAccessFlags.ACC_PROTECTED;
            fixedVisibility |= DexAccessFlags.ACC_PUBLIC;
        } else {
            AccessRight accessRight = AccessRight.fromAccessFlag(access);
            if (accessRight != AccessRight.PRIVATE) {
                if (mMakeVirtualMethodToPublic) {
                    fixedVisibility &= ~DexAccessFlags.ACC_PROTECTED;
                    fixedVisibility &= ~DexAccessFlags.ACC_PRIVATE;
                    fixedVisibility |= DexAccessFlags.ACC_PUBLIC;
                }
            }
        }
        return fixedVisibility;
    }

    /**
     * modify field access
     *
     * @param access
     * @return
     */
    private int transformFieldAccess(int access) {
        int fixedVisibility = access;
        fixedVisibility &= ~DexAccessFlags.ACC_PRIVATE;
        fixedVisibility &= ~DexAccessFlags.ACC_PROTECTED;
        fixedVisibility |= DexAccessFlags.ACC_PUBLIC;

        return fixedVisibility;
    }


    @Override
    public void visitClassAnnotation(DexAnnotationNode dan) {

    }

    @Override
    public void visitMethod(DexMethodNode dmn) {
        int access = dmn.accessFlags.getFlags();
        int newAccess = transformMethodAccess(access);
        if (access != newAccess) {
            dmn.accessFlags.setFlags(newAccess);
        }
    }

    @Override
    public void visitField(DexFieldNode dfn) {
        int access = dfn.accessFlags.getFlags();
        int newAccess = transformFieldAccess(access);
        if (access != newAccess) {
            dfn.accessFlags.setFlags(newAccess);
        }
    }

    @Override
    public void visitClassNodeEnd() {

    }
}
