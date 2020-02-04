.class public Lcom/baidu/titan/sample/MainActivity$1$chg;
.super Ljava/lang/Object;


# direct methods

.method public static onClick(Lcom/baidu/titan/sample/MainActivity$1;Landroid/view/View;)V
    .param p0    # Lcom/baidu/titan/sample/MainActivity$1;
    .param p1    # Landroid/view/View;
    .locals 2


    const-string v0, "Titan.Sample"

    const-string v1, "toast_lib_text"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    new-instance v0, Ljava/lang/Thread;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$4$1$copy;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$4$1$copy;-><init>(Lcom/baidu/titan/sample/MainActivity$1;)V

    invoke-direct {v0, v1}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    invoke-virtual {v0}, Ljava/lang/Thread;->start()V

    return-void


.end method
