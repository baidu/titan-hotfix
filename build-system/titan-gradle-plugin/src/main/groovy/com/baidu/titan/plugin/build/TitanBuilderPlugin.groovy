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

package com.baidu.titan.plugin.build

import com.android.build.gradle.tasks.PackageApplication
import com.baidu.titan.core.instrument.InstrumentFilter
import com.baidu.titan.core.instrument.InstrumentMain
import com.baidu.titan.core.instrument.InstrumentType
import com.baidu.titan.dex.DexType
import com.baidu.titan.dex.MultiDexFileBytes
import com.baidu.titan.plugin.build.extensions.BuildExtension
import com.baidu.titan.plugin.build.extensions.PatchVerifyConfigExtension
import com.baidu.titan.plugin.build.extensions.SignaturePolicy
import com.google.common.hash.Hashing
import com.google.common.io.Files
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.ConventionTask
import org.json.JSONArray
import org.json.JSONObject

import java.nio.charset.Charset
import java.util.concurrent.Callable

/**
 * BuildPlugin,主要完成dex插桩和元信息生成等工作
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/6
 */

public class TitanBuilderPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.extensions.create("titanBuild", BuildExtension)

        project.titanBuild.extensions.create('verifyConfig', PatchVerifyConfigExtension, project)

        setup(project)
    }

//    /**
//     * Runtime Version
//     * TODO 根据脚本自动生成
//     */
//    private static final String TITAN_VERSION = "0.8.6";
//
//    protected static final String COM_BAIDU_TITAN_RUNTIME =
//            "com.baidu.titan:titan-runtime:" + TITAN_VERSION;

    private void setup(Project project) {

// Patch版本和Runtime版本不在一一对应，不再自动添加依赖，需要集成方主动添加依赖
//        project.getDependencies().add("compile", COM_BAIDU_TITAN_RUNTIME);

        project.afterEvaluate {
            def buildConfig = project.titanBuild;

            if (!buildConfig.enable) {
                project.logger.error("titan build is disabled")
                return
            }

            def android = project.extensions.android
            android.applicationVariants.all { variant ->
                prepareBuildForVariant(project, variant)
            }
        }
    }

    private void prepareBuildForVariant(Project project, def variant) {

        def buildConfig = project.titanBuild;

        Closure enableForVariant = buildConfig.enableForVariant
        if (enableForVariant == null) {
            throw new TitanBuildException("enableForVariant config unset")
        }

        if (!enableForVariant.call(variant)) {
            project.logger.error("build disable For " + variant.name)
            return
        }


        project.logger.error("prepare build for " + variant.name)

        File buildBaseDir = project.file("$project.buildDir/intermediates/titan")

        File buildInfoDir = new File(buildBaseDir, "buildinfo/${variant.dirName}");
        File orgDexDir = new File(buildBaseDir, "org-dex/${variant.dirName}")

        File orgDexOutputDir = new File("$project.buildDir/outputs/titan/${variant.dirName}/org-dex")

        PackageApplication packageTask = variant.packageApplication

        packageTask.doFirst {
            doDexInstrument(variant, buildConfig, project, packageTask, buildBaseDir, orgDexDir, orgDexOutputDir)
            generateAssert(variant, buildConfig, project, packageTask, buildInfoDir, orgDexDir)
        }
    }

    /**
     * 对 Gradle 编译生成的 Dex 文件进行插桩
     */
    private void doDexInstrument(def variant, def buildConfig,
                                 Project project, PackageApplication packageTask,
                                 File buildBaseDir, File orgDexDir, File orgDexOutputDir) {
        orgDexDir.deleteDir()
        orgDexDir.mkdirs()

        File dexOutDir = packageTask.getDexFolders().getSingleFile()

        project.logger.error("begin processing " + dexOutDir)

        project.copy {
            into(orgDexDir)
            from(dexOutDir)
        }

        project.copy {
            into(orgDexOutputDir)
            from(dexOutDir)
        }

        dexOutDir.deleteDir()
        dexOutDir.mkdirs()
        InstrumentMain.Argument instruArg = new InstrumentMain.Argument()
        instruArg.workDir = buildBaseDir

        MultiDexFileBytes orgDexBytes = MultiDexFileBytes
                .createFromDirectory(orgDexDir)


        if (buildConfig.manifestFile == null) {
            throw new IllegalArgumentException("dsl titanBuild extension " +
                    "-> manifestFile unset")
        }

        File manifestFile = buildConfig.manifestFile.call(variant)

        instruArg.setInstrumentMateriel(orgDexBytes, manifestFile)


        Closure classInstrumentFilter = buildConfig.classInstrumentFilter
        Closure methodInstrumentFilter = buildConfig.methodInstrumentFilter
        Closure maindexListSupplier = buildConfig.maindexList

        if (buildConfig.bootClassPath == null) {
            throw new IllegalArgumentException(
                    "dsl titanBuild extension -> bootClassPath unset")
        }

        instruArg.setBootClassPath(buildConfig.bootClassPath.call())

        instruArg.setFilter(new InstrumentFilter() {
            @Override
            boolean acceptClass(String typeDesc) {
                if (classInstrumentFilter != null) {
                    return classInstrumentFilter.call(typeDesc)
                }
                return true
            }

            @Override
            boolean acceptMethod(String typeDesc) {
                if (methodInstrumentFilter != null) {
                    return true
                }
                return true
            }
        })

        if (maindexListSupplier == null) {
            instruArg.setMainDexList(Collections.emptySet())
        } else {
            def mainDexList = maindexListSupplier.call(variant)
            def set = new HashSet<DexType>()
            mainDexList.each {
                set.add(new DexType(it))
            }
            instruArg.setMainDexList(set)
        }

        instruArg.setInstrumentType(InstrumentType.METHOD)

        instruArg.setFileterMeizuPush(buildConfig.filterMeizuPush)

        InstrumentMain instrument = new InstrumentMain(instruArg)
        MultiDexFileBytes outDexBytes = instrument.doInstrument()
        outDexBytes.writeToDir(dexOutDir)
    }

    /**
     * 动态生成一些文件到 assert 中
     */
    private void generateAssert(def variant, def buildConfig,
                                Project project, PackageApplication packageTask,
                                File buildInfoDir, File orgDexDir) {
        String apkId = null

        if (buildConfig.apkId != null) {
            apkId = buildConfig.apkId.call(variant)
        }
        if (apkId == null || apkId.length() == 0) {
            throw new TitanBuildException("apkid is empty")
        }

        buildInfoDir.deleteDir()
        buildInfoDir.mkdirs()
        File titanResourceDir = new File(buildInfoDir, "assets/titan")
        titanResourceDir.mkdirs()

        // build info
        File buildInfo = new File(titanResourceDir, "buildinfo");
        writeBuildInfo(project, buildConfig, buildInfo, orgDexDir, variant)

        // apk id
        File apkIdInfo = new File(titanResourceDir, "apkid")
        Files.write(apkId, apkIdInfo, Charset.forName("utf-8"))

        // verify config
        def verifyConfig = buildConfig.verifyConfig
        JSONObject verifyConfigJson = new JSONObject();
        verifyConfigJson.put("signaturePolicy", verifyConfig.signaturePolicy.name())
        if (verifyConfig.signaturePolicy != SignaturePolicy.NO_SIGNATURE) {
            if (verifyConfig.sigs == null || verifyConfig.sigs.size() == 0) {
                throw new TitanBuildException("")
            }
            JSONArray sigsJson = new JSONArray()

            for (String sig : verifyConfig.sigs) {
                sigsJson.put(sig)
            }

            verifyConfigJson.put("sigs", sigsJson);
        }

        File sigConfigFile = new File(titanResourceDir, "verify-config")
        Files.write(verifyConfigJson.toString(), sigConfigFile, Charset.forName("utf-8"))

        // append java resources
        FileCollection newJavaResourceFiles = project.files { buildInfoDir }
        if (packageTask.getJavaResourceFiles() != null) {
            newJavaResourceFiles = project.files(buildInfoDir, packageTask.getJavaResourceFiles())
        }

        conventionMappingMap(
                packageTask,
                "javaResourceFiles",
                new Callable<FileCollection>() {
                    @Override
                    FileCollection call() throws Exception {
                        return newJavaResourceFiles
                    }
                });
    }

    private void writeBuildInfo(Project project, def buildConfig, File buildInfoFile, File orgDexDir, def variant) {

        String commitId = null

        if (buildConfig.commitId != null) {
            commitId = buildConfig.commitId.call(variant)
        }
        if (commitId == null || commitId.length() == 0) {
            throw new TitanBuildException("commitId is empty")
        }
        BuildInfo buildInfo = new BuildInfo();

        buildInfo.commitId = commitId

        orgDexDir.eachFile {File f ->
            BuildInfo.DexInfo dexInfo = new BuildInfo.DexInfo();
            dexInfo.name = f.getName()
            dexInfo.sha256 = Hashing.sha256().hashBytes(Files.toByteArray(f)).toString();
            buildInfo.dexs.add(dexInfo)
        }

        boolean minifyEnable = variant.buildType.minifyEnabled
        buildInfo.proguardEnable = minifyEnable

        if (minifyEnable) {
            File mappingFile = new File(project.buildDir,
                    "outputs/mapping/${variant.dirName}/mapping.txt")
            if (!mappingFile.exists()) {
                throw new TitanBuildException("proguard mapping don't exist")
            }
            buildInfo.mappingSha256 = Hashing.sha256().hashBytes(Files.toByteArray(mappingFile)).toString()
        }


        String content = buildInfo.toJsonString()
        project.logger.error("titan buildinfo = " + content)

        Files.write(content, buildInfoFile, Charset.forName("utf-8"))
    }


    private void conventionMappingMap(Task task, String key, Callable<?> value) {
        if (task instanceof ConventionTask) {
            ((ConventionTask) task).getConventionMapping().map(key, value)
        } else if (task instanceof GroovyObject) {
            ConventionMapping conventionMapping = (ConventionMapping) ((GroovyObject) task).getProperty("conventionMapping")
            conventionMapping.map(key, value)
        } else {
            throw new IllegalArgumentException("Don't know how to apply convention mapping to task of type" + task.getClass().getName())
        }
    }


}
