.class public Lcom/baidu/titan/sample/ToastShower;
.super Ljava/lang/Object;
.source "ToastShower.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields
.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;


# direct methods
.method public constructor <init>()V
    .registers 6

    .prologue
    sget-object v0, Lcom/baidu/titan/sample/ToastShower;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_8

    .line 5
    :cond_4
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void

    :cond_8
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const/high16 v2, 0x10000

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_4

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void
.end method


# virtual methods
.method public showToast(Landroid/content/Context;)V
    .registers 6

    .prologue
    sget-object v0, Lcom/baidu/titan/sample/ToastShower;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_10

    .line 7
    :cond_4
    new-instance v0, Lcom/baidu/titan/sample/ToastUtil;

    const/4 v1, 0x0

    invoke-direct {v0, p1, v1}, Lcom/baidu/titan/sample/ToastUtil;-><init>(Landroid/content/Context;I)V

    .line 8
    iget-object v1, v0, Lcom/baidu/titan/sample/ToastUtil;->toastStr:Ljava/lang/String;

    invoke-static {p1, v1}, Lcom/baidu/titan/sample/ToastUtil;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    .line 9
    return-void

    :cond_10
    move-object v2, v0

    const/high16 v3, 0x100000

    invoke-interface/range {v2 .. v5}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_4

    return-void
.end method
