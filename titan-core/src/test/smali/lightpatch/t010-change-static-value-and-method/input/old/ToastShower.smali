.class public Lcom/baidu/titan/sample/ToastShower;
.super Ljava/lang/Object;
.source "ToastShower.java"


# direct methods
.method public constructor <init>()V
    .registers 1

    .prologue
    .line 5
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public showToast(Landroid/content/Context;)V
    .registers 4
    .param p1, "context"    # Landroid/content/Context;

    .prologue
    .line 7
    new-instance v0, Lcom/baidu/titan/sample/ToastUtil;

    const/4 v1, 0x0

    invoke-direct {v0, p1, v1}, Lcom/baidu/titan/sample/ToastUtil;-><init>(Landroid/content/Context;I)V

    .line 8
    .local v0, "toastUtil":Lcom/baidu/titan/sample/ToastUtil;
    iget-object v1, v0, Lcom/baidu/titan/sample/ToastUtil;->toastStr:Ljava/lang/String;

    invoke-static {p1, v1}, Lcom/baidu/titan/sample/ToastUtil;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    .line 9
    return-void
.end method
