## Titan已知问题

1. 在某些9.0 rom机型，修复代码中使用HttpClient发起网络请求引发崩溃

    原因分析：命中了Android 9.0 rom中HttpClient库相关的bug 77342775.
    参考google对此问题的修复：https://android.googlesource.com/platform/external/apache-http/+/47b7e876148e345142200af4ba8b99862ddcbdaf

    解决方案：在修复代码中使用HttpUrlConnection或者okhttp等网络库发起网络请求。

2. 不支持在父类、接口中新增方法并在子类中override。

    原因分析：父类中新增的方法会被放到patch代码中作为一个静态方法，调用该方法的指令也随之改为inovke-static，子类override的方法无法被调用到。暂不支持抽象方法和接口中方法的新增。

    解决方案：可通过增加新类方式修复问题

3. 通过新增类方式修复问题时，patch不生效。

    原因分析：通过新增类方式修复问题，原有的类被替换，不再被引用。在混淆时，原有类被移除，新增的类混淆后与原有的类混淆后同名，导致work around方案失效。

    解决方案：避免原有类在混淆时被移除。

    后期会对此问题进行修复。