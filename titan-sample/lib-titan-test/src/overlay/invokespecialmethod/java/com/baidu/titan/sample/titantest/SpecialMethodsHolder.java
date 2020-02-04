package com.baidu.titan.sample.titantest;

/**
 * 这个类中根据special method列表，定义了许多方法，包含了special method的各种参数列表
 *
 * 与nopatch中的类相比，所以方法的返回值都进行了修改，便于生成patch
 *
 * @author xuduokai
 * @since 2019/02/13
 */
public class SpecialMethodsHolder {


    static String invokeS(short a) {
        return "new";
    }


    static String invokeD(double a) {
        return "new";
    }

    static String invokeC(char a) {
        return "new";
    }


    public String invokeV(int methodId, Object thisObj) {
        return "new";
    }


    public String invokeL(Object arg0) {
        return "new";
    }


    public String invokeLL(Object arg0, Object arg1) {
        return "new";
    }


    public String invokeI(int arg0) {
        return "new";
    }


    public String invokeLI(Object arg0, int arg1) {
        return "new";
    }


    public String invokeLLL(Object arg0, Object arg1,
                            Object arg2) {
        return "new";
    }


    public String invokeZ(boolean arg0) {
        return "new";
    }


    public String invokeIL(int arg0, Object arg1) {
        return "new";
    }


    public String invokeLZ(Object arg0, boolean arg1) {
        return "new";
    }


    public String invokeJ(long arg0) {
        return "new";
    }


    public String invokeLLLL(Object arg0, Object arg1,
                             Object arg2, Object arg3) {
        return "new";
    }


    public String invokeLLI(Object arg0, Object arg1,
                            int arg2) {
        return "new";
    }


    public String invokeII(int arg0, int arg1) {
        return "new";
    }


    public String invokeLII(Object arg0, int arg1, int arg2) {
        return "new";
    }


    public String invokeLIL(Object arg0, int arg1,
                            Object arg2) {
        return "new";
    }


    public String invokeF(float arg0) {
        return "new";
    }


    public String invokeLJ(Object arg0, long arg1) {
        return "new";
    }


    public String invokeILL(int arg0, Object arg1,
                            Object arg2) {
        return "new";
    }


    public String invokeLLZ(Object arg0, Object arg1,
                            boolean arg2) {
        return "new";
    }


    public String invokeLLLLL(Object arg0, Object arg1,
                              Object arg2, Object arg3, Object arg4) {
        return "new";
    }


    public String invokeIIL(int arg0, int arg1, Object arg2) {
        return "new";
    }


    public String invokeLIII(Object arg0, int arg1, int arg2,
                             int arg3) {
        return "new";
    }


    public String invokeJL(long arg0, Object arg1) {
        return "new";
    }


    public String invokeLF(Object arg0, float arg1) {
        return "new";
    }


    public String invokeZL(boolean arg0, Object arg1) {
        return "new";
    }


    public String invokeLLLI(Object arg0, Object arg1,
                             Object arg2, int arg3) {
        return "new";
    }


    public String invokeIIII(int arg0, int arg1, int arg2,
                             int arg3) {
        return "new";
    }


    public String invokeB(byte arg0) {
        return "new";
    }


    public String invokeIII(int arg0, int arg1, int arg2) {
        return "new";
    }


    public String invokeLLII(Object arg0, Object arg1,
                             int arg2, int arg3) {
        return "new";
    }


    public String invokeLLIL(Object arg0, Object arg1,
                             int arg2, Object arg3) {
        return "new";
    }


    public String invokeLILL(Object arg0, int arg1,
                             Object arg2, Object arg3) {
        return "new";
    }

    boolean returnZ() {
        return true;
    }

    byte returnB() {
        return Byte.MAX_VALUE;
    }

    short returnS() {
        return Short.MAX_VALUE;
    }

    char returnC() {
        return Character.MAX_VALUE;
    }

    int returnI() {
        return Integer.MAX_VALUE;
    }

    long returnJ() {
        return Long.MAX_VALUE;
    }

    float returnF() {
        return Float.MAX_VALUE;
    }

    double returnD() {
        return Double.MAX_VALUE;
    }

    Object returnL() {
        return new Object();
    }
}
