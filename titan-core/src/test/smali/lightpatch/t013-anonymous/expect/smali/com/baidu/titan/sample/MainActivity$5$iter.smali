.class public Lcom/baidu/titan/sample/MainActivity$5$iter;
.super Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;-><init>()V

    return-void


.end method


# virtual methods

.method public invokeIL(ILjava/lang/Object;ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/Object;
    .param p3    # I
    .param p4    # Ljava/lang/Object;
    .locals 1


    sparse-switch p1, :sswitch_data_0

    const v0, 0x0

    return-object v0

    :sswitch_0
    check-cast p2, Lcom/baidu/titan/sample/MainActivity$5;

    check-cast p4, Landroid/os/Bundle;

    # register: p2, p3, p4
    invoke-static/range {p2 .. p4}, Lcom/baidu/titan/sample/MainActivity$5$chg;->onPatchInstalled(Lcom/baidu/titan/sample/MainActivity$5;ILandroid/os/Bundle;)V

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    return-object v0


    :sswitch_data_0
    .sparse-switch
        0x100000 -> :sswitch_0
    .end sparse-switch
.end method
