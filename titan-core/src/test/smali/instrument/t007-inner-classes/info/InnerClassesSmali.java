package com.baidu.titan.sample;

import java.util.LinkedList;
import java.util.List;

public class InnerClassesSmali {
    List<Runnable> list = new LinkedList<>();

    private void doSomething() {
        list.add(new Runnable() {
            @Override
            public void run() {
                Thread.dumpStack();
            }
        });

        list.add(new PrivateStaticRunnable());

        list.add(new PrivateRunnable());
    }

    private static class PrivateStaticRunnable implements Runnable {

        @Override
        public void run() {
            Thread.dumpStack();
        }

        public static String getName() {
            return "PrivateStaticRunnable";
        }
    }

    private class PrivateRunnable implements Runnable {

        @Override
        public void run() {

        }
    }
}
