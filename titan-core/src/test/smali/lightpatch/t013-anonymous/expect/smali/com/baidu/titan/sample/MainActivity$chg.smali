.class public Lcom/baidu/titan/sample/MainActivity$chg;
.super Landroid/app/Activity;


# direct methods

.method public static onCreate(Lcom/baidu/titan/sample/MainActivity;Landroid/os/Bundle;)V
    .param p0    # Lcom/baidu/titan/sample/MainActivity;
    .param p1    # Landroid/os/Bundle;
    .locals 5


    const/4 v4, 0x0

    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    const/high16 v0, 0x7f040000

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->setContentView(I)V

    invoke-static {p0}, Lcom/baidu/titan/sample/MainActivity$chg;->requestPermission(Lcom/baidu/titan/sample/MainActivity;)V

    const/high16 v0, 0x7f080000

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    const v0, 0x7f080001

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$2;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$2;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    new-instance v0, Ljava/lang/Thread;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$2$copy;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$2$copy;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-direct {v0, v1}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    invoke-virtual {v0}, Ljava/lang/Thread;->start()V

    const v0, 0x7f080003

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$4;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$4;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const v0, 0x7f080002

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$1;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$1;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const-string v0, "ToastUtil"

    const-string v1, "MainActivity onCreate"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    new-instance v0, Lcom/baidu/titan/sample/ToastUtil$copy;

    invoke-direct {v0, p0, v4}, Lcom/baidu/titan/sample/ToastUtil$copy;-><init>(Landroid/content/Context;I)V

    new-instance v0, Lcom/baidu/titan/sample/Person;

    const-string v1, "perter"

    const/16 v2, 0x12

    const-string v3, "12837482738"

    invoke-direct {v0, v1, v4, v2, v3}, Lcom/baidu/titan/sample/Person;-><init>(Ljava/lang/String;IILjava/lang/String;)V

    invoke-virtual {v0}, Lcom/baidu/titan/sample/Person;->getBirthYear()Ljava/lang/String;

    move-result-object v0

    invoke-static {p0, v0}, Lcom/baidu/titan/sample/ToastUtil$copy;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    return-void


.end method

.method public static requestPermission(Lcom/baidu/titan/sample/MainActivity;)V
    .param p0    # Lcom/baidu/titan/sample/MainActivity;
    .locals 4


    const/4 v3, 0x1

    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x17

    if-lt v0, v1, :cond_0

    const-string v0, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->checkSelfPermission(Ljava/lang/String;)I

    move-result v0

    if-eqz v0, :cond_0

    new-array v0, v3, [Ljava/lang/String;

    const/4 v1, 0x0

    const-string v2, "android.permission.WRITE_EXTERNAL_STORAGE"

    aput-object v2, v0, v1

    invoke-virtual {p0, v0, v3}, Lcom/baidu/titan/sample/MainActivity;->requestPermissions([Ljava/lang/String;I)V

    :cond_0
    return-void


.end method
