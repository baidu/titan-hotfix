.class public Lcom/baidu/titan/sample/ToastUtil;
.super Lcom/baidu/titan/sample/ToastDad;
.source "ToastUtil.java"


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
    .registers 2

    .prologue
    .line 21
    const/16 v0, 0x29a

    sput v0, Lcom/baidu/titan/sample/ToastUtil;->b:I

    .line 23
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v0

    sput-wide v0, Lcom/baidu/titan/sample/ToastUtil;->startTime:J

    .line 25
    const/4 v0, 0x1

    sput-boolean v0, Lcom/baidu/titan/sample/ToastUtil;->debug:Z

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;I)V
    .registers 5
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "i"    # I

    .prologue
    .line 32
    add-int/lit8 v0, p2, 0x1

    invoke-direct {p0, v0}, Lcom/baidu/titan/sample/ToastDad;-><init>(I)V

    .line 15
    const-string/jumbo v0, "hello"

    iput-object v0, p0, Lcom/baidu/titan/sample/ToastUtil;->tag:Ljava/lang/String;

    .line 33
    iput-object p1, p0, Lcom/baidu/titan/sample/ToastUtil;->mContext:Landroid/content/Context;

    .line 34
    const-string/jumbo v0, "test"

    iput-object v0, p0, Lcom/baidu/titan/sample/ToastUtil;->toastStr:Ljava/lang/String;

    .line 35
    const-string/jumbo v0, "ToastUtil"

    const-string/jumbo v1, "what\'s up guy"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 36
    return-void
.end method

.method public static showToast(Landroid/content/Context;Ljava/lang/String;)V
    .registers 6
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "text"    # Ljava/lang/String;

    .prologue
    .line 40
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/Toast;->show()V

    .line 41
    const-string/jumbo v0, "ToastUtil"

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string/jumbo v2, "showToast "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string/jumbo v2, " "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    sget-wide v2, Lcom/baidu/titan/sample/ToastUtil;->startTime:J

    invoke-virtual {v1, v2, v3}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string/jumbo v2, " --->"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    sget v2, Lcom/baidu/titan/sample/ToastUtil;->b:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 42
    return-void
.end method


# virtual methods
.method public logInt(I)V
    .registers 4
    .param p1, "value"    # I

    .prologue
    .line 45
    sparse-switch p1, :sswitch_data_22

    .line 53
    const-string/jumbo v0, "ToastUtil"

    invoke-static {p1}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 56
    :goto_d
    return-void

    .line 47
    :sswitch_e
    const-string/jumbo v0, "ToastUtil"

    const-string/jumbo v1, "100"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_d

    .line 50
    :sswitch_18
    const-string/jumbo v0, "ToastUtil"

    const-string/jumbo v1, "200"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_d

    .line 45
    :sswitch_data_22
    .sparse-switch
        0x64 -> :sswitch_e
        0xc8 -> :sswitch_18
    .end sparse-switch
.end method

.method public setUpdateTime(J)V
    .registers 4
    .param p1, "updateTime"    # J

    .prologue
    .line 59
    iput-wide p1, p0, Lcom/baidu/titan/sample/ToastUtil;->updateTime:J

    .line 62
    return-void
.end method
