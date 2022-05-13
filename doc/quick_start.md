### gradle.properties定义常量
```
TITAN_PLUGIN_VERSION=1.0.13
TITAN_RUNTIME_VERSION=1.0.13
```

### 添加maven repositories
在rootproject/build.grdle中添加maven repositories url

1. 在buildscript中添加
    ```
        repositories {
            repositories {
                jcenter()
            }
        }
    ```
2. 在allprojects中添加
    ```
        repositories {
            jcenter()
        }
    ```
3. 在rootproject/build.gradle的dependencies中添加
    ```
    classpath "com.baidu.titan.build:titan-plugin:${TITAN_PLUGIN_VERSION}"
    ```


### 应用titan插件及添加runtime依赖
1. 在app/build.gradle中头部添加
    ```
    apply plugin: 'com.baidu.titan.builder'
    apply plugin: 'com.baidu.titan.patch'
    apply from: 'titan-build/titan-config.gradle'
    ```
2. 在app/build.gradle中的dependencies中添加
    ```
    implementation "com.baidu.titan.sdk:all:${TITAN_RUNTIME_VERSION}"
    ```
3. 在app/build.gradle底部添加
    ```
    configTitan(project)
    ```
### 对titan进行配置
在titan-build/titan-config.gradle中对titan进行配置。

配置参数说明：[Titan配置参数](config_params.md)

具体配置信息见titan-build/titan-config.gradle文件示例如下：
```
import java.security.Key
import java.security.KeyStore

String EXT_TITAN_BUILD = "ext_titan_build"

def configTitan(Project project) {

    project.titanPatch {
        enable = project.hasProperty("useTitanPatch")
        patchEnable = true

        versionInfo {
            hostVersionName = "10.0.0.1"
            hostVersionCode = 38010880
            patchVersionName = "1.0.0"
            patchVersionCode = 1000
            disablePatchVersionCode = 1001
        }

        buildVariant = "release"

        /** 需要从old apk中获取commit id，此设置为获取old apk的策略，值为external或just-build"*/
        prepareOldProjectPolicy = "external"

        /** prepareOldProjectPolicy为just-build时需要使用，需要提供切换到指定提交的命令*/
        justBuildInfo {
            checkout = { String commit , File dir ->
                File archive = File.createTempFile("arch", ".zip", dir)
                println "checkout commit = " + commit + " dir = " + dir
                exec {
                    workingDir project.rootProject.rootDir
                    commandLine "git"
                    args  "archive", "-o", archive.getAbsolutePath(), commit
                }

                copy {
                    from zipTree(archive)
                    into dir
                }
                archive.delete()
            }

            buildTaskName = ["assembleRelease"]
        }

        oldApkFile {
            return rootProject.file("titan-product/old.apk")
        }

        /** prepareOldProjectPolicy为just 为external时需要，指定mapping文件和org-dex文件夹的位置*/
        externalBuildInfo {
            mappingFile = rootProject.file("titan-product/mapping.txt")
            orgDexDir = rootProject.file("titan-product/org-dex")
        }

        patchPackageName = "com.baidu.titan.patch"

        checkMappingFile = false

        /** 对patch apk进行签名*/
        patchSignAction = { def unsignedPatch, def signedPatch ->
            def input =  rootProject.file(unsignedPatch)
            def output = rootProject.file(signedPatch)
            try {
                signApk(input, output, project)
            } catch (IOException e) {
                e.printStackTrace()
            }

        }

        bootClassPath {
            return project.android.bootClasspath
        }

        workDir {
            return rootProject.file("titan-product/workDir")
        }

        newApkManifestFile {
            return rootProject.file("app/build/intermediates/manifests/full/release/AndroidManifest.xml")
        }
    }

    project.titanBuild {
        enable true

        enableForVariant { variant ->
            return true
        }

        // 通过git命令或其它方式获得当前提交的commitId
        commitId { "1234567" }

        // 当前patch唯一id，可自行实现
        apkId { "1234567" }

        verifyConfig {
            signaturePolicy 'V2_ONLY'
            sigs = ["be2bd7d41106307ae1449ae0846ca4a26405623a"]
        }

        manifestFile { variant ->
            def variantBuildType = variant.buildType.name.toLowerCase()
            return project.file("build/intermediates/manifests/full/" + variantBuildType + "/AndroidManifest.xml")
        }

        bootClassPath {
            return project.android.bootClasspath
        }
    }


}

def signApk(def input, def output, Project project) {
    //sign apk
    def keystorePropertiesFile = project.rootProject.file("keystore.properties")
    def keystoreProperties = new Properties()
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
    String storeFilePath = keystoreProperties['storeFile']
    def storepass = keystoreProperties['storePassword']
    def keypass = keystoreProperties['keyPassword']
    def keyAlias = keystoreProperties['keyAlias']
    println(String.format("Signing apk: %s", output.getName()));
    def storeFile = project.rootProject.file(storeFilePath)
    String signatureAlgorithm = getSignatureAlgorithm(keystoreProperties);
    println(String.format("Signing key algorithm is %s", signatureAlgorithm));

    if (output.exists()) {
        output.delete();
    }
    ArrayList<String> command = new ArrayList<>();
    command.add("jarsigner");

    command.add("-sigalg");
    command.add(signatureAlgorithm);
    command.add("-digestalg");
    command.add("SHA1");
    command.add("-keystore");
    command.add(storeFile.absolutePath);
    command.add("-storepass");
    command.add(storepass);
    command.add("-keypass");
    command.add(keypass);
    command.add("-signedjar");
    command.add(output.getAbsolutePath());
    command.add(input.getAbsolutePath());
    command.add(keyAlias);

    Process process = new ProcessBuilder(command).start();
    process.waitFor();
    process.destroy();
    if (!output.exists()) {
        throw new IOException("Can't Generate signed APK. Please check if your sign info is correct.");
    }
}

def getSignatureAlgorithm(Properties keystoreProperties) {
    InputStream is = null
    try {
        String storeFilePath = keystoreProperties['storeFile']
        def storepass = keystoreProperties['storePassword']
        def keypass = keystoreProperties['keyPassword']
        def keyAlias = keystoreProperties['keyAlias']
        is = new BufferedInputStream(new FileInputStream(rootProject.file(storeFilePath)))
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, storepass.toCharArray());
        Key key = keyStore.getKey(keyAlias, keypass.toCharArray());
        String keyAlgorithm = key.getAlgorithm();
        String signatureAlgorithm;
        if (keyAlgorithm.equalsIgnoreCase("DSA")) {
            signatureAlgorithm = "SHA1withDSA";
        } else if (keyAlgorithm.equalsIgnoreCase("RSA")) {
            signatureAlgorithm = "SHA1withRSA";
        } else if (keyAlgorithm.equalsIgnoreCase("EC")) {
            signatureAlgorithm = "SHA1withECDSA";
        } else {
            throw new RuntimeException("private key is not a DSA or "
                    + "RSA key");
        }
        return signatureAlgorithm;
    } catch (IOException e) {
        e.printStackTrace()
    } finally {
        if (is != null) {
            is.close()
        }
    }
}

def getVariantExtConfig(def variant, String key) {
    def buildType = variant.buildType
    boolean fromBuildType = buildType.getExtConfigs().containsKey(key)

    def productFlavor = variant.getProductFlavors().size() > 0 ?
            variant.getProductFlavors()[0] : variant.getMergedFlavor()

    boolean fromProduct = productFlavor.getExtConfigs().containsKey(key)

    if (fromBuildType && fromProduct) {
        throw new IllegalStateException("extconfig both exist in buildtype and product for variant " + variant)
    } else if (fromBuildType) {
        return buildType.getExtConfigs()[key]
    } else if (fromBuildType) {
        return productFlavor.getExtConfigs()[key]
    } else {
        return null
    }
}


ext {
    configTitan = this.&configTitan;
}
```

### 混淆处理
在proguard-rules.pro里面加上-keep class com.baidu.titan.**{*;}

## patch打包流程
生成patch包需要准备三类文件：
1. base apk包，即未做修改的apk包 
2. base apk包对应的mapping.txt文件
3. base apk包对应的org-dex文件

所以需要在生成base包，保存对应包的mapping.txt文件与org-dex文件<br>
mapping文件路经为app/build/outputs/mapping/release/mapping.txt<br>
org-dex路径为app/build/outputs/titan/release/org-dex/*.dex<br>

patch打包流程为：
1. 在工程目录下创建titan-product目录
2. 将以上提到的三类文件保存到titan-product目录
3. 修改代码
4. 执行./gradlew assembleRelease -PuseTitanPatch生成patch
5. patch生成后保存在app/build/outputs/titan/release/patch/titan-signed-patch.apk

## patch校验
patch生成后，会在app/build/outputs/titan/release/report/report.txt文件中，记录发生变更的类信息，可用于验证patch是否正确<br>
同时，在app/build/outputs/titan/release/smali目录下，会输出发生变更的类的smali文件，可用于对比发生的变更是否符合预期

## patch下载、安装与加载
[patch best practice](patch_best_practice.md)
