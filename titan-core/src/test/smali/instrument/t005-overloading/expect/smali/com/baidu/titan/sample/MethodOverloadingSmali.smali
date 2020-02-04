.class public Lcom/baidu/titan/sample/MethodOverloadingSmali;
.super Ljava/lang/Object;
.source "MethodOverloadingSmali.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;


# direct methods

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

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

.method public static invokeC(C)V
    .param p0    # C
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 17
    :cond_0
    return-void

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p0}, Ljava/lang/Character;->valueOf(C)Ljava/lang/Character;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x10001

    const/4 v2, 0x0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public static invokeD(D)V
    .param p0    # D
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 13
    :cond_0
    return-void

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p0}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x10002

    const/4 v2, 0x0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public static invokeS(S)V
    .param p0    # S
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 8
    :cond_0
    return-void

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p0}, Ljava/lang/Short;->valueOf(S)Ljava/lang/Short;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x10003

    const/4 v2, 0x0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method


# virtual methods

.method public invokeB(B)V
    .param p1    # B
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 185
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const/high16 v3, 0x100000

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeB(ILjava/lang/Object;B)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeF(F)V
    .param p1    # F
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 113
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100001

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeF(ILjava/lang/Object;F)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeI(I)V
    .param p1    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 41
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100002

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeI(ILjava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeII(II)V
    .param p1    # I
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 95
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100003

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeII(ILjava/lang/Object;II)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeIII(III)V
    .param p1    # I
    .param p2    # I
    .param p3    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 191
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100004

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeIII(ILjava/lang/Object;III)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeIIII(IIII)V
    .param p1    # I
    .param p2    # I
    .param p3    # I
    .param p4    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 179
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100005

    # register: v2, v3, p0, p1, p2, p3, p4
    invoke-interface/range {v2 .. p4}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeIIII(ILjava/lang/Object;IIII)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeIIL(IILjava/lang/Object;)V
    .param p1    # I
    .param p2    # I
    .param p3    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 143
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100006

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeIIL(ILjava/lang/Object;IILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeIL(ILjava/lang/Object;)V
    .param p1    # I
    .param p2    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 65
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100007

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeIL(ILjava/lang/Object;ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeILL(ILjava/lang/Object;Ljava/lang/Object;)V
    .param p1    # I
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 125
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100008

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeILL(ILjava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeJ(J)V
    .param p1    # J
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 77
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100009

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeJ(ILjava/lang/Object;J)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeJL(JLjava/lang/Object;)V
    .param p1    # J
    .param p3    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 155
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10000a

    # register: v2, v3, p0, p1, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeJL(ILjava/lang/Object;JLjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeL(Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 29
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10000b

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLF(Ljava/lang/Object;F)V
    .param p1    # Ljava/lang/Object;
    .param p2    # F
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 161
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10000c

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLF(ILjava/lang/Object;Ljava/lang/Object;F)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLI(Ljava/lang/Object;I)V
    .param p1    # Ljava/lang/Object;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 47
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10000d

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLII(Ljava/lang/Object;II)V
    .param p1    # Ljava/lang/Object;
    .param p2    # I
    .param p3    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 101
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10000e

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLII(ILjava/lang/Object;Ljava/lang/Object;II)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLIII(Ljava/lang/Object;III)V
    .param p1    # Ljava/lang/Object;
    .param p2    # I
    .param p3    # I
    .param p4    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 149
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10000f

    # register: v2, v3, p0, p1, p2, p3, p4
    invoke-interface/range {v2 .. p4}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLIII(ILjava/lang/Object;Ljava/lang/Object;III)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLIL(Ljava/lang/Object;ILjava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # I
    .param p3    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 107
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100010

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLIL(ILjava/lang/Object;Ljava/lang/Object;ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLILL(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # I
    .param p3    # Ljava/lang/Object;
    .param p4    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 209
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100011

    # register: v2, v3, p0, p1, p2, p3, p4
    invoke-interface/range {v2 .. p4}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLILL(ILjava/lang/Object;Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLJ(Ljava/lang/Object;J)V
    .param p1    # Ljava/lang/Object;
    .param p2    # J
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 119
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100012

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLJ(ILjava/lang/Object;Ljava/lang/Object;J)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLL(Ljava/lang/Object;Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 35
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100013

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLI(Ljava/lang/Object;Ljava/lang/Object;I)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 89
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100014

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLI(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLII(Ljava/lang/Object;Ljava/lang/Object;II)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # I
    .param p4    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 197
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100015

    # register: v2, v3, p0, p1, p2, p3, p4
    invoke-interface/range {v2 .. p4}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLII(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;II)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLIL(Ljava/lang/Object;Ljava/lang/Object;ILjava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # I
    .param p4    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 203
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100016

    # register: v2, v3, p0, p1, p2, p3, p4
    invoke-interface/range {v2 .. p4}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLIL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLL(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 53
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100017

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLLI(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .param p4    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 173
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100018

    # register: v2, v3, p0, p1, p2, p3, p4
    invoke-interface/range {v2 .. p4}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLLI(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLLL(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .param p4    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 83
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100019

    # register: v2, v3, p0, p1, p2, p3, p4
    invoke-interface/range {v2 .. p4}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLLLL(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .param p4    # Ljava/lang/Object;
    .param p5    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 137
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10001a

    # register: v2, v3, p0, p1, p2, p3, p4, p5
    invoke-interface/range {v2 .. p5}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLLLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLLZ(Ljava/lang/Object;Ljava/lang/Object;Z)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .param p3    # Z
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 131
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10001b

    # register: v2, v3, p0, p1, p2, p3
    invoke-interface/range {v2 .. p3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLLZ(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Z)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeLZ(Ljava/lang/Object;Z)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Z
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 71
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10001c

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLZ(ILjava/lang/Object;Ljava/lang/Object;Z)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeV()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 23
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10001d

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeZ(Z)V
    .param p1    # Z
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 59
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10001e

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeZ(ILjava/lang/Object;Z)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public invokeZL(ZLjava/lang/Object;)V
    .param p1    # Z
    .param p2    # Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 167
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x10001f

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeZL(ILjava/lang/Object;ZLjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public returnB()B
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 220
    :cond_0
    const/16 v0, 0x7f

    return v0

    :cond_1
    move-object v2, v0

    const v3, 0x100020

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-byte v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->byteValue:B

    return v1


.end method

.method public returnC()C
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 232
    :cond_0
    const v0, 0xffff

    return v0

    :cond_1
    move-object v2, v0

    const v3, 0x100021

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-char v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->charValue:C

    return v1


.end method

.method public returnD()D
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 256
    :cond_0
    const-wide v0, 0x7fefffffffffffffL

    return-wide v0

    :cond_1
    move-object v2, v0

    const v3, 0x100022

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-wide v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->doubleValue:D

    return-wide v1


.end method

.method public returnF()F
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 250
    :cond_0
    const v0, 0x7f7fffff

    return v0

    :cond_1
    move-object v2, v0

    const v3, 0x100023

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->floatValue:F

    return v1


.end method

.method public returnI()I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 238
    :cond_0
    const v0, 0x7fffffff

    return v0

    :cond_1
    move-object v2, v0

    const v3, 0x100024

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->intValue:I

    return v1


.end method

.method public returnJ()J
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 244
    :cond_0
    const-wide v0, 0x7fffffffffffffffL

    return-wide v0

    :cond_1
    move-object v2, v0

    const v3, 0x100025

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-wide v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->longValue:J

    return-wide v1


.end method

.method public returnL()Ljava/lang/Object;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 262
    :cond_0
    new-instance v0, Ljava/lang/Object;

    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    return-object v0

    :cond_1
    move-object v2, v0

    const v3, 0x100026

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    return-object v1


.end method

.method public returnS()S
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 226
    :cond_0
    const/16 v0, 0x7fff

    return v0

    :cond_1
    move-object v2, v0

    const v3, 0x100027

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-short v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->shortValue:S

    return v1


.end method

.method public returnZ()Z
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/MethodOverloadingSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 214
    :cond_0
    const/4 v0, 0x1

    return v0

    :cond_1
    move-object v2, v0

    const v3, 0x100028

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-boolean v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->booleanValue:Z

    return v1


.end method
