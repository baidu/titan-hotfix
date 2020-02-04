package com.baidu.titan.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TryCatchSmali {
    void tryCatch() {
        try {
            File file = new File("tryCatch");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void tryCatchFinally() {
        InputStream in = null;
        try {
            in = new FileInputStream("tryCatchFinally");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
