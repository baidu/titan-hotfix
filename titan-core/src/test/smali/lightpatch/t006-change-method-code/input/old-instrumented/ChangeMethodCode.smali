.class public Lcom/baidu/titan/sample/ChangeMethodCode;
.super Ljava/lang/Object;
.source "ChangeMethodCode.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;


# direct methods

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodCode;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 11
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

.method private method1(Ljava/lang/String;I)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodCode;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 14
    :cond_0
    const-string v0, ""

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10001

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method private method2(Ljava/lang/String;I)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodCode;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 20
    :cond_0
    const-string v0, ""

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10002

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method private method3(Ljava/lang/String;I)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodCode;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 26
    :cond_0
    const-string v0, ""

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10003

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method private method4(Ljava/lang/String;I)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodCode;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 33
    :cond_0
    const-string v0, ""

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10004

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method private method5(Ljava/lang/String;I)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/ChangeMethodCode;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 52
    :cond_0
    const-string v0, ""

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10005

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method
