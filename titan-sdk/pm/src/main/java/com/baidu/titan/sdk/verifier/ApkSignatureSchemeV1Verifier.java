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

package com.baidu.titan.sdk.verifier;

import android.util.Log;

import com.baidu.titan.sdk.config.TitanConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * v1版本签名校验
 *
 * @author shanghuibo
 */
public class ApkSignatureSchemeV1Verifier {
    /** debug tag*/
    private static final String TAG = "SignatureVerifierV1";
    /** sync lock */
    private static Object mSync = new Object();
    /** shared read buffer*/
    private static WeakReference<byte[]> sReadBuffer;

    /**
     * Verifies APK Signature Scheme v1 signatures of the provided APK and returns the certificates
     *
     */
    public static Certificate[] verify(File apkFile) {


        WeakReference<byte[]> readBufferRef;
        byte[] readBuffer = null;
        synchronized (mSync) {
            readBufferRef = sReadBuffer;
            if (readBufferRef != null) {
                sReadBuffer = null;
                readBuffer = readBufferRef.get();
            }
            if (readBuffer == null) {
                readBuffer = new byte[8192];
                readBufferRef = new WeakReference<byte[]>(readBuffer);
            }
        }

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(apkFile);
            Certificate[] certs = null;

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry je = entries.nextElement();
                if (je.isDirectory()) {
                    continue;
                }

                final String name = je.getName();
                if (name.startsWith("META-INF/")) {
                    continue;
                }

                final Certificate[] localCerts = loadCertificates(jarFile, je, readBuffer);
                if (localCerts == null) {
                    if (TitanConfig.DEBUG) {
                        Log.e(TAG, "Package " + apkFile.getAbsolutePath()
                                + " has no certificates at entry " + je.getName() + "; ignoring!");
                    }
                    return null;
                } else if (certs == null) {
                    certs = localCerts;
                } else {
                    // Ensure all certificates match.
                    for (int i = 0; i < certs.length; i++) {
                        boolean found = false;
                        for (int j = 0; j < localCerts.length; j++) {
                            if (certs[i] != null && certs[i].equals(localCerts[j])) {
                                found = true;
                                break;
                            }
                        }
                        if (!found || certs.length != localCerts.length) {
                            return null;
                        }
                    }
                }
            }

            synchronized (mSync) {
                sReadBuffer = readBufferRef;
            }

            if (certs != null && certs.length > 0) {
                return certs;
            } else {
                if (TitanConfig.DEBUG) {
                    Log.e(TAG, "Package " + apkFile.getAbsolutePath() + " has no certificates; ignoring!");
                }
                return null;
            }
        } catch (Exception e) {
            if (TitanConfig.DEBUG) {
                Log.w(TAG, "Exception reading " + apkFile.getAbsolutePath(), e);
            }
            return null;
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    if (TitanConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * 读取jar包中某个entry的证书
     *
     * @param jarFile jar包对应的文件
     * @param je jar entry
     * @param readBuffer 读取数据时使用的byte buffer
     * @return 对应entry的证书
     */
    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
        InputStream is = null;
        try {
            // We must read the stream for the JarEntry to retrieve
            // its certificates.
            is = new BufferedInputStream(jarFile.getInputStream(je));
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                // not using
            }
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            if (TitanConfig.DEBUG) {
                Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
            }
        } catch (RuntimeException e) {
            if (TitanConfig.DEBUG) {
                Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    if (TitanConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

}
