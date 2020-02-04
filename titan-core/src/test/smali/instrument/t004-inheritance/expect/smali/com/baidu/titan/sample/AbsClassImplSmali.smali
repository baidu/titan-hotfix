.class public Lcom/baidu/titan/sample/AbsClassImplSmali;
.super Lcom/baidu/titan/sample/AbsClassSmali;
.source "AbsClassImplSmali.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;


# direct methods

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 3
    :cond_0
    invoke-direct {p0}, Lcom/baidu/titan/sample/AbsClassSmali;-><init>()V

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

    invoke-direct {p0}, Lcom/baidu/titan/sample/AbsClassSmali;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void


.end method


# virtual methods

.method public invoke()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 8
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const/high16 v3, 0x100000

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(B)V
    .param p1    # B
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 13
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100001

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeB(ILjava/lang/Object;B)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(C)V
    .param p1    # C
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 43
    :cond_0
    return-void

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p1}, Ljava/lang/Character;->valueOf(C)Ljava/lang/Character;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x100002

    move-object v2, p0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(D)V
    .param p1    # D
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 38
    :cond_0
    return-void

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p1}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x100003

    move-object v2, p0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(F)V
    .param p1    # F
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 33
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100004

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeF(ILjava/lang/Object;F)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(I)V
    .param p1    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 23
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100005

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeI(ILjava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(J)V
    .param p1    # J
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 28
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100006

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeJ(ILjava/lang/Object;J)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 53
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100007

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(S)V
    .param p1    # S
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 18
    :cond_0
    return-void

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p1}, Ljava/lang/Short;->valueOf(S)Ljava/lang/Short;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x100008

    move-object v2, p0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invoke(Z)V
    .param p1    # Z
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 48
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100009

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeZ(ILjava/lang/Object;Z)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLIL(ILjava/lang/String;)Ljava/lang/String;
    .param p1    # I
    .param p2    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 77
    :cond_0
    const/4 v0, 0x0

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10000a

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeIL(ILjava/lang/Object;ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method public invokeLL(Ljava/lang/String;)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 67
    :cond_0
    const/4 v0, 0x0

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10000b

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method public invokeLLI(Ljava/lang/String;I)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 72
    :cond_0
    const/4 v0, 0x0

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10000c

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method public invokeLV()Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 57
    :cond_0
    const/4 v0, 0x0

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x10000d

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    check-cast v1, Ljava/lang/String;

    return-object v1


.end method

.method public invokeVL(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/AbsClassImplSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 63
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10000e

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method
