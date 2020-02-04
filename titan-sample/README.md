# Titan接入说明文档

## gradle.properties定义常量
```
TITAN_PLUGIN_VERSION=1.0.2
TITAN_RUNTIME_VERSION=1.0.2
```

## 添加maven repositories
在rootproject/build.grdle中添加maven repositories url

1. 在buildscript中添加
    ```
        repositories {
            jcenter()
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


## 应用titan插件及添加runtime依赖
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
## 对titan进行配置
在titan-build/titan-config.gradle中对titan进行配置。具体配置信息见titan-build/titan-config.gradle文件

## 混淆处理
在proguard-rules.pro里面加上-keep class com.baidu.titan.**{*;}

## 如何生成patch
1. 首先生成base包
./gradlew app:assembleRelease
mkdir titan-product
cp app/build/outputs/apk/release/app-release.apk titan-product/old.apk
cp app/build/outputs/mapping/release/mapping.txt titan-product/mapping.txt
mkdir titan-product/org-dex
rm titan-product/org-dex/*
cp app/build/outputs/titan/release/org-dex/* titan-product/org-dex/

2. 然后修改代码
3. 执行./gradlew app:assembleRelease -PuseTitanPatch，生成patch包，patch包生成路径为app/build/outputs/titan/release/patches/titan-patch-signed.apk