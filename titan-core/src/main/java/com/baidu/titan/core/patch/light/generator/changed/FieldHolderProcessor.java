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

package com.baidu.titan.core.patch.light.generator.changed;

import com.baidu.titan.core.TitanDexItemFactory;
import com.baidu.titan.core.patch.PatchUtils;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexRegister;
import com.baidu.titan.dex.DexRegisterList;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.Dops;
import com.baidu.titan.dex.extensions.DexCodeFormatVerifier;
import com.baidu.titan.dex.extensions.DexCodeRegisterCalculator;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexFieldNode;
import com.baidu.titan.dex.visitor.DexClassVisitor;
import com.baidu.titan.dex.visitor.DexClassVisitorInfo;
import com.baidu.titan.dex.visitor.DexCodeVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitor;
import com.baidu.titan.dex.visitor.DexFieldVisitorInfo;
import com.baidu.titan.dex.visitor.DexLabel;
import com.baidu.titan.dex.visitor.DexMethodVisitor;
import com.baidu.titan.dex.visitor.DexMethodVisitorInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * 处理新增字段逻辑
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/12/14
 */
public class FieldHolderProcessor {

    /** $fdh */
    private DexClassNode mFieldHolderClassNode;

    private TitanDexItemFactory mDexItemFactory;

    private LightChangedClassGenerator mHost;

    private List<DexFieldNode> mNewFields = new ArrayList<>();
    // TODO 增加外部传参和当前类判断的方式，确定使用哪一种方法
    private int mGetOrCreateFieldHolderWay = GET_OR_CREATE_FIELD_HOLDER_WAY_INSTRUMENT_FIELD;
    /** 通过插桩字段 */
    public static final int GET_OR_CREATE_FIELD_HOLDER_WAY_INSTRUMENT_FIELD = 0;
    /** 通过弱引用 */
    public static final int GET_OR_CREATE_FIELD_HOLDER_WAY_WEAK_REF = 1;

    public FieldHolderProcessor(TitanDexItemFactory dexItemFactory,
                                LightChangedClassGenerator host) {
        this.mDexItemFactory = dexItemFactory;
        this.mHost = host;
    }

    /**
     * 添加<clinit>方法
     *
     * @param fieldHolderClassVisitor $fdh class visitor
     */
    private void initClinitMethod(DexClassVisitor fieldHolderClassVisitor) {
        // init method
        DexMethodVisitor clinitMethod = fieldHolderClassVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        mFieldHolderClassNode.type,
                        mDexItemFactory.methods.staticInitMethodName,
                        DexTypeList.empty(),
                        mDexItemFactory.voidClass.primitiveType,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_STATIC
                                | DexAccessFlags.ACC_CONSTRUCTOR)));


        clinitMethod.visitBegin();

        DexCodeRegisterCalculator clinitCodeVisitor =
                new DexCodeRegisterCalculator(
                        true,
                        DexTypeList.empty(),
                        new DexCodeFormatVerifier(clinitMethod.visitCode()));


        clinitCodeVisitor.visitBegin();

        DexLabel tryStartLabel = new DexLabel();
        clinitCodeVisitor.visitLabel(tryStartLabel);

        int vClassNameReg = 0;

        String className = PatchUtils.descToType(mHost.mOldOrgClassNode.type.toTypeDescriptor());

        clinitCodeVisitor.visitConstInsn(
                Dops.CONST_STRING,
                DexRegisterList.make(DexRegister.makeLocalReg(vClassNameReg)),
                DexConst.ConstString.make(className));

        clinitCodeVisitor.visitConstInsn(
                Dops.INVOKE_STATIC,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vClassNameReg)),
                mDexItemFactory.javaLangClass.forName);

        DexLabel tryEndLabel = new DexLabel();
        clinitCodeVisitor.visitLabel(tryEndLabel);

        DexLabel returnLabel = new DexLabel();
        // goto :return
        clinitCodeVisitor.visitTargetInsn(Dops.GOTO, DexRegisterList.empty(), returnLabel);

        DexLabel catchLabel = new DexLabel();
        clinitCodeVisitor.visitLabel(catchLabel);

        int vExceptionReg = 0;

        // move-exception v0
        clinitCodeVisitor.visitSimpleInsn(Dops.MOVE_EXCEPTION,
                DexRegisterList.newBuilder()
                        .addReg(DexRegister.makeLocalReg(vExceptionReg))
                        .build());

        clinitCodeVisitor.visitConstInsn(
                Dops.INVOKE_VIRTUAL,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vExceptionReg)),
                mDexItemFactory.javaLangClass.printStackTrace);


        clinitCodeVisitor.visitLabel(returnLabel);
        clinitCodeVisitor.visitSimpleInsn(
                Dops.RETURN_VOID,
                DexRegisterList.EMPTY);

        // 记录trycatch位置
        clinitCodeVisitor.visitTryCatch(tryStartLabel, tryEndLabel,
                mDexItemFactory.createTypesVariable(mDexItemFactory.javaLangClass.classNotFoundType),
                new DexLabel[] {catchLabel}, null);

        clinitCodeVisitor.fillRegisterCount();

        clinitCodeVisitor.visitEnd();

        clinitMethod.visitEnd();

    }

    private void initInitMethod(DexClassVisitor fieldHolderClassVisitor) {
        // init method
        DexMethodVisitor initMethod = fieldHolderClassVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        mFieldHolderClassNode.type,
                        mDexItemFactory.methods.initMethodName,
                        DexTypeList.empty(),
                        mDexItemFactory.voidClass.primitiveType,
                        new DexAccessFlags(DexAccessFlags.ACC_CONSTRUCTOR,
                                DexAccessFlags.ACC_PUBLIC)));
        initMethod.visitBegin();
        DexCodeRegisterCalculator initCode = new DexCodeRegisterCalculator(
                false,
                DexTypeList.empty(),
                new DexCodeFormatVerifier(initMethod.visitCode()));

        initCode.visitBegin();

        final int pThisObjReg = 0;
        initCode.visitConstInsn(Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeParameterReg(pThisObjReg)),
                DexConst.ConstMethodRef.make(
                        mFieldHolderClassNode.superType,
                        mDexItemFactory.methods.initMethodName,
                        mDexItemFactory.voidClass.primitiveType,
                        DexTypeList.empty()));
        initCode.visitSimpleInsn(Dops.RETURN_VOID, DexRegisterList.empty());

        initCode.fillRegisterCount();
        initCode.visitEnd();

        initMethod.visitEnd();
    }

    private void initGetOrCreateFieldHolderMethodByWeakRef(DexClassVisitor fieldHolderClassVisitor) {
        DexFieldVisitor fieldRefVisitor = fieldHolderClassVisitor.visitField(new DexFieldVisitorInfo(
                mFieldHolderClassNode.type,
                mDexItemFactory.changedFieldHolderClass.fieldHolderRefFieldName,
                mDexItemFactory.changedFieldHolderClass.fieldHolderRefFieldType,
                new DexAccessFlags(DexAccessFlags.ACC_STATIC, DexAccessFlags.ACC_SYNTHETIC)));
        fieldRefVisitor.visitBegin();
        fieldRefVisitor.visitEnd();

        DexMethodVisitor getOrCreateFieldHolderMethod = fieldHolderClassVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        mFieldHolderClassNode.type,
                        mDexItemFactory.changedFieldHolderClass.getOrCreateFieldHolderFieldName,
                        mDexItemFactory.createTypesVariable(mHost.mNewOrgClassNode.type),
                        mFieldHolderClassNode.type,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_STATIC,
                                DexAccessFlags.ACC_SYNCHRONIZED)));

        getOrCreateFieldHolderMethod.visitBegin();

        DexCodeVisitor getOrCreateFieldHolderCode = getOrCreateFieldHolderMethod.visitCode();

        getOrCreateFieldHolderCode.visitBegin();
        // TODO 需要实现以下伪代码逻辑
        // WeakReference<ChangedFieldHolder> ref = sFieldHolderRef;
        // if (ref == null) {
        //    ref = WeakReference<ChangedFieldHolder>
        //    sFieldHolderRef = ref
        // }
        // ChangedFieldHolder result = ref.get()
        // if (result == null) {
        //    result = new ChangedFieldHolder()
        //    ref.set(result)
        // }
        // return result
        //
        getOrCreateFieldHolderCode.visitEnd();

        throw new UnsupportedOperationException("impl later");
    }

    private void initGetOrCreateFieldHolderMethod(
            DexClassVisitor fieldHolderClassVisitor) {
        switch (mGetOrCreateFieldHolderWay) {
            case GET_OR_CREATE_FIELD_HOLDER_WAY_INSTRUMENT_FIELD: {
                initGetOrCreateFieldHolderMethodByInstrumentField(fieldHolderClassVisitor);
                break;
            }
            case GET_OR_CREATE_FIELD_HOLDER_WAY_WEAK_REF: {
                initGetOrCreateFieldHolderMethodByWeakRef(fieldHolderClassVisitor);
                break;
            }
            default: {
                throw new IllegalStateException(
                        String.format("unknown way %d", mGetOrCreateFieldHolderWay));
            }
        }
    }

    private void initGetOrCreateFieldHolderMethodByInstrumentField(
            DexClassVisitor fieldHolderClassVisitor) {
        // getOrCreateFieldHolder Method
        // 该方法应该为同步方法，不同于JVM，在dalvik字节码中单独加上ACC_SYNCHRONIZED access flag
        // 是不起作用的，需要通过monitor-enter monitor-exit的字节码实现
        DexTypeList types = mDexItemFactory.createTypesVariable(mHost.mNewOrgClassNode.type);
        DexMethodVisitor getOrCreateFieldHolderMethod = fieldHolderClassVisitor.visitMethod(
                new DexMethodVisitorInfo(
                        mFieldHolderClassNode.type,
                        mDexItemFactory.changedFieldHolderClass.getOrCreateFieldHolderFieldName,
                        mDexItemFactory.createTypesVariable(mHost.mNewOrgClassNode.type),
                        mFieldHolderClassNode.type,
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC, DexAccessFlags.ACC_STATIC)));

        getOrCreateFieldHolderMethod.visitBegin();

        DexCodeRegisterCalculator getOrCreateFieldHolderCode = new DexCodeRegisterCalculator(
                true,
                types,
                new DexCodeFormatVerifier(getOrCreateFieldHolderMethod.visitCode()));

        getOrCreateFieldHolderCode.visitBegin();

        final int vFieldHolderReg = 0;
        final int pObjReg = 0;

        DexLabel returnLabel = new DexLabel();
        DexLabel monitorLabel = new DexLabel();
        DexLabel monitorExitLabel = new DexLabel();

        DexConst.ConstFieldRef fieldHolderFieldRef = mDexItemFactory.instrumentedClass
                .getFieldHolderField(mHost.mNewOrgClassNode.type);

        // 获取$fh field对象
        // iget-object v0, p0, $fh
        getOrCreateFieldHolderCode.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vFieldHolderReg),
                        DexRegister.makeParameterReg(pObjReg)),
                fieldHolderFieldRef);

        // 只有$fh为空的时候，进行跳转。因为除了第一次访问，后续都不为空，使用if-eqz可以加快执行速度
        // if-eqz v0 :monitor_enter
        getOrCreateFieldHolderCode.visitTargetInsn(Dops.IF_EQZ,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldHolderReg)),
                monitorLabel);

        // :return
        getOrCreateFieldHolderCode.visitLabel(returnLabel);

        getOrCreateFieldHolderCode.visitConstInsn(Dops.CHECK_CAST,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldHolderReg)),
                DexConst.ConstType.make(mFieldHolderClassNode.type));
        // return-object v0
        getOrCreateFieldHolderCode.visitSimpleInsn(Dops.RETURN_OBJECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldHolderReg)));

        // :monitor_enter
        getOrCreateFieldHolderCode.visitLabel(monitorLabel);
        // monitor-enter p0
        getOrCreateFieldHolderCode.visitSimpleInsn(Dops.MONITOR_ENTER,
                DexRegisterList.newBuilder()
                        .addReg(DexRegister.makeParameterReg(pObjReg))
                        .build());

        // :try_start
        DexLabel tryStartLabel = new DexLabel();
        getOrCreateFieldHolderCode.visitLabel(tryStartLabel);

        // 加锁后，重新获取一下$fh对象
        // iget-object v0, p0, $fh
        getOrCreateFieldHolderCode.visitConstInsn(
                Dops.IGET_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vFieldHolderReg),
                        DexRegister.makeParameterReg(pObjReg)),
                fieldHolderFieldRef);

        // 如果不为空，直接跳过创建对象
        // if-nez v0 :monitor_exit
        getOrCreateFieldHolderCode.visitTargetInsn(Dops.IF_NEZ,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldHolderReg)),
                monitorExitLabel);

        // 创建FieldHolder实例
        // new-instance v0 $fdh
        getOrCreateFieldHolderCode.visitConstInsn(Dops.NEW_INSTANCE,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldHolderReg)),
                DexConst.ConstType.make(mFieldHolderClassNode.type));
        // invoke-direct v0, $fdh.<init>()
        getOrCreateFieldHolderCode.visitConstInsn(Dops.INVOKE_DIRECT,
                DexRegisterList.make(DexRegister.makeLocalReg(vFieldHolderReg)),
                DexConst.ConstMethodRef.make(
                        mFieldHolderClassNode.type,
                        mDexItemFactory.methods.initMethodName,
                        mDexItemFactory.voidClass.primitiveType,
                        DexTypeList.empty()));
        // iput-object v0, p0, $fd
        getOrCreateFieldHolderCode.visitConstInsn(
                Dops.IPUT_OBJECT,
                DexRegisterList.make(
                        DexRegister.makeLocalReg(vFieldHolderReg),
                        DexRegister.makeParameterReg(pObjReg)),
                fieldHolderFieldRef);

        // :try_end
        DexLabel tryEndLabel = new DexLabel();
        getOrCreateFieldHolderCode.visitLabel(tryEndLabel);

        // :monitor_exit
        getOrCreateFieldHolderCode.visitLabel(monitorExitLabel);
        // monitor-exit p0
        getOrCreateFieldHolderCode.visitSimpleInsn(Dops.MONITOR_EXIT,
                DexRegisterList.newBuilder()
                        .addReg(DexRegister.makeParameterReg(pObjReg))
                        .build());

        // goto :return
        getOrCreateFieldHolderCode.visitTargetInsn(Dops.GOTO, DexRegisterList.empty(), returnLabel);

        // :catch_all
        DexLabel catchAllLabel = new DexLabel();
        getOrCreateFieldHolderCode.visitLabel(catchAllLabel);

        int vExceptionReg = 0;

        // move-exception v0
        getOrCreateFieldHolderCode.visitSimpleInsn(Dops.MOVE_EXCEPTION,
                DexRegisterList.newBuilder()
                        .addReg(DexRegister.makeLocalReg(vExceptionReg))
                        .build());

        // monitor-exit p0
        getOrCreateFieldHolderCode.visitSimpleInsn(Dops.MONITOR_EXIT,
                DexRegisterList.newBuilder()
                        .addReg(DexRegister.makeParameterReg(pObjReg))
                        .build());

        // throw v0
        getOrCreateFieldHolderCode.visitSimpleInsn(Dops.THROW,
                DexRegisterList.newBuilder()
                        .addReg(DexRegister.makeLocalReg(vExceptionReg))
                        .build());

        // 记录trycatch位置
        getOrCreateFieldHolderCode.visitTryCatch(tryStartLabel, tryEndLabel,
                DexTypeList.empty(), new DexLabel[0], catchAllLabel);

        getOrCreateFieldHolderCode.fillRegisterCount();

        getOrCreateFieldHolderCode.visitEnd();

        getOrCreateFieldHolderMethod.visitEnd();
    }

    public void addField(DexFieldNode newField) {
        this.mNewFields.add(newField);
    }

    /**
     * 完成新增字段，以及相关方法，并添加到class pool中
     */
    public void finish() {
        mFieldHolderClassNode = new DexClassNode(
                new DexClassVisitorInfo(
                        mDexItemFactory.changedFieldHolderClass.getType(mHost.mNewOrgClassNode.type),
                        mDexItemFactory.fieldHolderClass.type,
                        DexTypeList.empty(),
                        new DexAccessFlags(DexAccessFlags.ACC_PUBLIC)));
        DexClassVisitor fieldHolderClassVisitor = mFieldHolderClassNode.asVisitor();
        fieldHolderClassVisitor.visitBegin();

        initInitMethod(fieldHolderClassVisitor);
        initGetOrCreateFieldHolderMethod(fieldHolderClassVisitor);

        AtomicBoolean addClinit = new AtomicBoolean(false);
        this.mNewFields.forEach(field -> {

            DexAccessFlags flags = new DexAccessFlags(field.accessFlags);
            if (flags.containsOneOf(DexAccessFlags.ACC_STATIC)) {
                addClinit.set(true);
            }
            flags.clearFlags(DexAccessFlags.ACC_FINAL);
            flags.clearFlags(DexAccessFlags.ACC_PRIVATE);
            flags.clearFlags(DexAccessFlags.ACC_PROTECTED);
            flags.appendFlags(DexAccessFlags.ACC_PUBLIC);
            DexFieldVisitor dfv = fieldHolderClassVisitor.visitField(
                    new DexFieldVisitorInfo(
                            mFieldHolderClassNode.type,
                            field.name,
                            field.type,
                            flags));
            dfv.visitBegin();
            if (field.isStatic() && field.staticValue != null) {
                dfv.visitStaticValue(field.staticValue);
            }
            dfv.visitEnd();
        });

        if (addClinit.get()) {
            initClinitMethod(fieldHolderClassVisitor);
        }

        fieldHolderClassVisitor.visitEnd();

        mHost.mClassPools.fieldHolderClassPool.addProgramClass(this.mFieldHolderClassNode);
    }

}
