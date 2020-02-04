.class public Lcom/baidu/titan/sample/ToastUtil$iter;
.super Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;-><init>()V

    return-void


.end method


# virtual methods

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
