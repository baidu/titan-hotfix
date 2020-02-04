.class public Lcom/baidu/titan/sample/ChangeMethodCode$iter;
.super Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/SimpleInterceptor;-><init>()V

    return-void


.end method


# virtual methods

.method public invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/Object;
    .param p3    # Ljava/lang/Object;
    .param p4    # I
    .locals 2


    sparse-switch p1, :sswitch_data_0

    const v0, 0x0

    return-object v0

    :sswitch_0
    check-cast p2, Lcom/baidu/titan/sample/ChangeMethodCode;

    check-cast p3, Ljava/lang/String;

    # register: p2, p3, p4
    invoke-static/range {p2 .. p4}, Lcom/baidu/titan/sample/ChangeMethodCode$chg;->method1(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;

    move-result-object v1

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    iput-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    return-object v0

    :sswitch_1
    check-cast p2, Lcom/baidu/titan/sample/ChangeMethodCode;

    check-cast p3, Ljava/lang/String;

    # register: p2, p3, p4
    invoke-static/range {p2 .. p4}, Lcom/baidu/titan/sample/ChangeMethodCode$chg;->method2(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;

    move-result-object v1

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    iput-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    return-object v0

    :sswitch_2
    check-cast p2, Lcom/baidu/titan/sample/ChangeMethodCode;

    check-cast p3, Ljava/lang/String;

    # register: p2, p3, p4
    invoke-static/range {p2 .. p4}, Lcom/baidu/titan/sample/ChangeMethodCode$chg;->method3(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;

    move-result-object v1

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    iput-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    return-object v0

    :sswitch_3
    check-cast p2, Lcom/baidu/titan/sample/ChangeMethodCode;

    check-cast p3, Ljava/lang/String;

    # register: p2, p3, p4
    invoke-static/range {p2 .. p4}, Lcom/baidu/titan/sample/ChangeMethodCode$chg;->method4(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;

    move-result-object v1

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    iput-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    return-object v0

    :sswitch_4
    check-cast p2, Lcom/baidu/titan/sample/ChangeMethodCode;

    check-cast p3, Ljava/lang/String;

    # register: p2, p3, p4
    invoke-static/range {p2 .. p4}, Lcom/baidu/titan/sample/ChangeMethodCode$chg;->method5(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;

    move-result-object v1

    new-instance v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;

    invoke-direct {v0}, Lcom/baidu/titan/sdk/runtime/InterceptResult;-><init>()V

    iput-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    return-object v0


    :sswitch_data_0
    .sparse-switch
        0x10001 -> :sswitch_0
        0x10002 -> :sswitch_1
        0x10003 -> :sswitch_2
        0x10004 -> :sswitch_3
        0x10005 -> :sswitch_4
    .end sparse-switch
.end method
