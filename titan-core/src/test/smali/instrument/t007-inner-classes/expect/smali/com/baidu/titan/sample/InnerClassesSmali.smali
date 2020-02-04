.class public Lcom/baidu/titan/sample/InnerClassesSmali;
.super Ljava/lang/Object;
.source "InnerClassesSmali.java"

# annotations

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;,
        Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;
    }
.end annotation


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;

.field public list:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List",
            "<",
            "Ljava/lang/Runnable;",
            ">;"
        }
    .end annotation
.end field


# direct methods

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/InnerClassesSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 6
    :cond_0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 7
    new-instance v0, Ljava/util/LinkedList;

    invoke-direct {v0}, Ljava/util/LinkedList;-><init>()V

    iput-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;

    return-void

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const/high16 v2, 0x10000

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void


.end method

.method private doSomething()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/InnerClassesSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v2, 0x0

    .line 10
    iget-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;

    new-instance v1, Lcom/baidu/titan/sample/InnerClassesSmali$1;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/InnerClassesSmali$1;-><init>(Lcom/baidu/titan/sample/InnerClassesSmali;)V

    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 17
    iget-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;

    new-instance v1, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;

    invoke-direct {v1, v2}, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateStaticRunnable;-><init>(Lcom/baidu/titan/sample/InnerClassesSmali$1;)V

    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 19
    iget-object v0, p0, Lcom/baidu/titan/sample/InnerClassesSmali;->list:Ljava/util/List;

    new-instance v1, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;

    invoke-direct {v1, p0, v2}, Lcom/baidu/titan/sample/InnerClassesSmali$PrivateRunnable;-><init>(Lcom/baidu/titan/sample/InnerClassesSmali;Lcom/baidu/titan/sample/InnerClassesSmali$1;)V

    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 20
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10001

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method
