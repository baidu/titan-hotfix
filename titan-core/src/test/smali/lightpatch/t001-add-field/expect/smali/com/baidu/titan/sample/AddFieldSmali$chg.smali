.class public Lcom/baidu/titan/sample/AddFieldSmali$chg;
.super Ljava/lang/Object;


# direct methods

.method public static $instanceInitBody(Lcom/baidu/titan/sample/AddFieldSmali;Lcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p0    # Lcom/baidu/titan/sample/AddFieldSmali;
    .param p1    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 2


    iget-object v1, p1, Lcom/baidu/titan/sdk/runtime/InitContext;->locals:[Ljava/lang/Object;

    const v0, 0x1

    .line 7
    invoke-static {p0}, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/AddFieldSmali;)Lcom/baidu/titan/sample/AddFieldSmali$fdh;

    move-result-object v1

    iput v0, v1, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->newInitI:I

    .line 10
    invoke-static {p0}, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/AddFieldSmali;)Lcom/baidu/titan/sample/AddFieldSmali$fdh;

    move-result-object v1

    iput v0, v1, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->newFinalI:I

    .line 14
    const-string v0, "s"

    invoke-static {p0}, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/AddFieldSmali;)Lcom/baidu/titan/sample/AddFieldSmali$fdh;

    move-result-object v1

    iput-object v0, v1, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->newInitS:Ljava/lang/String;

    .line 17
    const-string v0, "s"

    invoke-static {p0}, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/AddFieldSmali;)Lcom/baidu/titan/sample/AddFieldSmali$fdh;

    move-result-object v1

    iput-object v0, v1, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->newFinalL:Ljava/lang/String;

    return-void


.end method

.method public static $instanceUninit(Lcom/baidu/titan/sample/AddFieldSmali;Lcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p0    # Lcom/baidu/titan/sample/AddFieldSmali;
    .param p1    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 2


    const/4 v0, 0x1

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


    .line 9
    const/4 v0, 0x1

    sput v0, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->newInitStaticI:I

    .line 16
    const-string v0, "s"

    sput-object v0, Lcom/baidu/titan/sample/AddFieldSmali$fdh;->newInitStaticL:Ljava/lang/String;

    return-void


.end method
