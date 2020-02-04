.class Lcom/baidu/titan/sample/InnerClassesSmali$1;
.super Ljava/lang/Object;
.implements Ljava/lang/Runnable;
.source "InnerClassesSmali.java"

.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/baidu/titan/sample/InnerClassesSmali;->doSomething()V
.end annotation
.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0
    name = null
.end annotation

.field final synthetic this$0:Lcom/baidu/titan/sample/InnerClassesSmali;

.method constructor <init>(Lcom/baidu/titan/sample/InnerClassesSmali;)V
    .registers 2
    .prologue
    .line 10
    iput-object p1, p0, Lcom/baidu/titan/sample/InnerClassesSmali$1;->this$0:Lcom/baidu/titan/sample/InnerClassesSmali;
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public run()V
    .registers 1
    .prologue
    .line 13
    invoke-static { }, Ljava/lang/Thread;->dumpStack()V
    .line 14
    return-void
.end method
