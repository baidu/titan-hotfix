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

package com.baidu.titan.sdk.runtime;


import com.baidu.titan.sdk.runtime.annotation.DisableIntercept;

import static com.baidu.titan.sdk.runtime.Log.logging;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 处理与运行时相关逻辑的公共类
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/6
 */
@DisableIntercept
public class TitanRuntime {

    public static Interceptable $ic;

    protected interface Logging {
        void log(Level level, String string);

        boolean isLoggable(Level level);

        void log(Level level, String string, Throwable throwable);
    }

    public static void setLogger(final Logger logger) {

        logging = new Log.Logging() {
            @Override
            public void log(Level level, String string) {
                logger.log(level, string);
            }

            @Override
            public boolean isLoggable(Level level) {
                return logger.isLoggable(level);
            }

            @Override
            public void log(Level level, String string,
                            Throwable throwable) {
                logger.log(level, string, throwable);
            }
        };
    }

    private static final ThreadLocal<WeakReference<InterceptResult>> sInterceptStorage = new ThreadLocal<>();

    private static final ThreadLocal<WeakReference<InitContext>> sInitContextStorage = new ThreadLocal<>();

    /**
     * 通过该方法，能够通过ThreadLocal的形式复用InterceptResult
     *
     * @return
     */
    public static InterceptResult getThreadInterceptResult() {
        InterceptResult res = null;
        WeakReference<InterceptResult> wr = sInterceptStorage.get();
        if (wr != null) {
            res = wr.get();
            if (res != null) {
                // 通过内联reset方法，较少一次方法调用，以获取更好性能
                // reset的目的是去除InterceptResult对上一个结果的强引用
                res.objValue = null;
                // res.reset();
                return res;
            }
        }

        res = new InterceptResult();
        sInterceptStorage.set(new WeakReference<>(res));
        return res;
    }

    public static InitContext newInitContext() {
        return new InitContext();
    }

    /**
     * set interceptor for <clinit> method
     * @param interceptor
     */
    public static void setClassClinitInterceptor(ClassClinitInterceptable interceptor) {
        ClassClinitInterceptorStorage.$ic = interceptor;
    }

    public static InitContext getInitContext() {
        return new InitContext();
    }

    
//    public static Object getStaticPrivateField(Class targetClass, String fieldName) {
//        return getPrivateField(null /* targetObject */, targetClass, fieldName);
//    }
//
//    public static void setStaticPrivateField(
//            Object value, Class targetClass,  String fieldName) {
//        setPrivateField(null /* targetObject */, value, targetClass, fieldName);
//    }

    public static void setField(
            Object targetObject,
             Object value,
            Class targetClass,
             String fieldName) {

        try {
            Field declaredField = getField(targetClass, fieldName);
            declaredField.set(targetObject, value);
        } catch (IllegalAccessException e) {
            if (logging != null) {
                logging.log(Level.SEVERE,
                        String.format("Exception during setPrivateField %s", fieldName), e);
            }
            throw new RuntimeException(e);
        }
    }

    
    public static Object getField(
            Object targetObject,
            Class targetClass,
             String fieldName) {

        try {
            Field declaredField = getField(targetClass, fieldName);
            return declaredField.get(targetObject);
        } catch (IllegalAccessException e) {
            if (logging != null) {
                logging.log(Level.SEVERE,
                        String.format("Exception during%1$s getField %2$s",
                                targetObject == null ? " static" : "",
                                fieldName), e);
            }
            throw new RuntimeException(e);
        }
    }


    
    private static Field getField(Class target, String name) {
        Field declareField = getFieldByName(target, name);
        if (declareField == null) {
            throw new RuntimeException(new NoSuchElementException(name));
        }
        declareField.setAccessible(true);
        return declareField;
    }

    public static Object invokeInstanceMethod(Object receiver,
                                               Object[] params,
                                               Class[] parameterTypes,
                                               String methodName) throws Throwable {

        if (logging!=null && logging.isLoggable(Level.FINE)) {
            logging.log(Level.FINE, String.format("protectedMethod:%s on %s", methodName, receiver));
        }
        try {
            Method toDispatchTo = getMethodByName(receiver.getClass(), methodName, parameterTypes);
            if (toDispatchTo == null) {
                throw new RuntimeException(new NoSuchMethodException(methodName));
            }
            toDispatchTo.setAccessible(true);
            return toDispatchTo.invoke(receiver, params);
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (IllegalAccessException e) {
            logging.log(Level.SEVERE, String.format("Exception while invoking %s", methodName), e);
            throw new RuntimeException(e);
        }
    }

    public static Object invokeStaticMethod(
            Class receiverClass,
            Object[] params,
            Class[] parameterTypes,
            String methodName) throws Throwable {

        if (logging!=null && logging.isLoggable(Level.FINE)) {
            logging.log(Level.FINE,
                    String.format("protectedStaticMethod:%s on %s", methodName, receiverClass.getName()));
        }
        try {
            Method toDispatchTo = getMethodByName(receiverClass, methodName, parameterTypes);
            if (toDispatchTo == null) {
                throw new RuntimeException(new NoSuchMethodException(
                        methodName + " in class " + receiverClass.getName()));
            }
            toDispatchTo.setAccessible(true);
            return toDispatchTo.invoke(null /* target */, params);
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (IllegalAccessException e) {
            logging.log(Level.SEVERE, String.format("Exception while invoking %s", methodName), e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T newForClass(Object[] params, Class[] paramTypes, Class<T> targetClass)
            throws Throwable {
        Constructor declaredConstructor;
        try {
            declaredConstructor = targetClass.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            logging.log(Level.SEVERE, "Exception while resolving constructor", e);
            throw new RuntimeException(e);
        }
        declaredConstructor.setAccessible(true);
        try {
            return targetClass.cast(declaredConstructor.newInstance(params));
        } catch (InvocationTargetException e) {
            // The called method threw an exception, rethrow
            throw e.getCause();
        } catch (InstantiationException e) {
            logging.log(Level.SEVERE, String.format("Exception while instantiating %s", targetClass), e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logging.log(Level.SEVERE, String.format("Exception while instantiating %s", targetClass), e);
            throw new RuntimeException(e);
        }
    }

    private static Field getFieldByName(Class<?> aClass, String name) {

        if (logging!= null && logging.isLoggable(Level.FINE)) {
            logging.log(Level.FINE, String.format("getFieldByName:%s in %s", name, aClass.getName()));
        }

        Class<?> currentClass = aClass;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    private static Method getMethodByName(Class<?> aClass, String name, Class[] paramTypes) {

        if (aClass == null) {
            return null;
        }

        Class<?> currentClass = aClass;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                // ignored.
            }
            currentClass = currentClass.getSuperclass();
            if (currentClass!= null && logging!=null && logging.isLoggable(Level.FINE)) {
                logging.log(Level.FINE, String.format(
                        "getMethodByName:Looking in %s now", currentClass.getName()));
            }

        }
        return null;
    }

    public static void trace(String s) {
        if (logging != null) {
            logging.log(Level.FINE, s);
        }
    }

    public static void trace(String s1, String s2) {
        if (logging != null) {
            logging.log(Level.FINE, String.format("%s %s", s1, s2));
        }
    }

    public static void trace(String s1, String s2, String s3) {
        if (logging != null) {
            logging.log(Level.FINE, String.format("%s %s %s", s1, s2, s3));
        }
    }

    public static void trace(String s1, String s2, String s3, String s4) {
        if (logging != null) {
            logging.log(Level.FINE, String.format("%s %s %s %s", s1, s2, s3, s4));
        }
    }
}
