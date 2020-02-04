.class public Lcom/baidu/titan/sample/InnerClassesSmali;
.super Ljava/lang/Object;
.source "InnerClassesSmali.java"

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;,
        Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;
    }
.end annotation

.field list:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List",
            "<",
            "Ljava/lang/Runnable;",
            ">;"
        }
    .end annotation
.end field

.method public constructor <init>()V
    .registers 2
    .prologue
    .line 6
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 7
    new-instance v0, Ljava/util/LinkedList;
    invoke-direct { v0 }, Ljava/util/LinkedList;-><init>()V
    iput-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;
    return-void
.end method

.method private doSomething()V
    .registers 4
    .prologue
    const/4 v2, 0
    .line 10
    iget-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;
    new-instance v1, Lcom/baidu/titan/sample/InnerClassesSmali$1;
    invoke-direct { v1, p0 }, Lcom/baidu/titan/sample/InnerClassesSmali$1;-><init>(Lcom/baidu/titan/sample/InnerClassesSmali;)V
    invoke-interface { v0, v1 }, Ljava/util/List;->add(Ljava/lang/Object;)Z
    .line 17
    iget-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;
    new-instance v1, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;
    invoke-direct { v1, v2 }, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;-><init>(Lcom/baidu/titan/sample/InnerClassesSmali$1;)V
    invoke-interface { v0, v1 }, Ljava/util/List;->add(Ljava/lang/Object;)Z
    .line 19
    iget-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;
    new-instance v1, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;
    invoke-direct { v1, p0, v2 }, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;-><init>(Lcom/baidu/titan/sample/InnerClassesSmali;Lcom/baidu/titan/sample/InnerClassesSmali$1;)V
    invoke-interface { v0, v1 }, Ljava/util/List;->add(Ljava/lang/Object;)Z
    .line 20
    return-void
.end method
