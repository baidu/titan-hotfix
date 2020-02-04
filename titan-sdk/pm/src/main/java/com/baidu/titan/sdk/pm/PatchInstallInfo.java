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

package com.baidu.titan.sdk.pm;

import android.os.Build;

import com.baidu.titan.sdk.internal.util.Closes;
import com.baidu.titan.sdk.internal.util.EncodeUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Patch安装信息
 *
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2017/4/26
 */

public class PatchInstallInfo {
    private File mPatchDir;

    private FileLock mShareFileLock;

    private FileLock mWriteFileLock;

    public PatchInstallInfo(File patchDir) {
        this.mPatchDir = patchDir;
    }

    public String getId() {
        return mPatchDir.getName();
    }

    public boolean exist() {
        return mPatchDir.exists() && mPatchDir.isDirectory() && mPatchDir.list() != null;
    }

    public File getPatchFile() {
        return new File(this.mPatchDir, "patch.apk");
    }

    public File getStatusFile() {
        return new File(this.mPatchDir, "status");
    }

    public File getLockFile() {
        File lockFile =  new File(this.mPatchDir, ".lock");
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lockFile;
    }

    public FileLock getShareLock() {
        return mShareFileLock;
    }

    public boolean shareLock() {
        File lockFile = getLockFile();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(lockFile, "r");
            FileChannel channel = raf.getChannel();
            FileLock fileLock = channel.tryLock(0, 0, true);
            mShareFileLock = fileLock;
            return fileLock != null;
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    /**
     * 释放共享锁
     * @return
     */
    public boolean releaseShareLock() {
        FileLock fileLock = mShareFileLock;
        if (fileLock != null) {
            try {
                mShareFileLock.release();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 释放互斥锁
     * @return
     */
    public boolean releaseWriteLock() {
        FileLock fileLock = mWriteFileLock;
        if (fileLock != null) {
            try {
                fileLock.release();
                Closes.closeQuiet(fileLock.channel());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取独占锁
     * @return
     */
    public boolean writeLock() {
        File lockFile = getLockFile();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(lockFile, "rw");
            FileChannel channel = raf.getChannel();
            FileLock fileLock = channel.tryLock(0, 0, false);
            mWriteFileLock = fileLock;
            return fileLock != null;
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    /**
     * 获取当前独占锁
     * @return
     */
    public FileLock getWriteLock() {
        return mWriteFileLock;
    }

    /**
     * 获取dexPatch
     * @return
     */
    public String getDexPath() {
        // kitkat版本且dex数量大于一时，dex会被解压并将每个dex压缩到一个jar包中，需要将所有jar包路径添加到dexpath中
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (getDexCount() > 1) {
                List<File> dexs = getOrderedDexList();
                if (dexs == null || dexs.size() == 0) {
                    return "";
                }
                StringBuilder dexPathBuilder = new StringBuilder();
                Iterator<File> it = dexs.iterator();
                while (it.hasNext()) {
                    File dexFile = it.next();
                    dexPathBuilder.append(dexFile.getAbsoluteFile());
                    if (it.hasNext()) {
                        dexPathBuilder.append(File.pathSeparator);
                    }
                }
                return dexPathBuilder.toString();
            }
        }

        return getPatchFile().getAbsolutePath();

    }

    public List<File> getOrderedDexList() {
        List<File> dexs = new ArrayList<>();
        File mainDex = new File(this.mPatchDir, "classes.jar");
        if (mainDex.exists()) {
            dexs.add(mainDex);
        }
        int idx = 2;
        while (true) {
            File secDex = new File(this.mPatchDir, "classes" + idx + ".jar");
            if (!secDex.exists()) {
                break;
            }
            dexs.add(secDex);
            idx++;
        }
        return dexs;
    }

    public File getDexOptDir() {
        return new File(this.mPatchDir, "dexopt");
    }

    public boolean finished() {
        File statusFile = getStatusFile();
        if (statusFile.exists()) {
            return true;
        }
        return false;
    }

    public File getPatchDir() {
        return this.mPatchDir;
    }

    public void prepare() {
        this.mPatchDir.mkdirs();
    }


    public void cleanIfNeed() {
        deleteFile(this.mPatchDir);
    }

    private void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            File[] sub = file.listFiles();
            if (sub != null) {
                for (File f : sub) {
                    deleteFile(f);
                }
            }
            // delete dir
            file.delete();
        }
    }

    /**
     * 保存opt 文件摘要
     *
     * @param dexOptDir dex opt dir
     */
    public boolean saveOptFileDigests(File dexOptDir) {
        File optDigest = new File(getPatchDir(), ".opt_dig");
        FileWriter fw = null;
        try {
            fw = new FileWriter(optDigest);
            File[] optFiles = dexOptDir.listFiles();
            for (int i = 0; i < optFiles.length; i++) {
                File file = optFiles[i];
                if (file.isDirectory()) {
                    continue;
                }
                String sha256 = EncodeUtils.bytesToHex(EncodeUtils.sha256(file));
                fw.write(file.getName());
                fw.write(":");
                fw.write(sha256);
                fw.write("\n");
            }
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            Closes.closeQuiet(fw);
        }
        return true;
    }

    /**
     * 读取opt 文件摘要
     *
     * @return 保存的dex opt 文件摘要
     */
    public HashMap<String, String> readOptDigests() {
        File optDigest = new File(getPatchDir(), ".opt_dig");
        HashMap<String, String> digestMap = new HashMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(optDigest)));

            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] array = line.trim().split(":");
                if (array.length == 2) {
                    digestMap.put(array[0], array[1]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closes.closeQuiet(br);
        }
        return digestMap;
    }

    /**
     * 将dex count保存到文件中
     * @param dexCount dex count
     * @return 保存文件是否成功
     */
    public boolean saveDexCount(int dexCount) {
        File file = new File(mPatchDir, ".dexCount");
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file));
            dos.writeInt(dexCount);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closes.closeQuiet(dos);
        }
        return false;
    }

    /**
     * 读取dexCount
     *
     * @return dex count
     */
    public int getDexCount() {
        File file = new File(mPatchDir, ".dexCount");
        DataInputStream dis = null;
        int dexCount = -1;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            dexCount = dis.readInt();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Closes.closeQuiet(dis);
        }
        return dexCount;
    }
}
