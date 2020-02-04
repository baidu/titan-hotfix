package com.baidu.titan.sample;

public class ConstructorSmali {
    private int a;

    public ConstructorSmali() {

    }

    protected ConstructorSmali(byte a) {
        this.a = a;
    }

    protected ConstructorSmali(short a) {
        this.a = a;
    }

    protected ConstructorSmali(int a) {
        this.a = a;
    }

    protected ConstructorSmali(long a) {
        this.a = (int) a;
    }

    protected ConstructorSmali(float a) {
        this.a = (int) a;
    }

    protected ConstructorSmali(double a) {
        this.a = (int) a;
    }

    protected ConstructorSmali(char a) {
        this.a = a;
    }

    protected ConstructorSmali(boolean a) {

    }

    ConstructorSmali(int a, int c) {
        this.a = a;
    }

    private ConstructorSmali(int a, int b, String s) {
        this.a = a;
    }


}
