.class public Lcom/baidu/titan/sample/MainActivity$5$chg;
.super Ljava/lang/Object;


# direct methods

.method public static onPatchInstalled(Lcom/baidu/titan/sample/MainActivity$5;ILandroid/os/Bundle;)V
    .param p0    # Lcom/baidu/titan/sample/MainActivity$5;
    .param p1    # I
    .param p2    # Landroid/os/Bundle;
    .locals 3


    if-nez p1, :cond_1

    iget-object v0, p0, Lcom/baidu/titan/sample/MainActivity$5;->this$0:Lcom/baidu/titan/sample/MainActivity;

    const-string v1, "Patch Install Success"

    invoke-static {v0, v1}, Lcom/baidu/titan/sample/ToastUtil$copy;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    :cond_0
    :goto_0
    const-string v0, "Titan.Sample"

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "onPatchInstalled result code : "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    :cond_1
    const/4 v0, 0x1

    if-ne p1, v0, :cond_0

    iget-object v0, p0, Lcom/baidu/titan/sample/MainActivity$5;->this$0:Lcom/baidu/titan/sample/MainActivity;

    const-string v1, "Patch Install Already installed"

    invoke-static {v0, v1}, Lcom/baidu/titan/sample/ToastUtil$copy;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    goto :goto_0


.end method
