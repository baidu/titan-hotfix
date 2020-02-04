.class public Lcom/baidu/titan/sample/ToastShower$chg;
.super Ljava/lang/Object;


# direct methods

.method public static showToast(Lcom/baidu/titan/sample/ToastShower;Landroid/content/Context;)V
    .param p0    # Lcom/baidu/titan/sample/ToastShower;
    .param p1    # Landroid/content/Context;
    .locals 2


    .line 7
    new-instance v0, Lcom/baidu/titan/sample/ToastUtil;

    const/4 v1, 0x0

    invoke-direct {v0, p1, v1}, Lcom/baidu/titan/sample/ToastUtil;-><init>(Landroid/content/Context;I)V

    .line 8
    invoke-static {v0}, Lcom/baidu/titan/sample/ToastUtil$fdh;->getOrCreateFieldHolder(Lcom/baidu/titan/sample/ToastUtil;)Lcom/baidu/titan/sample/ToastUtil$fdh;

    move-result-object v1

    iget-object v1, v1, Lcom/baidu/titan/sample/ToastUtil$fdh;->toastStr:Ljava/lang/String;

    invoke-static {p1, v1}, Lcom/baidu/titan/sample/ToastUtil;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    .line 9
    return-void


.end method
