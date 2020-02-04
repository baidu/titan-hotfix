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

package com.baidu.titan.core.patch.light.plum;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.pool.ApplicationDexPool;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.node.DexAnnotationNode;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexAnnotationVisitor;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

import java.util.HashSet;

/**
 * 用于对匿名内部类进行标记
 *
 * @author shanghuibo
 * @since 2019/01/27
 */
public class AnonymousClassMarker implements DexClassPoolNodeVisitor {

    /** 类中的匿名内部类集合 key*/
    private static final String ANONYMOUS_CLASS_SET = "anonymous_class_set";

    private ApplicationDexPool mClassPool;

    private TitanDexItemFactory mItemFactory;

    public AnonymousClassMarker(ApplicationDexPool classPool, TitanDexItemFactory itemFactory) {
        mClassPool = classPool;
        mItemFactory = itemFactory;
    }


    static boolean isAnonymousClass(ApplicationDexPool classPool, TitanDexItemFactory itemFactory, DexType type) {
        DexClassNode dcn = classPool.findClassFromAll(type);
        if (dcn == null) {
//            throw new IllegalArgumentException(type + " not found in specified class pool");
            return false;
        }
        return isAnonymousClass(itemFactory, dcn);
    }

    public static boolean isAnonymousClass(TitanDexItemFactory itemFactory, DexClassNode dcn) {
        boolean hasEnclosingMethodAnnotation = dcn.getClassAnnotations().stream()
                .anyMatch(dan -> dan.getType().equals(itemFactory.anonymousClassAnnotations.enclosingMethodType));
        boolean hasInnerClassAnnotation = dcn.getClassAnnotations().stream()
                .anyMatch(dan -> dan.getType().equals(itemFactory.anonymousClassAnnotations.innerClassType));
        return hasEnclosingMethodAnnotation && hasInnerClassAnnotation;
    }

    /**
     * 对class pool中的类进行判断和标记匿名内部类
     *
     */
    public void mark() {
        mClassPool.acceptProgram(this);
    }

    /**
     * 标记dex class node表示的类为匿名内部类
     *
     * @param classPool
     * @param itemFactory
     * @param dcn
     */
    private static void markAnonymous(ApplicationDexPool classPool, TitanDexItemFactory itemFactory, DexClassNode dcn) {
        DexClassNode outerDcn = getOuterClassNode(classPool, itemFactory, dcn);
        if (outerDcn != null) {
            HashSet<DexType> anonymousClassSet = outerDcn.getExtraInfo(ANONYMOUS_CLASS_SET, null);
            if (anonymousClassSet == null) {
                anonymousClassSet = new HashSet<>();
                outerDcn.setExtraInfo(ANONYMOUS_CLASS_SET, anonymousClassSet);
            }
            anonymousClassSet.add(dcn.type);
        }
    }

    static HashSet<DexType> getAnonymousClassType(DexClassNode dcn) {
        if (dcn == null) {
            return null;
        }
        return dcn.getExtraInfo(ANONYMOUS_CLASS_SET, null);
    }

    private static DexClassNode getOuterClassNode(ApplicationDexPool classPool,
                                                  TitanDexItemFactory itemFactory,
                                                  DexClassNode dcn) {
        if (!AnonymousClassMarker.isAnonymousClass(itemFactory, dcn)) {
            throw new IllegalArgumentException(dcn.type.toTypeDescriptor() + " is not an anonymous class");
        }
        DexAnnotationNode enclsingMethodAN = dcn.getClassAnnotations().stream()
                .filter(dan -> dan.getType().equals(itemFactory.anonymousClassAnnotations.enclosingMethodType))
                .findFirst()
                .get();
        final DexType[] outer = {null};
        enclsingMethodAN.accept(new DexAnnotationVisitor() {
            @Override
            public void visitMethod(DexString name, DexConst.ConstMethodRef methodRef) {
                super.visitMethod(name, methodRef);
                outer[0] = methodRef.getOwner();
            }
        });
        DexType outType = outer[0];
        return classPool.findClassFromAll(outType);
    }

    static DexType getOutClassType(ApplicationDexPool classPool, TitanDexItemFactory dexItemFactory, DexType type) {
        if (!AnonymousClassMarker.isAnonymousClass(classPool, dexItemFactory, type)) {
            throw new IllegalArgumentException(type.toTypeDescriptor() + " is not an anonymous class");
        }
        DexClassNode dcn = classPool.findClassFromAll(type);
        DexAnnotationNode enclsingMethodAN = dcn.getClassAnnotations().stream().filter(dan -> {
            return dan.getType().equals(dexItemFactory.anonymousClassAnnotations.enclosingMethodType);
        }).findFirst().get();
        final DexType[] outer = {null};
        enclsingMethodAN.accept(new DexAnnotationVisitor() {
            @Override
            public void visitMethod(DexString name, DexConst.ConstMethodRef methodRef) {
                super.visitMethod(name, methodRef);
                outer[0] = methodRef.getOwner();
            }
        });
        return outer[0];
    }

    @Override
    public void visitClass(DexClassNode dcn) {
        if (AnonymousClassMarker.isAnonymousClass(mItemFactory, dcn)) {
            markAnonymous(mClassPool, mItemFactory, dcn);
        }
    }

    @Override
    public void classPoolVisitEnd() {

    }
}
