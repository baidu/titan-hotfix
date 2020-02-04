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

package com.baidu.titan.core;

import com.baidu.titan.dex.DexConst;
import com.baidu.titan.dex.DexItemFactory;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存Titan中共用的类的实例
 *
 * @author zhangdi07@baidu.com
 * @since 2018/1/28
 */
public class TitanDexItemFactory extends DexItemFactory {

    public final BuddyInitHolderClass buddyInitHolderClass = new BuddyInitHolderClass();

    public final BuddyInitContextClass buddyInitContextClass = new BuddyInitContextClass();

    public final InitContextClass initContextClass = new InitContextClass();

    public final ActivityClass activityClass = new ActivityClass();

    public final ServiceClass serviceClass = new ServiceClass();

    public final BroadcastReceiverClass broadcastReceiverClass = new BroadcastReceiverClass();

    public final ContentProviderClass contentProviderClass = new ContentProviderClass();

    public final TitanMethods titanMethods = new TitanMethods();

    public final FieldHolderClass fieldHolderClass = new FieldHolderClass();

    public final ChangedFieldHolderClass changedFieldHolderClass = new ChangedFieldHolderClass();

    public final InterceptResultClass interceptResultClass = new InterceptResultClass();

    public final InterceptableClass interceptableClass = new InterceptableClass();

    public final SimpleInterceptorClass simpleInterceptorClass = new SimpleInterceptorClass();

    public final InstrumentedClass instrumentedClass = new InstrumentedClass();

    public final BuddyClass buddyClass = new BuddyClass();

    public final GenesisClass genesisClass = new GenesisClass();

    public final GenesisTypeAnnotation genesisTypeAnnotation = new GenesisTypeAnnotation();

    public final ClassClinitInterceptableClass classClinitInterceptableClass =
            new ClassClinitInterceptableClass();

    public final SimpleClassClinitInterceptorClass simpleClassClinitInterceptorClass =
            new SimpleClassClinitInterceptorClass();

    public final ClassInitInterceptorClass classInitInterceptorClass = new ClassInitInterceptorClass();

    public final ClassClinitInterceptorStorageClass classClinitInterceptorStorageClass =
            new ClassClinitInterceptorStorageClass();

    public final TitanRuntimeClass titanRuntimeClass = new TitanRuntimeClass();

    public final AnnotationClasses annotationClasses = new AnnotationClasses();

    public final PatchBaseLoaderClass patchBaseLoaderClass = new PatchBaseLoaderClass();

    public final PatchLoaderClass patchLoaderClass = new PatchLoaderClass();

    public final ChangedClass changedClass = new ChangedClass();

    public final InterceptorClass interceptorClass = new InterceptorClass();

    public final AnonymousClassAnnotations anonymousClassAnnotations = new AnonymousClassAnnotations();

    /** Ljava/lang/Class 类型相关声明*/
    public final JavaLangClass javaLangClass = new JavaLangClass();

    public class InterceptableClass {

        static final String TYPE_DESC = "Lcom/baidu/titan/sdk/runtime/Interceptable;";

        public final DexType type = createType(TYPE_DESC);

        private ConcurrentHashMap<String, DexConst.ConstMethodRef> mSpecialMethods =
                new ConcurrentHashMap<>();

        public DexConst.ConstMethodRef invokeUnInitMethod = DexConst.ConstMethodRef.make(
                type,
                createString("invokeUnInit"),
                voidClass.primitiveType,
                createTypes(new DexType[] {
                        integerClass.primitiveType,
                        initContextClass.type}));

        public DexConst.ConstMethodRef invokeCommonMethod = DexConst.ConstMethodRef.make(
                type,
                createString("invokeCommon"),
                interceptResultClass.type,
                createTypesVariable(
                        integerClass.primitiveType,
                        objectClass.type,
                        createArrayType(objectClass.type)));


        public DexConst.ConstMethodRef invokeInitBodyMethod = DexConst.ConstMethodRef.make(
                type,
                createString("invokeInitBody"),
                voidClass.primitiveType,
                createTypesVariable(
                        integerClass.primitiveType,
                        initContextClass.type));

        public DexConst.ConstMethodRef getInvokeSpecialMethod(DexType... types) {
            DexTypeList.Builder typesBuilder = DexTypeList.newBuilder();
            for (DexType type : types) {
                typesBuilder.addType(type);
            }
            return getInvokeSpecialMethod(typesBuilder.build());
        }

        public DexConst.ConstMethodRef getInvokeSpecialMethod(DexTypeList types) {

            StringBuilder specialMethodNameBuilder = new StringBuilder();
            specialMethodNameBuilder.append("invoke");

            DexTypeList.Builder paraTypesBuilder = DexTypeList.newBuilder();
            // method id
            paraTypesBuilder.addType(integerClass.primitiveType);
            // this obj
            paraTypesBuilder.addType(objectClass.type);

            types.forEach(type -> {

                switch (type.toShortDescriptor()) {
                    case VoidClass.SHORT_DESCRIPTOR: {
                        specialMethodNameBuilder.append(VoidClass.SHORT_DESCRIPTOR);

                        break;
                    }
                    case ReferenceType.SHORT_DESCRIPTOR:
                    case ArrayType.SHORT_DESCRIPTOR: {
                        specialMethodNameBuilder.append(ReferenceType.SHORT_DESCRIPTOR);
                        paraTypesBuilder.addType(objectClass.type);
                        break;
                    }
                    default: {
                        specialMethodNameBuilder.append(type.toShortDescriptor());
                        paraTypesBuilder.addType(type);
                        break;
                    }
                }
            });

            String specialMethodName = specialMethodNameBuilder.toString();

            DexConst.ConstMethodRef methodRef = DexConst.ConstMethodRef.make(
                    type,
                    createString(specialMethodName),
                    interceptResultClass.type,
                    paraTypesBuilder.build());

            DexConst.ConstMethodRef cachedMethodRef =
                    mSpecialMethods.putIfAbsent(specialMethodName, methodRef);

            return cachedMethodRef != null ? cachedMethodRef : methodRef;
        }

        public DexConst.ConstMethodRef getInvokeSpecialMethod(String shortDesc) {
            DexTypeList.Builder typeListBuilder = DexTypeList.newBuilder();
            for (int i = 0; i < shortDesc.length(); i++) {
                char shortType = shortDesc.charAt(i);
                switch (shortType) {
                    case VoidClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(voidClass.primitiveType);
                        break;
                    }
                    case ArrayType.SHORT_DESCRIPTOR:
                    case ReferenceType.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(objectClass.type);
                        break;
                    }
                    case BooleanClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(booleanClass.primitiveType);
                        break;
                    }
                    case ShortClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(shortClass.primitiveType);
                        break;
                    }
                    case DoubleClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(doubleClass.primitiveType);
                        break;
                    }
                    case LongClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(longClass.primitiveType);
                        break;
                    }
                    case CharacterClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(characterClass.primitiveType);
                        break;
                    }
                    case ByteClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(byteClass.primitiveType);
                        break;
                    }
                    case IntegerClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(integerClass.primitiveType);
                        break;
                    }
                    case FloatClass.SHORT_DESCRIPTOR: {
                        typeListBuilder.addType(floatClass.primitiveType);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("unkown short type " + shortType);
                    }
                }
            }
            return getInvokeSpecialMethod(typeListBuilder.build());
        }

    }

    public class SimpleInterceptorClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;");

    }

    public class BuddyInitContextClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/runtime/BuddyInitContext;");

        public final DexConst.ConstFieldRef genesisObjField = DexConst.ConstFieldRef.make(
                type,
                objectClass.type,
                createString("genesisObj"));

        public final DexConst.ConstFieldRef initMethodIdField = DexConst.ConstFieldRef.make(
                type,
                objectClass.type,
                createString("initMethodId"));

        public DexConst.ConstMethodRef nextMethod = DexConst.ConstMethodRef.make(
                type,
                createString("next"),
                buddyInitHolderClass.type,
                DexTypeList.empty());

        public DexConst.ConstMethodRef makeNextMethod = DexConst.ConstMethodRef.make(
                type,
                createString("makeNext"),
                buddyInitHolderClass.type,
                createTypesVariable(integerClass.primitiveType, integerClass.primitiveType));

        public DexConst.ConstMethodRef moveToFirstMethod = DexConst.ConstMethodRef.make(
                type,
                createString("moveToFirst"),
                buddyInitHolderClass.type,
                DexTypeList.empty());

        public DexConst.ConstMethodRef currentMethod = DexConst.ConstMethodRef.make(
                type,
                createString("current"),
                buddyInitHolderClass.type,
                DexTypeList.empty());

    }

    public class BuddyInitHolderClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/runtime/BuddyInitHolder;");

        public final DexConst.ConstFieldRef localsField = DexConst.ConstFieldRef.make(
                type,
                createArrayType(objectClass.type),
                createString("locals"));

        public final DexConst.ConstFieldRef parasField = DexConst.ConstFieldRef.make(
                type,
                createArrayType(objectClass.type),
                createString("paras"));
    }

    /**
     *  com.baidu.titan.runtime.InitContext
     */
    public class InitContextClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/runtime/InitContext;");

//        public final DexConst.ConstFieldRef thisObjField = DexConst.ConstFieldRef.make(
//                type,
//                objectClass.type,
//                createString("thisObj"));

        public final DexConst.ConstFieldRef initArgsField = DexConst.ConstFieldRef.make(
                type,
                createArrayType(objectClass.type),
                createString("initArgs"));

        public final DexConst.ConstFieldRef callArgsField = DexConst.ConstFieldRef.make(
                type,
                createArrayType(objectClass.type),
                createString("callArgs"));

        public final DexConst.ConstFieldRef localsField = DexConst.ConstFieldRef.make(
                type,
                createArrayType(objectClass.type),
                createString("locals"));

        public final DexConst.ConstFieldRef thisArgField = DexConst.ConstFieldRef.make(
                type,
                objectClass.type,
                createString("thisArg"));

        public final DexConst.ConstFieldRef flagField = DexConst.ConstFieldRef.make(
                type,
                integerClass.primitiveType,
                createString("flag"));


    }

    public class TitanRuntimeClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/runtime/TitanRuntime;");

        public final DexConst.ConstMethodRef newInitContextMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("newInitContext"),
                        initContextClass.type,
                        DexTypeList.empty());

        public final DexConst.ConstMethodRef setClassClinitInterceptorMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("setClassClinitInterceptor"),
                        voidClass.primitiveType,
                        DexTypeList.newBuilder()
                                .addType(classClinitInterceptableClass.type).build());

        public final DexConst.ConstMethodRef getThreadInterceptResultMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("getThreadInterceptResult"),
                        interceptResultClass.type,
                        DexTypeList.empty());

        public final DexConst.ConstMethodRef invokeInstanceMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("invokeInstanceMethod"),
                        objectClass.type,
                        DexTypeList.newBuilder()
                                .addType(objectClass.type)                  // Object receiver,
                                .addType(createArrayType(objectClass.type)) // Object[] params
                                .addType(createArrayType(classClass.type))  // Class[] parameterTypes,
                                .addType(stringClass.type)                  // String methodName
                                .build());

        public final DexConst.ConstMethodRef invokeStaticMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("invokeStaticMethod"),
                        objectClass.type,
                        DexTypeList.newBuilder()
                                .addType(classClass.type)                   // Class receiverClass,
                                .addType(createArrayType(objectClass.type)) // Object[] params
                                .addType(createArrayType(classClass.type))  // Class[] parameterTypes,
                                .addType(stringClass.type)                  // String methodName
                                .build());

        public final DexConst.ConstMethodRef setFieldMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("setField"),
                        voidClass.primitiveType,
                        DexTypeList.newBuilder()
                                .addType(objectClass.type)                  // Object targetObject,
                                .addType(objectClass.type)                  // Object value,
                                .addType(classClass.type)                   // Class targetClass,
                                .addType(stringClass.type)                  // String fieldName
                                .build());

        public final DexConst.ConstMethodRef getFieldMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("getField"),
                        objectClass.type,
                        DexTypeList.newBuilder()
                                .addType(objectClass.type)                  // Object targetObject,
                                .addType(classClass.type)                   // Class targetClass,
                                .addType(stringClass.type)                  // String fieldName
                                .build());

    }

    public class FieldHolderClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/runtime/FieldHolder;");

    }

    public class ChangedFieldHolderClass {

        public DexType getType(DexType orgType) {
            return appendTypeSuffix(orgType, "$fdh");
        }

        public final DexString fieldHolderRefFieldName = createString("sFieldHolderRef");

        public final DexType fieldHolderRefFieldType = createType("Ljava/lang/ref/WeakReference;");

        public final DexString getOrCreateFieldHolderFieldName =
                createString("getOrCreateFieldHolder");

        public final DexConst.ConstMethodRef getOrCreateFieldHolderMethod(DexType fieldHolderType,
                                                                          DexType orgType) {
            return DexConst.ConstMethodRef.make(
                    fieldHolderType,
                    getOrCreateFieldHolderFieldName,
                    fieldHolderType,
                    createTypesVariable(orgType));
        }

    }

    /**
     * 非具体类，表示插装类
     */
    public class InstrumentedClass {

        /**
         * 静态字段，存储Interceptable实例。
         */
        public final DexString interceptorFieldName =
                createString("$ic");
        /**
         * 实例字段，用于存储新增的实例字段
         */
        public final DexString fieldHolderFieldName =
                createString("$fh");

        public final DexConst.ConstFieldRef getFieldHolderField(DexType type) {
            return DexConst.ConstFieldRef.make(
                    type,
                    fieldHolderClass.type,
                    fieldHolderFieldName);
        }

    }

    public class BuddyClass {
        public final DexString genesisObjFieldName = createString("$genesisObj");
    }

    public class GenesisClass {
        public final DexString buddyObjFiledName = createString("$buddyObj");
    }


    public class GenesisTypeAnnotation {

        public final DexType type =
                createType("Lcom/baidu/titan/sdk/runtime/annotation/GenesisType;");

    }

    public class TitanMethods {

        public final DexString preInitMethodName = createString("$preInit");

    }

    public class ActivityClass {

        public final DexType type = createType("Landroid/app/Activity;");

    }

    public class ServiceClass {

        public final DexType type = createType("Landroid/app/Service;");

    }

    public class BroadcastReceiverClass {

        public final DexType type = createType("Landroid/content/BroadcastReceiver;");

    }

    public class ContentProviderClass {

        public final DexType type = createType("Landroid/content/ContentProvider;");

    }

    public class InterceptResultClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/runtime/InterceptResult;");

        public final DexConst.ConstMethodRef initMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("<init>"),
                        voidClass.primitiveType,
                        DexTypeList.empty());

        public final DexConst.ConstFieldRef objValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        objectClass.type,
                        createString("objValue"));

        public final DexConst.ConstFieldRef booleanValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        booleanClass.primitiveType,
                        createString("booleanValue"));

        public final DexConst.ConstFieldRef byteValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        byteClass.primitiveType,
                        createString("byteValue"));

        public final DexConst.ConstFieldRef shortValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        shortClass.primitiveType,
                        createString("shortValue"));

        public final DexConst.ConstFieldRef charValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        characterClass.primitiveType,
                        createString("charValue"));

        public final DexConst.ConstFieldRef intValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        integerClass.primitiveType,
                        createString("intValue"));

        public final DexConst.ConstFieldRef longValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        longClass.primitiveType,
                        createString("longValue"));

        public final DexConst.ConstFieldRef floatValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        floatClass.primitiveType,
                        createString("floatValue"));

        public final DexConst.ConstFieldRef doubleValueField =
                DexConst.ConstFieldRef.make(
                        type,
                        doubleClass.primitiveType,
                        createString("doubleValue"));

        public DexConst.ConstFieldRef getValueFieldForType(DexType type) {
            switch (type.toShortDescriptor()) {
                case VoidClass.SHORT_DESCRIPTOR: {
                    throw new IllegalArgumentException("void type is not support");
                }
                case ByteClass.SHORT_DESCRIPTOR: {
                    return byteValueField;
                }
                case IntegerClass.SHORT_DESCRIPTOR: {
                    return intValueField;
                }
                case ShortClass.SHORT_DESCRIPTOR: {
                    return shortValueField;
                }
                case CharacterClass.SHORT_DESCRIPTOR: {
                    return charValueField;
                }
                case DoubleClass.SHORT_DESCRIPTOR: {
                    return doubleValueField;
                }
                case FloatClass.SHORT_DESCRIPTOR: {
                    return floatValueField;
                }
                case LongClass.SHORT_DESCRIPTOR: {
                    return longValueField;
                }
                case BooleanClass.SHORT_DESCRIPTOR: {
                    return booleanValueField;
                }
                case ReferenceType.SHORT_DESCRIPTOR:
                case ArrayType.SHORT_DESCRIPTOR: {
                    return objValueField;
                }
                default: {
                    throw new IllegalArgumentException(type.toString());
                }
            }
        }

        public final DexConst.ConstFieldRef interceptorField =
                DexConst.ConstFieldRef.make(
                        type,
                        createType(InterceptableClass.TYPE_DESC),
                        createString("interceptor"));

        public final DexConst.ConstFieldRef flagsField =
                DexConst.ConstFieldRef.make(
                        type,
                        integerClass.primitiveType,
                        createString("flags"));

        public final DexConst.ConstMethodRef obtainMethod = DexConst.ConstMethodRef.make(
                type,
                createString("obtain"),
                type,
                DexTypeList.empty());

        public final DexConst.ConstMethodRef reecycleMethod = DexConst.ConstMethodRef.make(
                type,
                createString("recycle"),
                type,
                DexTypeList.empty());

    }

    public class ClassClinitInterceptableClass {

        public final DexType type = createType
                ("Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;");

        public final DexConst.ConstMethodRef invokeClinitMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("invokeClinit"),
                        interceptResultClass.type,
                        createTypesVariable(integerClass.primitiveType, stringClass.type));

        public final DexConst.ConstMethodRef invokePostClinitMethod =
                DexConst.ConstMethodRef.make(
                type,
                createString("invokePostClinit"),
                interceptResultClass.type,
                createTypesVariable(integerClass.primitiveType, stringClass.type));

    }

    public class SimpleClassClinitInterceptorClass {

        public final DexType type = createType
                ("Lcom/baidu/titan/sdk/runtime/SimpleClassClinitInterceptor;");

    }

    public class ClassInitInterceptorClass {

        public final DexType type = createType("" +
                "Lcom/baidu/titan/patch/ClassClinitInterceptor;");

        public final DexConst.ConstMethodRef invokeClinitMethod =
                DexConst.ConstMethodRef.make(
                        type,
                        createString("invokeClinit"),
                        interceptResultClass.type,
                        createTypesVariable(integerClass.primitiveType, stringClass.type));

    }


    public class ClassClinitInterceptorStorageClass {

        public final DexType type = createType
                ("Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptorStorage;");

        public final DexConst.ConstFieldRef interceptorField =
                DexConst.ConstFieldRef.make(
                        type,
                        classClinitInterceptableClass.type,
                        createString("$ic"));
    }

    public class PatchBaseLoaderClass {

        public final DexType type = createType("Lcom/baidu/titan/sdk/loader/BaseLoader;");


    }

    public class PatchLoaderClass {

        public final DexType type = createType("Lcom/baidu/titan/patch/PatchLoader;");

        public DexConst.ConstMethodRef applyMethod = DexConst.ConstMethodRef.make(
                type,
                createString("apply"),
                voidClass.primitiveType,
                DexTypeList.empty());

        public DexConst.ConstMethodRef applyInTimeMethod = DexConst.ConstMethodRef.make(
                type,
                createString("applyInTime"),
                voidClass.primitiveType,
                DexTypeList.empty());
    }

    public class ChangedClass {

        public final DexString staticInitMethodName = createString("$staticInit");

        public DexConst.ConstMethodRef staticInitMethodForType(DexType changedType) {
            return DexConst.ConstMethodRef.make(
                    changedType,
                    staticInitMethodName,
                    voidClass.primitiveType,
                    createTypesVariable(createArrayType(objectClass.type)));
        }

        public final DexString instanceInitBodyMethodName = createString("$instanceInitBody");
        public final DexString instanceUnInitMethodName = createString("$instanceUninit");

    }

    private DexType appendTypeSuffix(DexType type, String suffix) {
        String curTypeDesc = type.toTypeDescriptor();
        return createType(curTypeDesc.substring(0, curTypeDesc.length() - 1) + suffix + ";");
    }

    public class InterceptorClass {

        public DexType getInterceptorType(DexType orgType) {
            return appendTypeSuffix(orgType, "$iter");
        }

    }

    public class AnnotationClasses {

        public final DisableInterceptAnnotation disableInterceptAnnotation =
                new DisableInterceptAnnotation();

        public final GenesisInitMethod genesisInitMethod = new GenesisInitMethod();

        public class DisableInterceptAnnotation {

            public final DexType type = createType(
                    "Lcom/baidu/titan/sdk/runtime/annotation/DisableIntercept;");
        }

        public class GenesisInitMethod {

            public final DexType type =
                    createType("Lcom/baidu/titan/sdk/runtime/annotation/GenesisInitMethod;");

            public final DexString methodIdElementName = createString("methodId");

        }

    }

    /**
     * 标识一个类属于匿名内部类
     */
    public class AnonymousClassAnnotations {

        public final DexType enclosingMethodType = createType("Ldalvik/annotation/EnclosingMethod;");

        public final DexType innerClassType = createType("Ldalvik/annotation/InnerClass;");
    }

    /**
     * Ljava/lang/Class;类型声明
     * Ljava/lang/ClassNotFoundException;类型声明
     */
    public class JavaLangClass {
        public final DexType classType = createType("Ljava/lang/Class;");
        public final DexType classNotFoundType = createType("Ljava/lang/ClassNotFoundException;");

        /** Class.forName*/
        public DexConst.ConstMethodRef forName = DexConst.ConstMethodRef.make(
                classType,
                createString("forName"),
                classType,
                createTypesVariable(stringClass.type));

        /** ClassNotFoundException.printStackTrace */
        public DexConst.ConstMethodRef printStackTrace = DexConst.ConstMethodRef.make(
                classNotFoundType,
                createString("printStackTrace"),
                voidClass.primitiveType,
                DexTypeList.empty());
    }

}
