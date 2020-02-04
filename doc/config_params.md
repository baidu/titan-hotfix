### build参数

| 参数 | 参数类型 | 说明 |
| ------ | ------ | ------ |
| enable | boolean | 是否启用热修复插桩 true为启用，false为禁用 |
| commitId | Closure | 获取当前代码的commit id |
| apkId | Closure | 传入apkId，作为发布apk的唯一id，用于匹配patch， 可以使用commit id，或者其它算法 |
| enableForVariant | Closure | 传入variant，根据需要，返回是否要对对应的variant apk插桩 |
| classInstrumentFilter | Closure | 传入class type description, 如Ljava/lang/Object;，返回是否对传入的类进行插桩|
| bootClassPath | Closure | 获取android framework class path |
| manifestFile | Closure | 获取AndroidManifest.xml文件 |
| maindexList | Closure | 获取maindex list |
| verifyConfig | dsl | 签名校验配置 |

#### verifyConfig参数

| 参数 | 参数类型 | 说明 |
| ------ | ------ | ------ |
| signaturePolicy | String | NO_SIGNATURE, 不校验签名<br>V1_ONLY, 只校验V1版本签名<br>V2_ONLY, 只校验V2版本签名<br>V2_FIRST, 优先校验V2版本签名，如果V2版本签名不存在，校验V1签名 |
| sigs | String list | patch包签名 |

### patch参数

| 参数 | 参数类型 | 说明 |
| ------ | ------ | ------ |
| enable | boolean | 是否是执行patch生成操作 |
| patchEnable | boolean | 是否是禁用包，该值为true时，生成的patch下发后，会将之前的patch包禁用 |
| prepareOldProjectPolicy | String | 生成patch包前，需要获取原apk相关的文件，包括mapping文件，org-dex目录，org-dex目录中保存着未插桩的dex。"just-build"是在patch生成前，先在原Apk对应提交上进行构建，生成相关文件并使用，"external"是从外部传入相关文件路径。 |
| oldApkFile | Closure | 返回原apk文件，用于获取commit id|
| justBuildInfo | dsl | 当"prepareOldProjectPolicy被设置为"just-build"时，进行相关配置 |
| externalBuildInfo | dsl | 当"prepareOldProjectPolicy"被设置为"external"时，进行相关配置 |
| buildVariant | String | patch生成支持的variant类型|
| checkMappingFile | boolean | 检查传入的mapping文件与apk中保存的mapping信息是否匹配 |
| loadPolicy | String | patch加载策略，"boot"为冷启生效，"just-in-time"为即时生效，每个app版本只有下发的第一个patch可设置为即时生效 |
| patchSignAction | Closure | patch签名操作|
| bootClassPath | Closure | 获取android framework class path |
| newApkManifestFile | Closure | 新apk中AndroidManifext.xml文件路径|
| workDir | Closure | 工作目录，用于保存一些中间文件 |
| versionInfo | dsl | patch版本信息|



#### versionInfo参数
| 参数 | 参数类型 | 说明 |
| ------ | ------ | ------ |
| patchVersionName | String | patch版本名 |
| patchVersionCode | int | patch版本号 |
| hostVersionName | String | app版本名 |
| hostVersionCode | int | app版本号 |
| disablePatchVersionCode | int | 禁用包版本号 |

#### justBuildInfo
| 参数 | 参数类型 | 说明 |
| ------ | ------ | ------ |
| checkout | Closure | 用于从cvs中拉取原apk对应代码打包 |
| buildTaskName | String[] | 打包任务名 |
| applicationModuleName | String | 主模块名 |


#### externalBuildInfo
| 参数 | 参数类型 | 说明 |
| ------ | ------ | ------ |
| mappingFile | File | 原apk对应的mapping文件 |
| orgDexDir | File | 原apk对应的org-dex目录，org-dex是在打包过程中保存的未插桩的dex, 在apk打包完成后，保存在app/build/outputs/titan/${variant}/org-dex|