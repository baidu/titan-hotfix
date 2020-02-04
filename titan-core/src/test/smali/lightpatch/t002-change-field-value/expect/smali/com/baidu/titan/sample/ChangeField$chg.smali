.class public Lcom/baidu/titan/sample/ChangeField$chg;
.super Ljava/lang/Object;


# direct methods

.method public static $instanceInitBody(Lcom/baidu/titan/sample/ChangeField;Lcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p0    # Lcom/baidu/titan/sample/ChangeField;
    .param p1    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 2


    iget-object v1, p1, Lcom/baidu/titan/sdk/runtime/InitContext;->locals:[Ljava/lang/Object;

    const v0, 0x2

    .line 5
    iput v0, p0, Lcom/baidu/titan/sample/ChangeField;->initI:I

    .line 8
    invoke-static {p0}, Lcom/baidu/titan/sample/ChangeField$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/ChangeField;)Lcom/baidu/titan/sample/ChangeField$fdh;

    move-result-object v1

    iput v0, v1, Lcom/baidu/titan/sample/ChangeField$fdh;->finalI:I

    .line 12
    const-string v0, "change"

    iput-object v0, p0, Lcom/baidu/titan/sample/ChangeField;->initS:Ljava/lang/String;

    .line 15
    const-string v0, "change"

    invoke-static {p0}, Lcom/baidu/titan/sample/ChangeField$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/ChangeField;)Lcom/baidu/titan/sample/ChangeField$fdh;

    move-result-object v1

    iput-object v0, v1, Lcom/baidu/titan/sample/ChangeField$fdh;->finalL:Ljava/lang/String;

    return-void


.end method

.method public static $instanceUninit(Lcom/baidu/titan/sample/ChangeField;Lcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p0    # Lcom/baidu/titan/sample/ChangeField;
    .param p1    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 2


    const/4 v0, 0x2

    .line 3
    const v1, 0x1

    new-array v1, v1, [Ljava/lang/Object;

    iput-object v1, p1, Lcom/baidu/titan/sdk/runtime/InitContext;->locals:[Ljava/lang/Object;

    iget v1, p1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    or-int/lit8 v1, v1, 0x1

    iput v1, p1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    return-void


.end method

.method public static $staticInit([Ljava/lang/Object;)V
    .param p0    # [Ljava/lang/Object;
    .locals 1


    .line 7
    const/4 v0, 0x2

    sput v0, Lcom/baidu/titan/sample/ChangeField;->initStaticI:I

    .line 14
    const-string v0, "change"

    sput-object v0, Lcom/baidu/titan/sample/ChangeField;->initStaticL:Ljava/lang/String;

    return-void


.end method
