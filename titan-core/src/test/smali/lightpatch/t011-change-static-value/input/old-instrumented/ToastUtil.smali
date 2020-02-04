.class public Lcom/baidu/titan/sample/ToastUtil;
.super Lcom/baidu/titan/sample/ToastDad;
.source "ToastUtil.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable; = null

.field public static final A:I = 0x3

.field public static b:I

.field public static debug:Z

.field public static final startTime:J


# instance fields
.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;

.field public mContext:Landroid/content/Context;

.field public tag:Ljava/lang/String;

.field public final toastStr:Ljava/lang/String;

.field public updateTime:J


# direct methods
.method public static constructor <clinit>()V
    .registers 6

    .prologue
    sget-object v0, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptorStorage;->$ic:Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;

    if-nez v0, :cond_11

    .line 21
    :cond_4
    const/4 v0, 0x6

    sput v0, Lcom/baidu/titan/sample/ToastUtil;->b:I

    .line 23
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v0

    sput-wide v0, Lcom/baidu/titan/sample/ToastUtil;->startTime:J

    .line 25
    const/4 v0, 0x1

    sput-boolean v0, Lcom/baidu/titan/sample/ToastUtil;->debug:Z

    return-void

    :cond_11
    const v1, -0x456e64da

    const-string v2, "Lcom/baidu/titan/sample/ToastUtil;"

    invoke-interface {v0, v1, v2}, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;->invokeClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v4

    if-eqz v4, :cond_4

    iget-object v3, v4, Lcom/baidu/titan/sdk/runtime/InterceptResult;->interceptor:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-eqz v3, :cond_22

    sput-object v3, Lcom/baidu/titan/sample/ToastUtil;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    :cond_22
    iget v5, v4, Lcom/baidu/titan/sdk/runtime/InterceptResult;->flags:I

    and-int/lit8 v5, v5, 0x1

    if-eqz v5, :cond_4

    invoke-interface {v0, v1, v2}, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;->invokePostClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;I)V
    .registers 9

    .prologue
    sget-object v0, Lcom/baidu/titan/sample/ToastUtil;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1f

    .line 31
    :cond_4
    add-int/lit8 v0, p2, 0x1

    invoke-direct {p0, v0}, Lcom/baidu/titan/sample/ToastDad;-><init>(I)V

    .line 15
    const-string/jumbo v0, "hello"

    iput-object v0, p0, Lcom/baidu/titan/sample/ToastUtil;->tag:Ljava/lang/String;

    .line 32
    iput-object p1, p0, Lcom/baidu/titan/sample/ToastUtil;->mContext:Landroid/content/Context;

    .line 33
    const-string/jumbo v0, "test"

    iput-object v0, p0, Lcom/baidu/titan/sample/ToastUtil;->toastStr:Ljava/lang/String;

    .line 34
    const-string/jumbo v0, "ToastUtil"

    const-string/jumbo v1, "what\'s up guy"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 35
    return-void

    :cond_1f
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x2

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    aput-object p1, v2, v3

    const/16 v3, 0x1

    invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10001

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_4

    and-int/lit8 v4, v3, 0x2

    iget-object v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->callArgs:[Ljava/lang/Object;

    const/16 v4, 0x0

    aget-object v5, v3, v4

    check-cast v5, Ljava/lang/Integer;

    invoke-virtual {v5}, Ljava/lang/Integer;->intValue()I

    move-result v5

    invoke-direct {p0, v5}, Lcom/baidu/titan/sample/ToastDad;-><init>(I)V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void
.end method

.method public static showToast(Landroid/content/Context;Ljava/lang/String;)V
    .registers 6

    .prologue
    sget-object v0, Lcom/baidu/titan/sample/ToastUtil;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_41

    .line 39
    :cond_4
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/Toast;->show()V

    .line 40
    const-string/jumbo v0, "ToastUtil"

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string/jumbo v2, "showToast "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string/jumbo v2, " "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    sget-wide v2, Lcom/baidu/titan/sample/ToastUtil;->startTime:J

    invoke-virtual {v1, v2, v3}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string/jumbo v2, " --->"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    sget v2, Lcom/baidu/titan/sample/ToastUtil;->b:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 43
    return-void

    :cond_41
    move-object v1, v0

    const v2, 0x10002

    const/16 v3, 0x0

    invoke-interface/range {v1 .. v5}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_4

    return-void
.end method


# virtual methods
.method public logInt(I)V
    .registers 6

    .prologue
    sget-object v0, Lcom/baidu/titan/sample/ToastUtil;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_26

    .line 46
    :cond_4
    sparse-switch p1, :sswitch_data_30

    .line 54
    const-string/jumbo v0, "ToastUtil"

    invoke-static {p1}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 57
    :goto_11
    return-void

    .line 48
    :sswitch_12
    const-string/jumbo v0, "ToastUtil"

    const-string/jumbo v1, "100"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_11

    .line 51
    :sswitch_1c
    const-string/jumbo v0, "ToastUtil"

    const-string/jumbo v1, "200"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_11

    :cond_26
    move-object v2, v0

    const/high16 v3, 0x100000

    invoke-interface/range {v2 .. v5}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeI(ILjava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_4

    return-void

    .line 46
    :sswitch_data_30
    .sparse-switch
        0x64 -> :sswitch_12
        0xc8 -> :sswitch_1c
    .end sparse-switch
.end method

.method public setUpdateTime(J)V
    .registers 7

    .prologue
    sget-object v0, Lcom/baidu/titan/sample/ToastUtil;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_7

    .line 60
    :cond_4
    iput-wide p1, p0, Lcom/baidu/titan/sample/ToastUtil;->updateTime:J

    .line 63
    return-void

    :cond_7
    move-object v2, v0

    const v3, 0x100001

    invoke-interface/range {v2 .. v6}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeJ(ILjava/lang/Object;J)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_4

    return-void
.end method
