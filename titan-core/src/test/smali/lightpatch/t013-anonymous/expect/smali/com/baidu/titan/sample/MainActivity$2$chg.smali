.class public Lcom/baidu/titan/sample/MainActivity$2$chg;
.super Ljava/lang/Object;


# direct methods

.method public static onClick(Lcom/baidu/titan/sample/MainActivity$2;Landroid/view/View;)V
    .param p0    # Lcom/baidu/titan/sample/MainActivity$2;
    .param p1    # Landroid/view/View;
    .locals 2


    iget-object v0, p0, Lcom/baidu/titan/sample/MainActivity$2;->this$0:Lcom/baidu/titan/sample/MainActivity;

    const-string v1, "This is Java. timestamp:11:25"

    invoke-static {v0, v1}, Lcom/baidu/titan/sample/ToastUtil$copy;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    return-void


.end method
