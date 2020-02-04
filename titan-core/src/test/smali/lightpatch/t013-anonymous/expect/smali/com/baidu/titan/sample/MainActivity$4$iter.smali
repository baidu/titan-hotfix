.class public Lcom/baidu/titan/sample/MainActivity$4$iter;
.super Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;-><init>()V

    return-void


.end method


# virtual methods

.method public invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .locals 1


    sparse-switch p1, :sswitch_data_0

    const v0, 0x0

    return-object v0

    :sswitch_0
    check-cast p2, Lcom/baidu/titan/sample/MainActivity$4;

    check-cast p3, Landroid/view/View;

    # register: p2, p3
    invoke-static/range {p2 .. p3}, Lcom/baidu/titan/sample/MainActivity$4$chg;->onClick(Lcom/baidu/titan/sample/MainActivity$4;Landroid/view/View;)V

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    return-object v0


    :sswitch_data_0
    .sparse-switch
        0x100000 -> :sswitch_0
    .end sparse-switch
.end method
