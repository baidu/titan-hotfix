.class public Lcom/baidu/titan/sample/ToastUtil$iter;
.super Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;-><init>()V

    return-void


.end method


# virtual methods

.method public invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p1    # I
    .param p2    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 8


    sparse-switch p1, :sswitch_data_0

    return-void

    :sswitch_0
    iget-object v2, p2, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    check-cast v2, Lcom/baidu/titan/sample/ToastUtil;

    iget-object v3, p2, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v4, 0x0

    aget-object v5, v3, v4

    check-cast v5, Landroid/content/Context;

    const/16 v4, 0x1

    aget-object v6, v3, v4

    check-cast v6, Ljava/lang/Integer;

    invoke-virtual {v6}, Ljava/lang/Integer;->intValue()I

    move-result v6

    move-object v7, p2

    invoke-static {v2, v5, v6, v7}, Lcom/baidu/titan/sample/ToastUtil$chg;->$instanceInitBody(Lcom/baidu/titan/sample/ToastUtil;Landroid/content/Context;ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void


    :sswitch_data_0
    .sparse-switch
        0x10001 -> :sswitch_0
    .end sparse-switch
.end method

.method public invokeLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .param p4    # Ljava/lang/Object;
    .locals 1


    sparse-switch p1, :sswitch_data_0

    const v0, 0x0

    return-object v0

    :sswitch_0
    check-cast p3, Landroid/content/Context;

    check-cast p4, Ljava/lang/String;

    # register: p3, p4
    invoke-static/range {p3 .. p4}, Lcom/baidu/titan/sample/ToastUtil$chg;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    return-object v0


    :sswitch_data_0
    .sparse-switch
        0x10002 -> :sswitch_0
    .end sparse-switch
.end method

.method public invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p1    # I
    .param p2    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 8


    sparse-switch p1, :sswitch_data_0

    return-void

    :sswitch_0
    iget-object v2, p2, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    check-cast v2, Lcom/baidu/titan/sample/ToastUtil;

    iget-object v3, p2, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v4, 0x0

    aget-object v5, v3, v4

    check-cast v5, Landroid/content/Context;

    const/16 v4, 0x1

    aget-object v6, v3, v4

    check-cast v6, Ljava/lang/Integer;

    invoke-virtual {v6}, Ljava/lang/Integer;->intValue()I

    move-result v6

    move-object v7, p2

    invoke-static {v2, v5, v6, v7}, Lcom/baidu/titan/sample/ToastUtil$chg;->$instanceUninit(Lcom/baidu/titan/sample/ToastUtil;Landroid/content/Context;ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void


    :sswitch_data_0
    .sparse-switch
        0x10001 -> :sswitch_0
    .end sparse-switch
.end method
