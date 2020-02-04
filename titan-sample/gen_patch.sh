#!/bin/bash
# -*- coding: utf-8 -*-

newest_apk()
{
    ls -t app/build/outputs/apk/release/*.apk | head -1
}

gen() {
    ./gradlew --stop
    ./gradlew clean
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false
    if [ $? != 0 ]
    then
        echo "build base apk fail"
        exit 2
    fi
    # copy old.apk, mapping.txt , org-dex to titan-product
    mkdir titan-product
    rm -rf titan-product/*
    mkdir titan-product/org-dex
    cp $(newest_apk) titan-product/old.apk
    if [ $? != 0 ]
    then
        echo "copy base apk fail"
        exit 2
    fi

    mkdir -p output/ ;
    cp -r app/build/outputs output/;
    mv output/outputs/apk/release/* output/outputs/apk/;
    rm -rf output/outputs/apk/release;
    rm -f output/outputs/apk/app-release-unaligned.apk

    cp app/build/outputs/mapping/release/mapping.txt titan-product/
    cp app/build/outputs/titan/release/org-dex/* titan-product/org-dex/
    cp app/build/intermediates/res/release/resources-release.ap_ titan-product/resources.ap_
    if [ $? != 0 ]
    then
        echo "copy resources.ap_ fail"
        exit 2
    fi

    patchDir=output/outputs/patches
    mkdir -p $patchDir
    rm $patchDir/*



    
    # launch crash patch
    ./gradlew clean
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=1 --stacktrace
    if [ $? != 0 ]
        then
            echo "build patch 1 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-lanuch-crash.apk

    
    # dex only patch
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=2 --stacktrace
    if [ $? != 0 ]
        then
            echo "build patch 2 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-dex-only.apk

    # patch disable
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=6
    if [ $? != 0 ]
        then
            echo "build patch 6 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-disable.apk

    
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=7
    if [ $? != 0 ]
        then
            echo "build patch 7 fail"
            exit 2
    fi

    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-fieldchange.apk

    
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=8
    if [ $? != 0 ]
        then
            echo "build patch 8 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-fieldadd.apk

    
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=9
    if [ $? != 0 ]
        then
            echo "build patch 9 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-serializable.apk


    
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=11
    if [ $? != 0 ]
        then
            echo "build patch 11 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-anonymous.apk

    
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=12
    if [ $? != 0 ]
        then
            echo "build patch 12 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-override-method.apk

    ./gradlew clean
    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=13
    if [ $? != 0 ]
        then
            echo "build patch 13 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-kotlin-lambda.apk


    ./gradlew app:assembleRelease -Pandroid.enableBuildCache=false -PuseTitanPatch -PpatchTest=10
    if [ $? != 0 ]
        then
            echo "build patch 10 fail"
            exit 2
    fi
    cp app/build/outputs/titan/release/patch/titan-patch-signed.apk $patchDir/titan-patch-invoke-special-method.apk
}

main() {
    time gen
    echo "----    -----------"    
    echo "^ total time cost ^"
    echo "==================="    
}

main