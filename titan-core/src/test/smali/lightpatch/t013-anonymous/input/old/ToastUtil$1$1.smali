.class Lcom/baidu/titan/sample/ToastUtil$1$1;
.super Ljava/lang/Object;

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/baidu/titan/sample/ToastUtil$1;->run()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/baidu/titan/sample/ToastUtil$1;

.field final synthetic val$textFinal2:Ljava/lang/String;


# direct methods
.method constructor <init>(Lcom/baidu/titan/sample/ToastUtil$1;Ljava/lang/String;)V
    .locals 0

    iput-object p1, p0, Lcom/baidu/titan/sample/ToastUtil$1$1;->this$0:Lcom/baidu/titan/sample/ToastUtil$1;

    iput-object p2, p0, Lcom/baidu/titan/sample/ToastUtil$1$1;->val$textFinal2:Ljava/lang/String;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 2

    const-string v0, "ToastUtil"

    iget-object v1, p0, Lcom/baidu/titan/sample/ToastUtil$1$1;->val$textFinal2:Ljava/lang/String;

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-void
.end method
