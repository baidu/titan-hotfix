.class Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;
.super Ljava/lang/Object;
.implements Ljava/lang/Runnable;
.source "InnerClassesSmali.java"

.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/baidu/titan/sample/InnerClassesSmali;
.end annotation
.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 2
    name = "PrivateRunnable"
.end annotation

.field final synthetic this$0:Lcom/baidu/titan/sample/InnerClassesSmali;

.method private constructor <init>(Lcom/baidu/titan/sample/InnerClassesSmali;)V
    .registers 2
    .prologue
    .line 30
    iput-object p1, p0, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;->this$0:Lcom/baidu/titan/sample/InnerClassesSmali;
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method synthetic constructor <init>(Lcom/baidu/titan/sample/InnerClassesSmali;Lcom/baidu/titan/sample/InnerClassesSmali$1;)V
    .registers 3
    .prologue
    .line 30
    invoke-direct { p0, p1 }, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;-><init>(Lcom/baidu/titan/sample/InnerClassesSmali;)V
    return-void
.end method

.method public run()V
    .registers 1
    .prologue
    .line 35
    return-void
.end method
