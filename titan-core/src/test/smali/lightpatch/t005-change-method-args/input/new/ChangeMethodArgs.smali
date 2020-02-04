.class public Lcom/baidu/titan/sample/ChangeMethodArgs;
.super Ljava/lang/Object;
.source "ChangeMethodArgs.java"

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 3
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method private method1(I)V
    .registers 2
    .prologue
    .line 39
    return-void
.end method

.method private method2(Ljava/lang/String;)V
    .registers 2
    .prologue
    .line 43
    return-void
.end method

.method private method3()Ljava/lang/String;
    .registers 2
    .prologue
    .line 46
    const-string v0, "s"
    return-object v0
.end method

.method private method4(Ljava/lang/String;)Ljava/lang/String;
    .registers 4
    .prologue
    .line 50
    new-instance v0, Ljava/lang/StringBuilder;
    invoke-direct { v0 }, Ljava/lang/StringBuilder;-><init>()V
    const-string v1, "return: "
    invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v0
    invoke-virtual { v0, p1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v0
    invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method

.method private static staticMethod1(I)V
    .registers 1
    .prologue
    .line 55
    return-void
.end method

.method private static staticMethod2(Ljava/lang/String;)V
    .registers 1
    .prologue
    .line 59
    return-void
.end method

.method private static staticMethod3()Ljava/lang/String;
    .registers 1
    .prologue
    .line 62
    const-string v0, "s"
    return-object v0
.end method

.method private static staticMethod4(Ljava/lang/String;)Ljava/lang/String;
    .registers 3
    .prologue
    .line 66
    new-instance v0, Ljava/lang/StringBuilder;
    invoke-direct { v0 }, Ljava/lang/StringBuilder;-><init>()V
    const-string v1, "return: "
    invoke-virtual { v0, v1 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v0
    invoke-virtual { v0, p0 }, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v0
    invoke-virtual { v0 }, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v0
    return-object v0
.end method
