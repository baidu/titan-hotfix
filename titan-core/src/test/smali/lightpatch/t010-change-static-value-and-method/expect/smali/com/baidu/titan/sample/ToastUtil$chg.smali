.class public Lcom/baidu/titan/sample/ToastUtil$chg;
.super Lcom/baidu/titan/sample/ToastDad;


# direct methods

.method public static $instanceInitBody(Lcom/baidu/titan/sample/ToastUtil;Landroid/content/Context;ILcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p0    # Lcom/baidu/titan/sample/ToastUtil;
    .param p1    # Landroid/content/Context;
    .param p2    # I
    .param p3    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 4


    iget-object v1, p3, Lcom/baidu/titan/sdk/runtime/InitContext;->locals:[Ljava/lang/Object;

    const v2, 0x0

    aget-object v3, v1, v2

    check-cast v3, Ljava/lang/Integer;

    invoke-virtual {v3}, Ljava/lang/Integer;->intValue()I

    move-result v0

    const v2, 0x1

    aget-object p1, v1, v2

    check-cast p1, Landroid/content/Context;

    const v2, 0x2

    aget-object v3, v1, v2

    check-cast v3, Ljava/lang/Integer;

    invoke-virtual {v3}, Ljava/lang/Integer;->intValue()I

    move-result p2

    .line 15
    const-string/jumbo v0, "hello"

    iput-object v0, p0, Lcom/baidu/titan/sample/ToastUtil;->tag:Ljava/lang/String;

    .line 32
    iput-object p1, p0, Lcom/baidu/titan/sample/ToastUtil;->mContext:Landroid/content/Context;

    .line 33
    const-string/jumbo v0, "test"

    invoke-static {p0}, Lcom/baidu/titan/sample/ToastUtil$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/ToastUtil;)Lcom/baidu/titan/sample/ToastUtil$fdh;

    move-result-object v2

    iput-object v0, v2, Lcom/baidu/titan/sample/ToastUtil$fdh;->toastStr:Ljava/lang/String;

    .line 34
    const-string/jumbo v0, "ToastUtil"

    const-string/jumbo v1, "what's up guy xxx"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 35
    return-void


.end method

.method public static $instanceUninit(Lcom/baidu/titan/sample/ToastUtil;Landroid/content/Context;ILcom/baidu/titan/sdk/runtime/InitContext;)V
    .param p0    # Lcom/baidu/titan/sample/ToastUtil;
    .param p1    # Landroid/content/Context;
    .param p2    # I
    .param p3    # Lcom/baidu/titan/sdk/runtime/InitContext;
    .locals 4


    .line 31
    add-int/lit8 v0, p2, 0x1

    const v1, 0x3

    new-array v1, v1, [Ljava/lang/Object;

    iput-object v1, p3, Lcom/baidu/titan/sdk/runtime/InitContext;->locals:[Ljava/lang/Object;

    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    const v2, 0x0

    aput-object v3, v1, v2

    const v2, 0x1

    aput-object p1, v1, v2

    invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    const v2, 0x2

    aput-object v3, v1, v2

    const v1, 0x1

    new-array v1, v1, [Ljava/lang/Object;

    iput-object v1, p3, Lcom/baidu/titan/sdk/runtime/InitContext;->callArgs:[Ljava/lang/Object;

    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    const v2, 0x0

    aput-object v3, v1, v2

    iget v1, p3, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    or-int/lit8 v1, v1, 0x1

    iput v1, p3, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    return-void


.end method

.method public static $staticInit([Ljava/lang/Object;)V
    .param p0    # [Ljava/lang/Object;
    .locals 1


    .line 21
    const/4 v0, 0x4

    sput v0, Lcom/baidu/titan/sample/ToastUtil$fdh;->b:I

    .line 25
    const/4 v0, 0x1

    sput-boolean v0, Lcom/baidu/titan/sample/ToastUtil$fdh;->debug:Z

    return-void


.end method

.method public static showToast(Landroid/content/Context;Ljava/lang/String;)V
    .param p0    # Landroid/content/Context;
    .param p1    # Ljava/lang/String;
    .locals 4


    .line 39
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/Toast;->show()V

    .line 40
    const-string/jumbo v0, "ToastUtil"

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string/jumbo v2, "showToast 123"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string/jumbo v2, " "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-wide/16 v2, 0x3L

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string/jumbo v2, " --->"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    sget v2, Lcom/baidu/titan/sample/ToastUtil$fdh;->b:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 43
    return-void


.end method
