.class public Lcom/baidu/titan/sample/NewArraySmali;
.super Ljava/lang/Object;


# instance fields
.field public numArray:[I


# direct methods
.method public constructor <init>()V
    .registers 2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x4

    new-array v0, v0, [I

    fill-array-data v0, :array_c

    iput-object v0, p0, Lcom/baidu/titan/sample/NewArraySmali;->numArray:[I

    return-void

    :array_c
    .array-data 4
        0x3
        0x5
        0x7
        0x9
    .end array-data
.end method