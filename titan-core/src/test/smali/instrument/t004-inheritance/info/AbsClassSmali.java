package com.baidu.titan.sample;

public abstract class AbsClassSmali {

    protected abstract void invoke();

    protected abstract void invoke(byte a);

    protected abstract void invoke(short a);

    protected abstract void invoke(int a);

    protected abstract void invoke(long a);

    protected abstract void invoke(float a);

    protected abstract void invoke(double a);

    protected abstract void invoke(char a);

    protected abstract void invoke(boolean a);

    protected abstract void invoke(String a);

    protected abstract String invokeLV();

    protected abstract void invokeVL(String s);

    protected abstract String invokeLL(String s);

    protected abstract String invokeLLI(String s, int a);

    protected abstract String invokeLIL(int a, String s);
}
