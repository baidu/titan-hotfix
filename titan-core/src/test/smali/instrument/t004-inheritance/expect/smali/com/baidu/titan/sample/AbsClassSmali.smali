.class public abstract Lcom/baidu/titan/sample/AbsClassSmali;
.super Ljava/lang/Object;
.source "AbsClassSmali.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;


# direct methods

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/AbsClassSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

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


# virtual methods

.method public abstract invoke()V
.end method

.method public abstract invoke(B)V
    .param p1    # B
.end method

.method public abstract invoke(C)V
    .param p1    # C
.end method

.method public abstract invoke(D)V
    .param p1    # D
.end method

.method public abstract invoke(F)V
    .param p1    # F
.end method

.method public abstract invoke(I)V
    .param p1    # I
.end method

.method public abstract invoke(J)V
    .param p1    # J
.end method

.method public abstract invoke(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
.end method

.method public abstract invoke(S)V
    .param p1    # S
.end method

.method public abstract invoke(Z)V
    .param p1    # Z
.end method

.method public abstract invokeLIL(ILjava/lang/String;)Ljava/lang/String;
    .param p1    # I
    .param p2    # Ljava/lang/String;
.end method

.method public abstract invokeLL(Ljava/lang/String;)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
.end method

.method public abstract invokeLLI(Ljava/lang/String;I)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .param p2    # I
.end method

.method public abstract invokeLV()Ljava/lang/String;
.end method

.method public abstract invokeVL(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
.end method
