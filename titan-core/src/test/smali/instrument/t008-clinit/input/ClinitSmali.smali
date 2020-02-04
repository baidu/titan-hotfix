.class public Lcom/baidu/titan/sample/ClinitSmali;
.super Ljava/lang/Object;
.source "ClinitSmali.java"


# static fields
.field public static a:I


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 4
    const/4 v0, 0x1

    sput v0, Lcom/baidu/titan/sample/ClinitSmali;->a:I

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
