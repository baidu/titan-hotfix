.class public Lcom/baidu/titan/sample/ChangeMethodArgs;
.super Ljava/lang/Object;
.source "ChangeMethodArgs.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;


# direct methods

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 3
    :cond_0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

.method private method1(Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 6
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x10001

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method private method2()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 10
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x10002

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method private method3()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 14
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x10003

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method private method4()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 18
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x10004

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static staticMethod1(Ljava/lang/Object;)V
    .param p0    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 22
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10005

    const/16 v3, 0x0

    # register: v1, v2, v3, p0
    invoke-interface/range {v1 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static staticMethod2()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 26
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10006

    const/16 v3, 0x0

    # register: v1, v2, v3
    invoke-interface/range {v1 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static staticMethod3()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 30
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10007

    const/16 v3, 0x0

    # register: v1, v2, v3
    invoke-interface/range {v1 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static staticMethod4()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodArgs;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 34
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10008

    const/16 v3, 0x0

    # register: v1, v2, v3
    invoke-interface/range {v1 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method
