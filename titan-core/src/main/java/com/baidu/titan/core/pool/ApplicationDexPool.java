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

package com.baidu.titan.core.pool;

import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.MultiDexFileBytes;
import com.baidu.titan.dex.extensions.DexClassKindMarker;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexClassPoolNode;
import com.baidu.titan.dex.node.MultiDexFileNode;
import com.baidu.titan.dex.reader.MultiDexFileReader;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;
import com.baidu.titan.jvm.reader.JvmClassFileReader;


/**
 * 用于保存apk中所有class及系统库中的class
 *
 * @author zhangdi07
 * @since 2017/11/4
 */

public class ApplicationDexPool {

    private DexClassPoolNode mLibraryClassPool;

    private DexClassPoolNode mProgramClassPool;

    private DexItemFactory mFactory;

    public ApplicationDexPool(DexItemFactory factory) {
        this.mFactory = factory;
        this.mLibraryClassPool = new DexClassPoolNode();
        this.mProgramClassPool = new DexClassPoolNode();
        initPrimitiveTypes();
    }

    private void initPrimitiveTypes() {
        addPrimitiveType(mFactory.booleanClass.primitiveType);
        addPrimitiveType(mFactory.byteClass.primitiveType);
        addPrimitiveType(mFactory.shortClass.primitiveType);
        addPrimitiveType(mFactory.integerClass.primitiveType);
        addPrimitiveType(mFactory.longClass.primitiveType);
        addPrimitiveType(mFactory.floatClass.primitiveType);
        addPrimitiveType(mFactory.doubleClass.primitiveType);
        addPrimitiveType(mFactory.voidClass.primitiveType);
    }

    private void addPrimitiveType(DexType primitiveType) {
        DexClassNode dcn = new DexClassNode(
                primitiveType,
                new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_FINAL |
                        DexAccessFlags.ACC_ABSTRACT),
                null,
                DexTypeList.empty());
        this.mLibraryClassPool.addClass(dcn);
    }


    public DexClassPoolNode getProgramClassPool() {
        return this.mProgramClassPool;
    }

    public DexClassPoolNode getLibraryClassPool() {
        return this.mLibraryClassPool;
    }

    public void acceptProgram(DexClassPoolNodeVisitor visitor) {
        this.mProgramClassPool.accept(visitor);
    }

    public void acceptLibrary(DexClassPoolNodeVisitor visitor) {
        this.mLibraryClassPool.accept(visitor);
    }

    public void acceptAll(DexClassPoolNodeVisitor visitor) {

        mLibraryClassPool.accept(new NoVisitEndClassPoolVisitor(visitor));

        mProgramClassPool.accept(new NoVisitEndClassPoolVisitor(visitor));

        visitor.classPoolVisitEnd();
    }

    public DexClassNode findClassFromAll(DexType type) {
        DexClassNode dcn = mLibraryClassPool.getClass(type);
        if (dcn == null) {
            dcn = mProgramClassPool.getClass(type);
        }
        return dcn;
    }

    private static class NoVisitEndClassPoolVisitor implements DexClassPoolNodeVisitor {

        private DexClassPoolNodeVisitor mDelegate;

        public NoVisitEndClassPoolVisitor(DexClassPoolNodeVisitor delegate) {
            this.mDelegate = delegate;
        }

        @Override
        public void visitClass(DexClassNode dcn) {
            this.mDelegate.visitClass(dcn);
        }

        @Override
        public void classPoolVisitEnd() {
            // do noting
        }
    }

    public void addProgramClass(DexClassNode dcn) {
        DexClassKindMarker.setClassKind(dcn, DexClassKindMarker.ClassKind.CLASS_KIND_PROGRAM);
        mProgramClassPool.addClass(dcn);
    }

    public void fillProgramDexs(MultiDexFileBytes multiDexFileBytes) {
        MultiDexFileReader mdfr = new MultiDexFileReader(this.mFactory);
        multiDexFileBytes.forEach((dexId, dexFileBytes) -> {
            mdfr.addDexContent(dexId, dexFileBytes.getDexFileBytes());

        });
        MultiDexFileNode mdfn = new MultiDexFileNode();
        mdfr.accept(mdfn.asVisitor());
        mdfn.accept(new DexClassPoolFiller(mProgramClassPool));

        // mark as program class
        mProgramClassPool.accept(
                new DexClassKindMarker(DexClassKindMarker.ClassKind.CLASS_KIND_PROGRAM));

    }

    public void fillLibraryClass(byte[] classBytes) {
        JvmClassFileReader reader = new JvmClassFileReader(classBytes, this.mFactory);
        DexClassNode dcn = reader.read();
        mLibraryClassPool.addClass(dcn);
    }

}
