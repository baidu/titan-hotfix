package com.baidu.titan.sample;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChangeMethodCode {
    private String method1(String s, int size) {
        System.out.println(s);
        return "";
    }

    // 调用静态方法
    private String method2(String s, int size) {
        Thread.dumpStack();
        return "";
    }

    // 调用参数方法
    private String method3(String s, int size) {
        System.out.println(s.length());
        return "";
    }

    // 构造新对象
    private String method4(String s, int size) {
        List<Integer> list = new ArrayList<>(size);
        list.add(1);
        return "";
    }

    // 测试 try-catch
    private String method5(String s, int size) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(s)));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
