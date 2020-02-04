.class public Lcom/baidu/titan/sample/ToastShower;
.super Ljava/lang/Object;


# direct methods
.method public constructor <init>()V
    .locals 0

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public showToast(Landroid/content/Context;)V
    .locals 2

    new-instance v0, Lcom/baidu/titan/sample/ToastUtil;

    const/4 v1, 0x0

    invoke-direct {v0, p1, v1}, Lcom/baidu/titan/sample/ToastUtil;-><init>(Landroid/content/Context;I)V

    invoke-virtual {v0}, Lcom/baidu/titan/sample/ToastUtil;->getName()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/baidu/titan/sample/ToastUtil;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    return-void
.end method
