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

package com.baidu.titan.plugin.patch

import com.baidu.titan.core.patch.PatchArgument
import com.baidu.titan.core.patch.PatchPolicy
import com.baidu.titan.core.util.TitanLogger
import com.baidu.titan.sdk.common.TitanConstant
import com.baidu.titan.core.patch.PatchMain
import com.baidu.titan.dex.MultiDexFileBytes
import com.baidu.titan.plugin.build.BuildInfo
import com.baidu.titan.plugin.patch.extensions.ExternalProjectInfo
import com.baidu.titan.plugin.patch.extensions.JustBuildProjectInfo
import com.baidu.titan.plugin.patch.extensions.PatchVersionInfo

import com.baidu.titan.plugin.patch.extensions.PatchExtension
import com.baidu.titan.plugin.patch.tasks.ProguardConfigTask
import com.baidu.titan.util.ZipUtil
import com.google.common.hash.Hashing
import com.google.common.io.Files
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild
import org.json.JSONObject

import java.util.function.Consumer
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Dex diff patch插件
 *
 * @author zhangdi07@baidu.com
 * @since 2017/5/15
 */
public class TitanPatchPlugin implements Plugin<Project> {

    /**
     * 用于保存基准包版本信息
     */
    private static class OldProjectInfo {
        File projectDir;
        File mappingFile;
        File orgDexDir;
    }

    /**
     * 准备基准包信息
     * @param project
     * @param titanIntermediates
     * @param patchConfig
     * @param buildInfo
     * @return
     */
    private OldProjectInfo prepareOldProjectArtifact(Project project, File titanIntermediates,
                                                     def patchConfig, BuildInfo buildInfo) {
        String policy = patchConfig.prepareOldProjectPolicy;
        if ("just-build".equals(policy)) {
            return prepareJustBuildOldProjectArtifact(project, titanIntermediates, patchConfig, buildInfo);
        } else if ("external".equals(policy)) {
            return prepareExternalOldProjectArtifact(project, titanIntermediates, patchConfig, buildInfo);
        } else {
            throw new PatchException("unkown preparePolicy = " + policy)
        }
    }

    /**
     * 使用上次编译产物
     * @param project
     * @param titanIntermediates
     * @param patchConfig
     * @param buildInfo
     * @return
     */
    private OldProjectInfo prepareExternalOldProjectArtifact(Project project, File titanIntermediates,
                                                              def patchConfig, BuildInfo buildInfo){
        ExternalProjectInfo epi = patchConfig.externalBuildInfo;
        if (epi == null) {
            throw new PatchException("externalBuildInfo not config")
        }
        if (epi.mappingFile == null || !epi.mappingFile.exists()) {
            throw new PatchException("mappingFile not config")
        }
        if (epi.orgDexDir == null || !epi.orgDexDir.exists()) {
            throw new PatchException("orgDexDir not config")
        }

        OldProjectInfo opi = new OldProjectInfo()
        opi.mappingFile = epi.mappingFile
        opi.orgDexDir = epi.orgDexDir
        return opi;
    }

    /**
     * 即时编译产物
     * @param project
     * @param titanIntermediates
     * @param patchConfig
     * @param buildInfo
     * @return
     */
    private OldProjectInfo prepareJustBuildOldProjectArtifact(Project project,
                                                              File titanIntermediates,
                                                              def patchConfig,
                                                              BuildInfo buildInfo) {

        JustBuildProjectInfo jbpi = patchConfig.justBuildInfo

        if (jbpi == null) {
            throw new PatchException("justBuildInfo not config");
        }

        File oldProjectIntermediates = new File(titanIntermediates, "oldProject")

        OldProjectInfo opi = new OldProjectInfo()

        def commitId = buildInfo.commitId

        opi.projectDir = new File(oldProjectIntermediates, "${patchConfig.buildVariant}/${commitId}/prj")

        File checkMarkFile = new File(opi.projectDir, ".checkout")

        if (!checkMarkFile.exists()) {
            opi.projectDir.deleteDir()
            opi.projectDir.mkdirs()
            // 检出
            Closure co = jbpi.checkout

            project.logger.error("checkout = " + co + " enable = " + patchConfig.enable)

            co.call(commitId, opi.projectDir)
            checkMarkFile.createNewFile()
        }

        def assembleOldProjectTask = project.tasks.create(name: 'assembleOldProject', type: GradleBuild) {
            dir = opi.projectDir
            tasks = jbpi.buildTaskName
            if (jbpi.projectProperties != null) {
                startParameter.projectProperties = jbpi.projectProperties
            }
            // startParameter.projectProperties = [deployDir: "/my_archive"]
        }
        project.tasks.getByName("preBuild").dependsOn assembleOldProjectTask;

        opi.mappingFile = new File(opi.projectDir,
                "${jbpi.applicationModuleName}/build/outputs/mapping/${patchConfig.buildVariant}/mapping.txt")

        String relativeBuildDir = project.rootProject.relativePath(project.buildDir.absolutePath)
        opi.orgDexDir = new File(opi.projectDir, "$relativeBuildDir/intermediates/titan/org-dex/${patchConfig.buildVariant}")

        return opi
    }


    private def findMatchedVariant(Project project, def patchConfig) {
        def android = project.extensions.android
        def matchedVariant = null;
        android.applicationVariants.all { variant ->
            project.logger.error("va name = " + variant.name + " buildV = " + patchConfig.buildVariant)
            if (variant.name.equals(patchConfig.buildVariant)) {
                project.logger.error("find matched v = " + patchConfig.buildVariant)
                matchedVariant = variant;
            }
        }
        if (matchedVariant == null) {
            throw new PatchException("cannot find buildVariant for patchConfig.buildVariant")
        }

        return matchedVariant
    }

    /**
     * 应用applymapping
     * @param project
     * @param titanIntermediates
     * @param applicationVariant
     * @param opi
     * @param patchConfig
     */
    private void tryApplyProguardMapping(Project project,
                                         File titanIntermediates,
                                         def applicationVariant,
                                         OldProjectInfo opi,
                                         def patchConfig) {
        boolean minifyEnable = applicationVariant.buildType.minifyEnabled

        if (minifyEnable) {
            ProguardConfigTask proguardConfigTask = project.tasks.create(
                    "titanConfig${applicationVariant.name}Proguard", ProguardConfigTask)

            File proguardConfigFile = new File(titanIntermediates,
                    "proguard/${patchConfig.buildVariant}/config.txt")

            proguardConfigTask.applicationVariant = applicationVariant
            proguardConfigTask.applyMappingFile = opi.mappingFile
            proguardConfigTask.proguardConfigFile = proguardConfigFile
            // proguardConfigTask.mustRunAfter assembleOldProjectTask


            def proguardTask = getProguardTask(project, applicationVariant.name)
            project.logger.error("proguardTask = " + proguardTask)

            if (proguardTask != null) {
                proguardTask.dependsOn proguardConfigTask
            }
        }
    }

    @Override
    public void apply(Project project) {
        project.extensions.create("titanPatch", PatchExtension)
        project.titanPatch.extensions.create('versionInfo', PatchVersionInfo)
        project.titanPatch.extensions.create('justBuildInfo', JustBuildProjectInfo)
        project.titanPatch.extensions.create('externalBuildInfo', ExternalProjectInfo)

        project.afterEvaluate {
            def patchConfig = project.titanPatch;
            boolean patchEnable = patchConfig.patchEnable;

            if (!patchConfig.enable) {
                project.logger.error("titan patch is disabled")
                return
            }

            def applicationVariant = findMatchedVariant(project, patchConfig)

            // output Base dir
            File outputBaseDir = new File("$project.buildDir/outputs/titan")

            if (patchConfig.oldApkFile == null) {
                throw new GradleException("you should config oldApkFile in titanPatch")
            }
            File oldApkFile = patchConfig.oldApkFile.call(applicationVariant);

            if(!oldApkFile.exists()) {
                throw new GradleException("old apk not exist")
            }

            BuildInfo buildInfo = BuildInfo.createFromJson(new String(
                    ZipUtil.getZipEntryContent(TitanConstant.BUILDINFO_PATH, oldApkFile), "utf-8"));

            String apkId = new String(ZipUtil.getZipEntryContent(TitanConstant.APKID_PATH, oldApkFile), "utf-8")

            project.logger.error("buildinfo = " + buildInfo + "apkid = " + apkId)

            File titanIntermediates = project.file("$project.buildDir/intermediates/titan")

            OldProjectInfo opi = null
            if (patchEnable) {
                opi = prepareOldProjectArtifact(project, titanIntermediates, patchConfig, buildInfo)
            }



            if (applicationVariant == null) {
                throw new PatchException("variant not matched: require " + patchConfig.buildVariant)
            }

            File titanOutDir = new File(outputBaseDir, patchConfig.buildVariant)

            if (patchEnable) {
                tryApplyProguardMapping(project, titanIntermediates, applicationVariant, opi, patchConfig)
            }

            //variantOutput.processManifest

            def packageTask = applicationVariant.outputs[0].packageApplication

            File patchDir = new File(titanOutDir, "patch")

            packageTask.doLast {
                if (patchEnable) {
                    if (patchConfig.checkMappingFile && buildInfo.proguardEnable) {
                        if (!buildInfo.mappingSha256
                                .equals(Hashing.sha256().hashBytes(
                                Files.toByteArray(opi.mappingFile))).toString()) {
                            throw new PatchException("proguard mapping not match")
                        }
                    }
                }

                patchDir.deleteDir()
                patchDir.mkdirs()

                File unSignedPatchFile = new File(patchDir, "titan-patch-unsigned.apk")

                ZipOutputStream patchZos = new ZipOutputStream(new FileOutputStream(unSignedPatchFile))

                if (patchEnable) {
                    writePatchDexs(project, patchZos, oldApkFile, titanIntermediates, opi,
                            applicationVariant, patchConfig, titanOutDir)
                }

                writePatchInfo(patchZos, patchEnable, patchConfig, apkId)

                patchZos.close()

                if (patchConfig.patchSignAction != null) {
                    File signedPatch = new File(patchDir, "titan-patch-signed.apk")
                    patchConfig.patchSignAction.call(unSignedPatchFile, signedPatch)
                }

                if (patchEnable) {
                    File unSignedDisablePatchFile = new File(patchDir, "titan-patch-disable-unsigned.apk")
                    ZipOutputStream disablePatchZos = new ZipOutputStream(
                            new FileOutputStream(unSignedDisablePatchFile))
                    writePatchInfo(disablePatchZos, false, patchConfig, apkId)
                    disablePatchZos.close()

                    if (patchConfig.patchSignAction != null) {
                        File signedDisablePatch = new File(patchDir, "titan-patch-disable-signed.apk")
                        patchConfig.patchSignAction.call(unSignedDisablePatchFile, signedDisablePatch)
                    }
                }

            }
        }
    }


    private void writePatchDexs(Project project, ZipOutputStream patchZos, File oldApkFile, File titanIntermediates,
                                OldProjectInfo opi, def applicationVariant, def patchConfig, File titanOutDir) {
        File oldOrgDexDir = opi.orgDexDir;

        File newDexDir = new File(project.buildDir, "intermediates/titan/org-dex/${applicationVariant.dirName}")

        File patchDexOutDir = new File(titanIntermediates, "patch-dexs/${patchConfig.buildVariant}")

        patchDexOutDir.deleteDir()

        patchDexOutDir.mkdirs()

        File smaliOutDir = new File(titanOutDir, "smali")

        smaliOutDir.mkdirs();

        PatchArgument patchArg = new PatchArgument()

        patchArg.setWorkDir(patchConfig.workDir.call(applicationVariant))
                .setPatchPolicy(PatchPolicy.PATCH_POLICY_LIGHT_ONLY)
                .setBootClassPath(patchConfig.bootClassPath.call())
                .setDexOutDir(patchDexOutDir)
                .setSmaliOutDir(smaliOutDir)

        patchArg.oldProject.setOldOrgDexs(MultiDexFileBytes.createFromDirectory(oldOrgDexDir))

        patchArg.oldProject.setOldApkFile(patchConfig.oldApkFile.call(applicationVariant))

        patchArg.oldProject.setManifestFromOldApk(true)

        patchArg.oldProject.setOldInstrumentedDexs(MultiDexFileBytes.createFromZipFile(oldApkFile))

        // new project

        patchArg.newProject.setNewOrgDexs(MultiDexFileBytes.createFromDirectory(newDexDir))
        if (patchConfig.newApkManifestFile != null) {
            patchArg.newProject.setManifestFile(patchConfig.newApkManifestFile.call(applicationVariant))
        }

        def proguardMap = getClassMapping(opi.mappingFile)

        patchArg.setClassPatchFilter(new PatchArgument.ClassPatchFilter() {

            @Override
            boolean skipPatch(String typeDesc) {
                if (patchConfig.classPatchFilter != null) {
                    if (patchConfig.classPatchFilter.call(typeDesc)) {
                        return true
                    }

                    String proguardedClassName = typeDesc.replace("/", ".")
                            .replace(";", "")
                            .substring(1)

                    String originClassName = proguardMap.get(proguardedClassName)
                    if (originClassName != null) {
                        if (patchConfig.classPatchFilter.call(originClassName)) {
                            return true
                        }
                    }

                }
                return false
            }
        })

        File patchReportDir = new File(titanOutDir, "report")
        patchReportDir.deleteDir();
        patchReportDir.mkdirs();

        File reportFile = new File(patchReportDir, "report.txt");
        def writer = reportFile.newWriter()
        patchArg.setExtraLogger(new TitanLogger(writer, false))

//        File patchDexFile = new File(patchDexOutDir, "classes.dex")

//        FileOutputStream fos = new FileOutputStream(patchDexFile);
        // arg.setOutput(fos)

        int loadPolicy = "boot".equals(patchConfig.loadPolicy) ? TitanConstant.PATCH_LOAD_POLICY_BOOT
                : TitanConstant.PATCH_LOAD_POLICY_JUST_IN_TIME

        patchArg.setLoadPolicy(loadPolicy)

        PatchMain.doPatch(patchArg)

//        fos.close()

        writer.close()
        MultiDexFileBytes dexsOutput = MultiDexFileBytes.createFromDirectory(patchDexOutDir)



//        if (!patchSucess) {
//            throw new PatchException("patch gen failed")
//        }

        dexsOutput.forEach(new Consumer<MultiDexFileBytes.Entry>() {
            @Override
            void accept(MultiDexFileBytes.Entry entry) {
                patchZos.putNextEntry(new ZipEntry(entry.getDexFileName()))
                patchZos.write(entry.dexFileBytes.getDexFileBytes())
                patchZos.closeEntry()
            }
        })

        File classInfoFile = new File(patchArg.getWorkDir(), "classInfo.json")
        if (classInfoFile.exists()) {
            def patchClassInfoEntry = new ZipEntry(TitanConstant.PATCH_CLASS_INFO_PATH)
            def data = classInfoFile.bytes
            patchClassInfoEntry.setMethod(ZipEntry.STORED)
            patchClassInfoEntry.setSize(data.length)
            patchClassInfoEntry.setCompressedSize(data.length)
            def crc32 = new CRC32()
            crc32.update(data)
            patchClassInfoEntry.setCrc(crc32.getValue())
            patchZos.putNextEntry(patchClassInfoEntry)
            patchZos.write(data)
            patchZos.closeEntry()
        }
    }

    /**
     * 读取mapping文件，将mapping关系保存在map中
     *
     * @param mappingFile mapping 文件
     * @return 保存mapping关系的map
     */
    private Map<String, String> getClassMapping(File mappingFile) {
        if (mappingFile != null && mappingFile.exists()) {
            Map<String, String> map = new HashMap<>()
            mappingFile.eachLine { String line ->
                if (line.length() > 0) {
                    char firstChar = line.charAt(0)
                    if (!firstChar.isWhitespace() && line.contains("->")) {
                        String[] lineArray = line.split("->")
                        String originClassName = lineArray[0].trim()
                        String proguardedClassName = lineArray[1].trim()
                        proguardedClassName = proguardedClassName.replace(":", "")
                        map.put(proguardedClassName, originClassName)
                    }
                }
            }
            return map
        }
        return null
    }


    private void writePatchInfo(ZipOutputStream patchZos, boolean patchEnable, def patchConfig, String apkId) {
        JSONObject patchInfoJson = new JSONObject();
        // status == 1 : avaiable
        // status == 0 : unavaiable
        patchInfoJson.put(TitanConstant.PatchInfoConstant.KEY_PATCH_STATUS, patchEnable
                ? TitanConstant.PATCH_STATUS_ENABLE : TitanConstant.PATCH_STATUS_DISABLE)

        int loadPolicy = "boot".equals(patchConfig.loadPolicy) ? TitanConstant.PATCH_LOAD_POLICY_BOOT
            : TitanConstant.PATCH_LOAD_POLICY_JUST_IN_TIME

        println("bootLoadSyncPolicy = ${patchConfig.bootLoadSyncPolicy}")

        int boolLoadSyncPolicy = "async".equals(patchConfig.bootLoadSyncPolicy)
                ? TitanConstant.PATCH_BOOT_LOAD_SYNC_POLICY_ASYNC : TitanConstant.PATCH_BOOT_LOAD_SYNC_POLICY_SYNC

        patchInfoJson.put(TitanConstant.PatchInfoConstant.KEY_LOAD_POLICY, loadPolicy)

        patchInfoJson.put(TitanConstant.PatchInfoConstant.KEY_BOOT_LOAD_SYNC_POLICY, boolLoadSyncPolicy)

        patchInfoJson.put(TitanConstant.PatchInfoConstant.KEY_TARGET_ID, apkId)


        PatchVersionInfo patchVersionInfo = patchConfig.versionInfo;
        if (patchVersionInfo == null) {
            throw new PatchException("versionInfo not configuration")
        }

        JSONObject versionInfoJson = new JSONObject();
        versionInfoJson.put(TitanConstant.PatchInfoConstant.KEY_PATCH_VERSIONNAME, patchVersionInfo.patchVersionName)

        if (patchEnable) {
            if (patchVersionInfo.patchVersionCode <= 0) {
                throw new PatchException("patchVersionCode illegal");
            }
            versionInfoJson.put(TitanConstant.PatchInfoConstant.KEY_PATCH_VERSIONCODE, patchVersionInfo.patchVersionCode)
        } else {
            if (patchVersionInfo.disablePatchVersionCode <= 0) {
                throw new PatchException("disablePatchVersionCode illegal")
            }
            versionInfoJson.put(TitanConstant.PatchInfoConstant.KEY_PATCH_VERSIONCODE,
                    patchVersionInfo.disablePatchVersionCode)
        }

        versionInfoJson.put(TitanConstant.PatchInfoConstant.KEY_HOST_VERSIONNAME, patchVersionInfo.hostVersionName)

        if (patchVersionInfo.hostVersionCode <= 0) {
            throw new PatchException("hostVersionCode illegal");
        }
        versionInfoJson.put(TitanConstant.PatchInfoConstant.KEY_HOST_VERSIONCODE, patchVersionInfo.hostVersionCode)

        patchInfoJson.put(TitanConstant.PatchInfoConstant.KEY_VERSION_INFO, versionInfoJson)

        if (patchConfig.patchPackageName != null) {
            patchInfoJson.put("patchPackage", patchConfig.patchPackageName)
        }

        def infoEntry = new ZipEntry(TitanConstant.PATCH_INFO_PATH)
        def data = patchInfoJson.toString().getBytes("utf-8")
        infoEntry.setMethod(ZipEntry.STORED)
        infoEntry.setSize(data.length)
        infoEntry.setCompressedSize(data.length)
        def crc32 = new CRC32()
        crc32.update(data)
        infoEntry.setCrc(crc32.getValue())

        patchZos.putNextEntry(infoEntry)
        patchZos.write(data)
        patchZos.closeEntry()
    }

    Task getProguardTask(Project project, String variantName) {
        String proguardTaskName = "transformClassesAndResourcesWithProguardFor${variantName.capitalize()}"
        project.logger.error("progarud name = " + proguardTaskName)
        def proguardTask =  project.tasks.findByName(proguardTaskName)
        if (proguardTask == null) {
            // 解决gradle 插件升级到4.x查找proguard task的问题
            proguardTaskName = "minify${variantName.capitalize()}WithProguard"
            proguardTask =  project.tasks.findByName(proguardTaskName)
        }
        if (proguardTask == null) {
            // 解决开启R8时查找proguard task的问题
            proguardTaskName = "minify${variantName.capitalize()}WithR8"
            proguardTask =  project.tasks.findByName(proguardTaskName)
        }
        return proguardTask
    }

}
