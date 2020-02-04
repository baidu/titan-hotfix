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

package com.baidu.titan.core.tests;

import com.baidu.titan.core.util.ZipUtil;
import com.baidu.titan.dex.DexConstant;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.reader.MultiDexFileReader;
import com.baidu.titan.dex.visitor.DexAnnotationVisitor;
import com.baidu.titan.dex.visitor.DexAnnotationVisitorInfo;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitorInfo;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexFileVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;
import com.baidu.titan.dex.visitor.MultiDexFileVisitor;
import com.baidu.titan.dex.writer.MultiDexFileWriter;

import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by zhangdi07 on 2018/3/15.
 */

public class DexSizeTest {

    public DexSizeTest() {
    }

    @Test
    public void test() throws Exception {
        File apkFile = new File("/Users/zhangdi07/dev/titan/size/release.apk");
        File outApkFile = new File("/Users/zhangdi07/dev/titan/size/release-size-3.apk");

        DexItemFactory factory = new DexItemFactory();
        MultiDexFileReader reader = new MultiDexFileReader(factory);
        Map<Integer, byte[]> dexContents = ZipUtil.getDexContentsFromApk(apkFile);
        dexContents.forEach((idx, bytes) -> {
            reader.addDexContent(idx, bytes);
        });

        HashSet<String> annotations = new HashSet<>();
//        annotations.add(DexConstant.ANNOTATION_SIGNATURE);
//        annotations.add(DexConstant.ANNOTATION_MEMBERCLASSES);
//        annotations.add(DexConstant.ANNOTATION_ENCLOSINGMETHOD);
//        annotations.add(DexConstant.ANNOTATION_THROWS);
//        annotations.add(DexConstant.ANNOTATION_INNERCLASS);
//        annotations.add(DexConstant.ANNOTATION_ENCLOSINGCLASS);




        MultiDexFileWriter writer = new MultiDexFileWriter();

        reader.accept(new MultiDexFileVisitor(writer) {

            @Override
            public DexFileVisitor visitDexFile(int dexId) {
                DexFileVisitor superVisitor = super.visitDexFile(dexId);
                return new DexFileVisitor(superVisitor) {
                    @Override
                    public DexClassVisitor visitClass(DexClassVisitorInfo classInfo) {
                        DexClassVisitor dcv = super.visitClass(classInfo);
                        return new DexClassVisitor(dcv) {
                            @Override
                            public DexAnnotationVisitor visitAnnotation(DexAnnotationVisitorInfo annotationInfo) {
                                if (annotations.contains(annotationInfo.type.toTypeDescriptor())) {
                                    return null;
                                }
                                return super.visitAnnotation(annotationInfo);
                            }

                            @Override
                            public DexFieldVisitor visitField(DexFieldVisitorInfo fieldInfo) {
                                DexFieldVisitor dfv = super.visitField(fieldInfo);
                                return new DexFieldVisitor(dfv) {
                                    @Override
                                    public DexAnnotationVisitor visitAnnotation(DexAnnotationVisitorInfo annotation) {
                                        if (annotations.contains(annotation.type.toTypeDescriptor())) {
                                            return null;
                                        }
                                        return super.visitAnnotation(annotation);
                                    }
                                };
                            }

                            @Override
                            public DexMethodVisitor visitMethod(DexMethodVisitorInfo methodInfo) {
                                DexMethodVisitor dmv = super.visitMethod(methodInfo);
                                return new DexMethodVisitor(dmv) {
                                    @Override
                                    public DexAnnotationVisitor visitAnnotation(DexAnnotationVisitorInfo annotationInfo) {
                                        if (annotations.contains(annotationInfo.type.toTypeDescriptor())) {
                                            return null;
                                        }
                                        return super.visitAnnotation(annotationInfo);
                                    }

                                    @Override
                                    public DexAnnotationVisitor visitParameterAnnotation(int parameter, DexAnnotationVisitorInfo annotationInfo) {
                                        if (annotations.contains(annotationInfo.type.toTypeDescriptor())) {
                                            return null;
                                        }
                                        return super.visitParameterAnnotation(parameter, annotationInfo);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        });

//        Map<Integer, byte[]> outputs = writer.getMultiDexContent();

//        ZipUtil.writeDexBytesToApk(outApkFile, outputs);
    }

}
