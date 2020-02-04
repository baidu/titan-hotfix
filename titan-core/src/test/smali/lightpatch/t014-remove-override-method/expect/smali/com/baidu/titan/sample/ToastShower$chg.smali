.class public Lcom/baidu/titan/sample/ToastShower$chg;
.super Ljava/lang/Object;


# direct methods

.method public static showToast(Lcom/baidu/titan/sample/ToastShower;Landroid/content/Context;)V
    .param p0    # Lcom/baidu/titan/sample/ToastShower;
    .param p1    # Landroid/content/Context;
    .locals 2


    new-instance v0, Lcom/baidu/titan/sample/ToastUtil$copy;

    const/4 v1, 0x0

    invoke-direct {v0, p1, v1}, Lcom/baidu/titan/sample/ToastUtil$copy;-><init>(Landroid/content/Context;I)V

    invoke-virtual {v0}, Lcom/baidu/titan/sample/ToastUtil$copy;->getName()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/baidu/titan/sample/ToastUtil$copy;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    return-void


.end method
