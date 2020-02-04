.class Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;
.super Ljava/lang/Object;
.implements Ljava/lang/Runnable;
.source "InnerClassesSmali.java"

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/baidu/titan/sample/InnerClassesSmali;
.end annotation
.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 10
    name = "PrivateStaticRunnable"
.end annotation

.method private constructor <init>()V
    .registers 1
    .prologue
    .line 22
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method synthetic constructor <init>(Lcom/baidu/titan/sample/InnerClassesSmali$1;)V
    .registers 2
    .prologue
    .line 22
    invoke-direct { p0 }, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;-><init>()V
    return-void
.end method

.method public static getName()Ljava/lang/String;
    .registers 1
    .prologue
    .line 30
    const-string v0, "PrivateStaticRunnable"
    return-object v0
.end method

.method public run()V
    .registers 1
    .prologue
    .line 26
    invoke-static { }, Ljava/lang/Thread;->dumpStack()V
    .line 27
    return-void
.end method
