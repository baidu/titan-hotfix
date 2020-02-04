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

package com.baidu.titan.core.patch;

import com.baidu.titan.core.util.TitanLogger;
import com.baidu.titan.dex.MultiDexFileBytes;
import com.baidu.titan.sdk.common.TitanConstant;
import com.baidu.titan.sdk.runtime.InteceptParameters;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangdi07@baidu.com
 * @since 2018/09/25
 */
public class PatchArgument {

    public static final String COMPONENT_APPLICATION = "application";

    public static final String COMPONENT_ACTIVITY = "activity";

    public static final String COMPONENT_SERVICE = "service";

    public static final String COMPONENT_RECEIVER = "receiver";

    public static final String COMPONENT_PROVIDER = "provider";

    /** 拦截器Specail方法的参数里列表，没有没有设定则使用默认值 */
    private Set<String> mSpecialInterceptParas;

    private File workDir;

    private File dexOutDir;
    /** smali 输出目录*/
    private File smaliOutDir;

    private PatchPolicy mPatchPolicy = PatchPolicy.PATCH_POLICY_LIGHT_THEN_FULL;

    public final OldProjectInfo oldProject = new OldProjectInfo();

    public final NewProjectInfo newProject = new NewProjectInfo();

    private List<File> bootClassPath;

    private boolean mSupportFinalFieldChange = true;
    /** 过滤不需要生成patch的类 */
    private ClassPatchFilter classPatchFilter;
    /** 外部设置的logger*/
    private TitanLogger extraLogger;
    /** patch 加载策略*/
    private int mLoadPolicy = TitanConstant.PATCH_LOAD_POLICY_BOOT;


    /**
     * 不需要生成patch类过滤器
     */
    public interface ClassPatchFilter {
        /**
         * 判断一个类是不是不需要patch
         *
         * @param typeDesc 类型描述
         * @return 类型是否不需要patch
         */
        boolean skipPatch(String typeDesc);
    }

    public PatchArgument() {
        HashSet<String> set = new HashSet<>();
        Collections.addAll(set, InteceptParameters.SPECIAL_PARAMETERS);
        mSpecialInterceptParas = set;
    }

    public Set<String> getSpecialInterceptParas() {
        return this.mSpecialInterceptParas;
    }

    public void setSepcialInterceptParas(Set<String> paras) {
        this.mSpecialInterceptParas = paras;
    }

    public PatchArgument setPatchPolicy(PatchPolicy policy) {
        this.mPatchPolicy = policy;
        return this;
    }

    public PatchPolicy getPatchPolicy() {
        return this.mPatchPolicy;
    }

    public PatchArgument setWorkDir(File workDir) {
        this.workDir = workDir;
        return this;
    }

    public File getWorkDir() {
        return this.workDir;
    }

    public PatchArgument setDexOutDir(File dexOutDir) {
        this.dexOutDir = dexOutDir;
        return this;
    }

    public File getDexOutDir() {
        return this.dexOutDir;
    }

    public List<File> getBootClassPath() {
        return this.bootClassPath;
    }

    public void setSupportFinalFieldChange(boolean supportFinalFieldChange) {
        mSupportFinalFieldChange = supportFinalFieldChange;
    }

    public boolean isSupportFinalFieldChange() {
        return mSupportFinalFieldChange;
    }

    public static class OldProjectInfo {

        private MultiDexFileBytes oldOrgDexs;

        private MultiDexFileBytes oldInstrumentedDexs;

        private boolean mManifestFromOldApk = false;

        private Map<String, List<String>> components = new HashMap<>();

        private File oldApkFile;

        private File mManifestFile;

        private Set<String> mInstrumentSpecialParas;

        public OldProjectInfo setInstrumentSpecialParas(Set<String> specialParas) {
            this.mInstrumentSpecialParas = specialParas;
            return this;
        }

        public Set<String> getInstrumentSpecialParas() {
            return this.mInstrumentSpecialParas;
        }

        public OldProjectInfo setManifestFile(File manifestFile) {
            this.mManifestFile = manifestFile;
            return this;
        }

        public File getManifestFile() {
            return mManifestFile;
        }

        public OldProjectInfo setManifestFromOldApk(boolean fromApk) {
            mManifestFromOldApk = fromApk;
            return this;
        }

        public OldProjectInfo setOldOrgDexs(MultiDexFileBytes oldOrgDexs) {
            this.oldOrgDexs = oldOrgDexs;
            return this;
        }

        public MultiDexFileBytes getOldOrgDexs() {
            return this.oldOrgDexs;
        }

        public OldProjectInfo setOldInstrumentedDexs(MultiDexFileBytes oldInstrumentedDexs) {
            this.oldInstrumentedDexs = oldInstrumentedDexs;
            return this;
        }

        public boolean isManifestFromOldApk() {
            return mManifestFromOldApk;
        }

        public MultiDexFileBytes getOldInstrumentedDexs() {
            return oldInstrumentedDexs;
        }

        public OldProjectInfo setComponent(String cn, List<String> components) {
            this.components.put(cn, components);
            return this;
        }

        public File getOldApkFile() {
            return this.oldApkFile;
        }

        public OldProjectInfo setOldApkFile(File oldApkFile) {
            this.oldApkFile = oldApkFile;
            return this;
        }

        public Map<String, List<String>> getComponents() {
            return this.components;
        }

    }


    public static class NewProjectInfo {

        private MultiDexFileBytes newOrgDexs;

        private Map<String, List<String>> components = new HashMap<>();

        private File newApkFile;

        private boolean mManifestFromNewApkFile = false;

        private File mManifestFile;

        public NewProjectInfo setManifestFromNewApkFile(boolean fromApk) {
            this.mManifestFromNewApkFile = fromApk;
            return this;
        }

        public boolean isManifestFromNewApkFile() {
            return this.mManifestFromNewApkFile;
        }

        public NewProjectInfo setManifestFile(File manifestFile) {
            this.mManifestFile = manifestFile;
            return this;
        }

        public File getManifestFile() {
            return this.mManifestFile;
        }

        public NewProjectInfo setNewOrgDexs(MultiDexFileBytes newOrgDexs) {
            this.newOrgDexs = newOrgDexs;
            return this;
        }

        public MultiDexFileBytes getNewOrgDexs() {
            return newOrgDexs;
        }

        public NewProjectInfo setComponent(String cn, List<String> components) {
            this.components.put(cn, components);
            return this;
        }

        public Map<String, List<String>> getComponents() {
            return this.components;
        }

        public NewProjectInfo setNewApkFile(File newApkFile) {
            this.newApkFile = newApkFile;
            return this;
        }

        public File getNewApkFile() {
            return this.newApkFile;
        }

    }

    public PatchArgument setBootClassPath(List<File> classPath) {
        this.bootClassPath = classPath;
        return this;
    }

    public PatchArgument setClassPatchFilter(ClassPatchFilter filter) {
        this.classPatchFilter = filter;
        return this;
    }

    public ClassPatchFilter getClassPatchFilter() {
        return this.classPatchFilter;
    }


    /**
     * extra logger setter
     *
     * @param extraLogger extra logger
     * @return this
     */
    public PatchArgument setExtraLogger(TitanLogger extraLogger) {
        this.extraLogger = extraLogger;
        return this;
    }

    /**
     * extra logger getter
     *
     * @return extra logger
     */
    public TitanLogger getExtraLogger() {
        return extraLogger;
    }

    /**
     * 设置patch加载策略
     *
     * @param loadPolicy patch加载策略
     * @return this
     */
    public PatchArgument setLoadPolicy(int loadPolicy) {
        this.mLoadPolicy = loadPolicy;
        return this;
    }

    /**
     * 获取patch加载策略
     *
     * @return patch加载策略
     */
    public int getLoadPolicy() {
        return mLoadPolicy;
    }

    /**
     * 设置smali输出目录
     *
     * @param smaliOutDir smali输出目录
     * @return this
     */
    public PatchArgument setSmaliOutDir(File smaliOutDir) {
        this.smaliOutDir = smaliOutDir;
        return this;
    }

    /**
     * 获取smali输出目录
     *
     * @return smali输出目录
     */
    public File getSmaliOutDir() {
        return this.smaliOutDir;
    }
}
