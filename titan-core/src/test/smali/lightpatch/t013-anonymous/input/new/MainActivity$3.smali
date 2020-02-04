.class Lcom/baidu/titan/sample/MainActivity$3;
.super Ljava/lang/Object;

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/baidu/titan/sample/MainActivity;->onCreate(Landroid/os/Bundle;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/baidu/titan/sample/MainActivity;


# direct methods
.method constructor <init>(Lcom/baidu/titan/sample/MainActivity;)V
    .locals 0

    iput-object p1, p0, Lcom/baidu/titan/sample/MainActivity$3;->this$0:Lcom/baidu/titan/sample/MainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 3

    iget-object v0, p0, Lcom/baidu/titan/sample/MainActivity$3;->this$0:Lcom/baidu/titan/sample/MainActivity;

    iget-object v1, p0, Lcom/baidu/titan/sample/MainActivity$3;->this$0:Lcom/baidu/titan/sample/MainActivity;

    invoke-virtual {v1}, Lcom/baidu/titan/sample/MainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f060001

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/baidu/titan/sample/ToastUtil;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    return-void
.end method
