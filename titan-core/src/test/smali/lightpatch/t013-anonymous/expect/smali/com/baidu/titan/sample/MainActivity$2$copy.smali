.class public Lcom/baidu/titan/sample/MainActivity$2$copy;
.super Ljava/lang/Object;

#interfaces
.implements Ljava/lang/Runnable;

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
    .param p1    # Lcom/baidu/titan/sample/MainActivity;
    .locals 0


    iput-object p1, p0, Lcom/baidu/titan/sample/MainActivity$2$copy;->this$0:Lcom/baidu/titan/sample/MainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void


.end method


# virtual methods

.method public run()V
    .locals 2


    const-string v0, "Titan.Sample"

    const-string v1, "thread"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-void


.end method
