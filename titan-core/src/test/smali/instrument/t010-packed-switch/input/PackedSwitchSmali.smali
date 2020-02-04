.class public Lcom/baidu/titan/sample/PackedSwitchSmali;
.super Ljava/lang/Object;


# direct methods
.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public logInt(I)V
    .registers 4

    packed-switch p1, :pswitch_data_1e

    const-string v0, "ToastUtil"

    invoke-static {p1}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :goto_c
    return-void

    :pswitch_d
    const-string v0, "ToastUtil"

    const-string v1, "hit 2"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_c

    :pswitch_15
    const-string v0, "ToastUtil"

    const-string v1, "hit 3"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_c

    nop

    :pswitch_data_1e
    .packed-switch 0x2
        :pswitch_d
        :pswitch_15
    .end packed-switch
.end method