.class Lcom/baidu/titan/sample/MainActivity$4;
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

    iput-object p1, p0, Lcom/baidu/titan/sample/MainActivity$4;->this$0:Lcom/baidu/titan/sample/MainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 2

    const-string v0, "Titan.Sample"

    const-string v1, "toast_lib_text"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    new-instance v0, Ljava/lang/Thread;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$4$1;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$4$1;-><init>(Lcom/baidu/titan/sample/MainActivity$4;)V

    invoke-direct {v0, v1}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    invoke-virtual {v0}, Ljava/lang/Thread;->start()V

    return-void
.end method
