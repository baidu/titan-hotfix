.class public Lcom/baidu/titan/patch/ClassClinitInterceptor;
.super Lcom/baidu/titan/sdk/runtime/SimpleClassClinitInterceptor;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/SimpleClassClinitInterceptor;-><init>()V

    return-void


.end method


# virtual methods

.method public invokeClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/String;
    .locals 5


    sparse-switch p1, :sswitch_data_0

    const v0, 0x0

    :cond_0
    :goto_0
    return-object v0

    :sswitch_0
    const-string v1, "Lcom/baidu/titan/sample/ChangeField;"

    invoke-virtual {v1, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_1

    new-instance v3, Lcom/baidu/titan/sample/ChangeField$iter;

    invoke-direct {v3}, Lcom/baidu/titan/sample/ChangeField$iter;-><init>()V

    const v4, 0x1

    goto :goto_1

    :cond_1
    const v0, 0x0

    goto :goto_0

    :goto_1
    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    iput-object v3, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->interceptor:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-eqz v4, :cond_0

    iget v4, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->flags:I

    or-int/lit8 v4, v4, 0x1

    iput v4, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->flags:I

    goto :goto_0


    :sswitch_data_0
    .sparse-switch
        0x9ba775e5 -> :sswitch_0
    .end sparse-switch
.end method

.method public invokePostClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/String;
    .locals 5


    sparse-switch p1, :sswitch_data_0

    :goto_0
    const v0, 0x0

    return-object v0

    :sswitch_0
    const-string v2, "Lcom/baidu/titan/sample/ChangeField;"

    invoke-virtual {v2, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    const v3, 0x0

    new-array v4, v3, [Ljava/lang/Object;

    invoke-static {v4}, Lcom/baidu/titan/sample/ChangeField$chg;->$staticInit([Ljava/lang/Object;)V

    goto :goto_0

    :cond_0
    goto :goto_0


    :sswitch_data_0
    .sparse-switch
        0x9ba775e5 -> :sswitch_0
    .end sparse-switch
.end method
