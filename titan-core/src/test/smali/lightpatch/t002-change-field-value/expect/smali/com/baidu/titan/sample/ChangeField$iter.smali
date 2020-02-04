.class public Lcom/baidu/titan/sample/ChangeField$iter;
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
    .locals 6


    sparse-switch p1, :sswitch_data_0

    return-void

    :sswitch_0
    iget-object v2, p2, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    check-cast v2, Lcom/baidu/titan/sample/ChangeField;

    move-object v5, p2

    invoke-static {v2, v5}, Lcom/baidu/titan/sample/ChangeField$chg;->$instanceInitBody(Lcom/baidu/titan/sample/ChangeField;Lcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void


    :sswitch_data_0
    .sparse-switch
        0x10001 -> :sswitch_0
    .end sparse-switch
.end method

.method public invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p1    # I
    .param p2    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 6


    sparse-switch p1, :sswitch_data_0

    return-void

    :sswitch_0
    iget-object v2, p2, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    check-cast v2, Lcom/baidu/titan/sample/ChangeField;

    move-object v5, p2

    invoke-static {v2, v5}, Lcom/baidu/titan/sample/ChangeField$chg;->$instanceUninit(Lcom/baidu/titan/sample/ChangeField;Lcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void


    :sswitch_data_0
    .sparse-switch
        0x10001 -> :sswitch_0
    .end sparse-switch
.end method
