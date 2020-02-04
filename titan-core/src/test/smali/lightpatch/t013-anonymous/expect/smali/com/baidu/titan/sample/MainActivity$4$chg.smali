.class public Lcom/baidu/titan/sample/MainActivity$4$chg;
.super Ljava/lang/Object;


# direct methods

.method public static onClick(Lcom/baidu/titan/sample/MainActivity$4;Landroid/view/View;)V
    .param p0    # Lcom/baidu/titan/sample/MainActivity$4;
    .param p1    # Landroid/view/View;
    .locals 3


    iget-object v0, p0, Lcom/baidu/titan/sample/MainActivity$4;->this$0:Lcom/baidu/titan/sample/MainActivity;

    iget-object v1, p0, Lcom/baidu/titan/sample/MainActivity$4;->this$0:Lcom/baidu/titan/sample/MainActivity;

    invoke-virtual {v1}, Lcom/baidu/titan/sample/MainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f060001

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/baidu/titan/sample/ToastUtil$copy;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    return-void


.end method
