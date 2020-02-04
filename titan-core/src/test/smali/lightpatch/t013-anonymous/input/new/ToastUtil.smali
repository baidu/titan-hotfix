.class public Lcom/baidu/titan/sample/ToastUtil;
.super Lcom/baidu/titan/sample/ToastDad;


# static fields
.field public static final A:I = 0x4

.field public static b:I

.field public static debug:Z

.field public static final startTime:J


# instance fields
.field private mContext:Landroid/content/Context;

.field private tag:Ljava/lang/String;

.field public final toastStr:Ljava/lang/String;

.field public updateTime:J


# direct methods
.method static constructor <clinit>()V
    .locals 2

    const/16 v0, 0x29a

    sput v0, Lcom/baidu/titan/sample/ToastUtil;->b:I

    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v0

    sput-wide v0, Lcom/baidu/titan/sample/ToastUtil;->startTime:J

    const/4 v0, 0x1

    sput-boolean v0, Lcom/baidu/titan/sample/ToastUtil;->debug:Z

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;I)V
    .locals 2

    add-int/lit8 v0, p2, 0x1

    invoke-direct {p0, v0}, Lcom/baidu/titan/sample/ToastDad;-><init>(I)V

    const-string v0, "hello"

    iput-object v0, p0, Lcom/baidu/titan/sample/ToastUtil;->tag:Ljava/lang/String;

    iput-object p1, p0, Lcom/baidu/titan/sample/ToastUtil;->mContext:Landroid/content/Context;

    const-string v0, "test"

    iput-object v0, p0, Lcom/baidu/titan/sample/ToastUtil;->toastStr:Ljava/lang/String;

    const-string v0, "ToastUtil"

    const-string v1, "what\'s up guy "

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-void
.end method

.method public static showToast(Landroid/content/Context;Ljava/lang/String;)V
    .locals 4

    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/Toast;->show()V

    const-string v0, "ToastUtil"

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "showToast "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    sget-wide v2, Lcom/baidu/titan/sample/ToastUtil;->startTime:J

    invoke-virtual {v1, v2, v3}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " --->"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    sget v2, Lcom/baidu/titan/sample/ToastUtil;->b:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    new-instance v0, Ljava/lang/Thread;

    new-instance v1, Lcom/baidu/titan/sample/ToastUtil$1;

    invoke-direct {v1, p1}, Lcom/baidu/titan/sample/ToastUtil$1;-><init>(Ljava/lang/String;)V

    invoke-direct {v0, v1}, Ljava/lang/Thread;-><init>(Ljava/lang/Runnable;)V

    invoke-virtual {v0}, Ljava/lang/Thread;->start()V

    return-void
.end method


# virtual methods
.method public getName()Ljava/lang/String;
    .locals 1

    const-string v0, "ToastUtil"

    return-object v0
.end method

.method public logInt(I)V
    .locals 2

    sparse-switch p1, :sswitch_data_0

    const-string v0, "ToastUtil"

    invoke-static {p1}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :goto_0
    return-void

    :sswitch_0
    const-string v0, "ToastUtil"

    const-string v1, "100"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    :sswitch_1
    const-string v0, "ToastUtil"

    const-string v1, "200"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    nop

    :sswitch_data_0
    .sparse-switch
        0x64 -> :sswitch_0
        0xc8 -> :sswitch_1
    .end sparse-switch
.end method

.method public setUpdateTime(J)V
    .locals 1

    iput-wide p1, p0, Lcom/baidu/titan/sample/ToastUtil;->updateTime:J

    return-void
.end method
