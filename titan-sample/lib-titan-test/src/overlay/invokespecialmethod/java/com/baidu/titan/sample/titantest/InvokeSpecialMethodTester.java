package com.baidu.titan.sample.titantest;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于测试special 类型method的调用
 *
 * special method是对热门应用中的方法调用进行统计，将最常使用的若干参数列表类型如：V, L, LL等，当作special method处理
 *
 * @author shanghuibo
 * @since 2019/02/13
 */
public class InvokeSpecialMethodTester {

    /**
     * 基本类型的class
     */
    private static final Map<Class<?>, Object> primitiveClazz; // 基本类型的class

    static {
        primitiveClazz = new HashMap<>();
        primitiveClazz.put(int.class, Integer.MAX_VALUE);
        primitiveClazz.put(byte.class, Byte.MAX_VALUE);
        primitiveClazz.put(char.class, Character.MAX_VALUE);
        primitiveClazz.put(short.class, Short.MAX_VALUE);
        primitiveClazz.put(long.class, Long.MAX_VALUE);
        primitiveClazz.put(float.class, Float.MAX_VALUE);
        primitiveClazz.put(double.class, Double.MAX_VALUE);
        primitiveClazz.put(boolean.class, true);
    }

    /**
     * 测试对special method的调用
     */
    public static void testInvokeSpecialMethod() {
        Class overloadClass = SpecialMethodsHolder.class;
        SpecialMethodsHolder methodOverloading = new SpecialMethodsHolder();
        Method[] methods = overloadClass.getDeclaredMethods();
        Log.d("TitanTestHelper", "method count: " + methods.length);
        for (Method method : methods) {
            try {
                Object[] paras = new Object[method.getParameterTypes().length];
                Class[] parasClass = method.getParameterTypes();

                for (int i = 0; i < method.getParameterTypes().length; i++) {
                    paras[i] = getInstance(parasClass[i]);

                }
                Object object = method.invoke(methodOverloading, paras);

                Log.d("TitanTestHelper", "method name: " + method.getName() + " result: " + object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得类的实例对象
     * @param cls Class
     * @return 类的实例对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static Object getInstance(Class cls) throws IllegalAccessException, InstantiationException {
        Object o = primitiveClazz.get(cls);
        if (o == null) {
            return cls.newInstance();
        } else {
            return o;
        }
    }
}
