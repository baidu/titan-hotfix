.class public Lcom/baidu/titan/sample/TryCatchSmali;
.super Ljava/lang/Object;
.source "TryCatchSmali.java"

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 9
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method tryCatch()V
    .catch Ljava/lang/Exception; { :L0 .. :L1 } :L2
    .registers 3
    :L0
    .prologue
    .line 12
    new-instance v0, Ljava/io/File;
    const-string v1, "tryCatch"
    invoke-direct { v0, v1 }, Ljava/io/File;-><init>(Ljava/lang/String;)V
    :L1
    .line 16
    return-void
    :L2
    .line 13
    move-exception v0
    .line 14
    new-instance v1, Ljava/lang/RuntimeException;
    invoke-direct { v1, v0 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/Throwable;)V
    throw v1
.end method

.method tryCatchFinally()V
    .catch Ljava/io/FileNotFoundException; { :L0 .. :L1 } :L5
    .catchall { :L0 .. :L1 } :L11
    .catch Ljava/io/IOException; { :L2 .. :L3 } :L4
    .catchall { :L6 .. :L7 } :L11
    .catch Ljava/io/IOException; { :L8 .. :L9 } :L10
    .catch Ljava/io/IOException; { :L12 .. :L13 } :L14
    .registers 4
    .prologue
    .line 19
    const/4 v1, 0
    :L0
    .line 21
    new-instance v0, Ljava/io/FileInputStream;
    const-string v2, "tryCatchFinally"
    invoke-direct { v0, v2 }, Ljava/io/FileInputStream;-><init>(Ljava/lang/String;)V
    :L1
    .line 25
    if-eqz v0, :L3
    :L2
    .line 27
    invoke-virtual { v0 }, Ljava/io/InputStream;->close()V
    :L3
    .line 33
    return-void
    :L4
    .line 28
    move-exception v0
    .line 29
    invoke-virtual { v0 }, Ljava/io/IOException;->printStackTrace()V
    goto :L3
    :L5
    .line 22
    move-exception v0
    :L6
    .line 23
    invoke-virtual { v0 }, Ljava/io/FileNotFoundException;->printStackTrace()V
    :L7
    .line 25
    if-eqz v1, :L3
    :L8
    .line 27
    invoke-virtual { v1 }, Ljava/io/InputStream;->close()V
    :L9
    goto :L3
    :L10
    .line 28
    move-exception v0
    .line 29
    invoke-virtual { v0 }, Ljava/io/IOException;->printStackTrace()V
    goto :L3
    :L11
    .line 25
    move-exception v0
    if-eqz v1, :L13
    :L12
    .line 27
    invoke-virtual { v1 }, Ljava/io/InputStream;->close()V
    :L13
    .line 30
    throw v0
    :L14
    .line 28
    move-exception v1
    .line 29
    invoke-virtual { v1 }, Ljava/io/IOException;->printStackTrace()V
    goto :L13
.end method
