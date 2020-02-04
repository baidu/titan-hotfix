package com.baidu.titan.sample;

public class MethodOverloadingSmali {


    static void invokeS(short a) {

    }


    static void invokeD(double a) {

    }

    static void invokeC(char a) {

    }

    /**
     * invokeV
     */
    void invokeV() {
    }

    /**
     * invokeL
     */
    void invokeL(Object arg0) {
    }

    /**
     * invokeLL
     */
    void invokeLL(Object arg0, Object arg1) {
    }

    /**
     * invokeI
     */
    void invokeI(int arg0) {
    }

    /**
     * invokeLI
     */
    void invokeLI(Object arg0, int arg1) {
    }

    /**
     * invokeLLL
     */
    void invokeLLL(Object arg0, Object arg1, Object arg2) {
    }

    /**
     * invokeZ
     */
    void invokeZ(boolean arg0) {
    }

    /**
     * invokeIL
     */
    void invokeIL(int arg0, Object arg1) {
    }

    /**
     * invokeLZ
     */
    void invokeLZ(Object arg0, boolean arg1) {
    }

    /**
     * invokeJ
     */
    void invokeJ(long arg0) {
    }

    /**
     * invokeLLLL
     */
    void invokeLLLL(Object arg0, Object arg1, Object arg2, Object arg3) {
    }

    /**
     * invokeLLI
     */
    void invokeLLI(Object arg0, Object arg1, int arg2) {
    }

    /**
     * invokeII
     */
    void invokeII(int arg0, int arg1) {
    }

    /**
     * invokeLII
     */
    void invokeLII(Object arg0, int arg1, int arg2) {
    }

    /**
     * invokeLIL
     */
    void invokeLIL(Object arg0, int arg1, Object arg2) {
    }

    /**
     * invokeF
     */
    void invokeF(float arg0) {
    }

    /**
     * invokeLJ
     */
    void invokeLJ(Object arg0, long arg1) {
    }

    /**
     * invokeILL
     */
    void invokeILL(int arg0, Object arg1, Object arg2) {
    }

    /**
     * invokeLLZ
     */
    void invokeLLZ(Object arg0, Object arg1, boolean arg2) {
    }

    /**
     * invokeLLLLL
     */
    void invokeLLLLL(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
    }

    /**
     * invokeIIL
     */
    void invokeIIL(int arg0, int arg1, Object arg2) {
    }

    /**
     * invokeLIII
     */
    void invokeLIII(Object arg0, int arg1, int arg2, int arg3) {
    }

    /**
     * invokeJL
     */
    void invokeJL(long arg0, Object arg1) {
    }

    /**
     * invokeLF
     */
    void invokeLF(Object arg0, float arg1) {
    }

    /**
     * invokeZL
     */
    void invokeZL(boolean arg0, Object arg1) {
    }

    /**
     * invokeLLLI
     */
    void invokeLLLI(Object arg0, Object arg1, Object arg2, int arg3) {
    }

    /**
     * invokeIIII
     */
    void invokeIIII(int arg0, int arg1, int arg2, int arg3) {
    }

    /**
     * invokeB
     */
    void invokeB(byte arg0) {
    }

    /**
     * invokeIII
     */
    void invokeIII(int arg0, int arg1, int arg2) {
    }

    /**
     * invokeLLII
     */
    void invokeLLII(Object arg0, Object arg1, int arg2, int arg3) {
    }

    /**
     * invokeLLIL
     */
    void invokeLLIL(Object arg0, Object arg1, int arg2, Object arg3) {
    }

    /**
     * invokeLILL
     */
    void invokeLILL(Object arg0, int arg1, Object arg2, Object arg3) {
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
