.class public Lcom/baidu/titan/sample/ChangeMethodCode;
.super Ljava/lang/Object;
.source "ChangeMethodCode.java"

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 11
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method private method1(Ljava/lang/String;I)Ljava/lang/String;
    .registers 4
    .prologue
    .line 13
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    invoke-virtual { v0, p1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    .line 14
    const-string v0, ""
    return-object v0
.end method

.method private method2(Ljava/lang/String;I)Ljava/lang/String;
    .registers 4
    .prologue
    .line 19
    invoke-static { }, Ljava/lang/Thread;->dumpStack()V
    .line 20
    const-string v0, ""
    return-object v0
.end method

.method private method3(Ljava/lang/String;I)Ljava/lang/String;
    .registers 5
    .prologue
    .line 25
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    invoke-virtual { p1 }, Ljava/lang/String;->length()I
    move-result v1
    invoke-virtual { v0, v1 }, Ljava/io/PrintStream;->println(I)V
    .line 26
    const-string v0, ""
    return-object v0
.end method

.method private method4(Ljava/lang/String;I)Ljava/lang/String;
    .registers 5
    .prologue
    .line 31
    new-instance v0, Ljava/util/ArrayList;
    invoke-direct { v0, p2 }, Ljava/util/ArrayList;-><init>(I)V
    .line 32
    const/4 v1, 1
    invoke-static { v1 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v1
    invoke-interface { v0, v1 }, Ljava/util/List;->add(Ljava/lang/Object;)Z
    .line 33
    const-string v0, ""
    return-object v0
.end method

.method private method5(Ljava/lang/String;I)Ljava/lang/String;
    .catch Ljava/io/IOException; { :L0 .. :L1 } :L5
    .catchall { :L0 .. :L1 } :L11
    .catch Ljava/io/IOException; { :L2 .. :L3 } :L4
    .catchall { :L6 .. :L7 } :L11
    .catch Ljava/io/IOException; { :L8 .. :L9 } :L10
    .catch Ljava/io/IOException; { :L12 .. :L13 } :L14
    .registers 7
    .prologue
    .line 38
    const/4 v1, 0
    :L0
    .line 40
    new-instance v0, Ljava/io/BufferedInputStream;
    new-instance v2, Ljava/io/FileInputStream;
    new-instance v3, Ljava/io/File;
    invoke-direct { v3, p1 }, Ljava/io/File;-><init>(Ljava/lang/String;)V
    invoke-direct { v2, v3 }, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    invoke-direct { v0, v2 }, Ljava/io/BufferedInputStream;-><init>(Ljava/io/InputStream;)V
    :L1
    .line 44
    if-eqz v0, :L3
    :L2
    .line 46
    invoke-virtual { v0 }, Ljava/io/InputStream;->close()V
    :L3
    .line 52
    const-string v0, ""
    return-object v0
    :L4
    .line 47
    move-exception v0
    .line 48
    invoke-virtual { v0 }, Ljava/io/IOException;->printStackTrace()V
    goto :L3
    :L5
    .line 41
    move-exception v0
    :L6
    .line 42
    invoke-virtual { v0 }, Ljava/io/IOException;->printStackTrace()V
    :L7
    .line 44
    if-eqz v1, :L3
    :L8
    .line 46
    invoke-virtual { v1 }, Ljava/io/InputStream;->close()V
    :L9
    goto :L3
    :L10
    .line 47
    move-exception v0
    .line 48
    invoke-virtual { v0 }, Ljava/io/IOException;->printStackTrace()V
    goto :L3
    :L11
    .line 44
    move-exception v0
    if-eqz v1, :L13
    :L12
    .line 46
    invoke-virtual { v1 }, Ljava/io/InputStream;->close()V
    :L13
    .line 49
    throw v0
    :L14
    .line 47
    move-exception v1
    .line 48
    invoke-virtual { v1 }, Ljava/io/IOException;->printStackTrace()V
    goto :L13
.end method
