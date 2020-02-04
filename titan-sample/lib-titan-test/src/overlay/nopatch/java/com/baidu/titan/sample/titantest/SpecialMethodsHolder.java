package com.baidu.titan.sample.titantest;

/**
 * 这个类中根据special method列表，定义了许多方法，包含了special method的各种参数列表
 *
 * @author xuduokai
 * @since 2019/02/13
 */
public class SpecialMethodsHolder {


    static String invokeS(short a) {
        return "old";
    }


    static String invokeD(double a) {
        return "old";
    }

    static String invokeC(char a) {
        return "old";
    }


    public String invokeV(int methodId, Object thisObj) {
        return "old";
    }


    public String invokeL(Object arg0) {
        return "old";
    }


    public String invokeLL(Object arg0, Object arg1) {
        return "old";
    }


    public String invokeI(int arg0) {
        return "old";
    }


    public String invokeLI(Object arg0, int arg1) {
        return "old";
    }


    public String invokeLLL(Object arg0, Object arg1,
                            Object arg2) {
        return "old";
    }


    public String invokeZ(boolean arg0) {
        return "old";
    }


    public String invokeIL(int arg0, Object arg1) {
        return "old";
    }


    public String invokeLZ(Object arg0, boolean arg1) {
        return "old";
    }


    public String invokeJ(long arg0) {
        return "old";
    }


    public String invokeLLLL(Object arg0, Object arg1,
                             Object arg2, Object arg3) {
        return "old";
    }


    public String invokeLLI(Object arg0, Object arg1,
                            int arg2) {
        return "old";
    }


    public String invokeII(int arg0, int arg1) {
        return "old";
    }


    public String invokeLII(Object arg0, int arg1, int arg2) {
        return "old";
    }


    public String invokeLIL(Object arg0, int arg1,
                            Object arg2) {
        return "old";
    }


    public String invokeF(float arg0) {
        return "old";
    }


    public String invokeLJ(Object arg0, long arg1) {
        return "old";
    }


    public String invokeILL(int arg0, Object arg1,
                            Object arg2) {
        return "old";
    }


    public String invokeLLZ(Object arg0, Object arg1,
                            boolean arg2) {
        return "old";
    }


    public String invokeLLLLL(Object arg0, Object arg1,
                              Object arg2, Object arg3, Object arg4) {
        return "old";
    }


    public String invokeIIL(int arg0, int arg1, Object arg2) {
        return "old";
    }


    public String invokeLIII(Object arg0, int arg1, int arg2,
                             int arg3) {
        return "old";
    }


    public String invokeJL(long arg0, Object arg1) {
        return "old";
    }


    public String invokeLF(Object arg0, float arg1) {
        return "old";
    }


    public String invokeZL(boolean arg0, Object arg1) {
        return "old";
    }


    public String invokeLLLI(Object arg0, Object arg1,
                             Object arg2, int arg3) {
        return "old";
    }


    public String invokeIIII(int arg0, int arg1, int arg2,
                             int arg3) {
        return "old";
    }


    public String invokeB(byte arg0) {
        return "old";
    }


    public String invokeIII(int arg0, int arg1, int arg2) {
        return "old";
    }


    public String invokeLLII(Object arg0, Object arg1,
                             int arg2, int arg3) {
        return "old";
    }


    public String invokeLLIL(Object arg0, Object arg1,
                             int arg2, Object arg3) {
        return "old";
    }


    public String invokeLILL(Object arg0, int arg1,
                             Object arg2, Object arg3) {
        return "old";
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
