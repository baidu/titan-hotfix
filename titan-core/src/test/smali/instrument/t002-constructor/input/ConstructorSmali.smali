.class public Lcom/baidu/titan/sample/ConstructorSmali;
.super Ljava/lang/Object;
.source "ConstructorSmali.java"

.field private a:I

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 6
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 8
    return-void
.end method

.method protected constructor <init>(B)V
    .registers 2
    .prologue
    .line 10
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 11
    iput p1, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 12
    return-void
.end method

.method protected constructor <init>(C)V
    .registers 2
    .prologue
    .line 32
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 33
    iput p1, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 34
    return-void
.end method

.method protected constructor <init>(D)V
    .registers 4
    .prologue
    .line 28
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 29
    double-to-int v0, p1
    iput v0, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 30
    return-void
.end method

.method protected constructor <init>(F)V
    .registers 3
    .prologue
    .line 25
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 26
    float-to-int v0, p1
    iput v0, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 27
    return-void
.end method

.method protected constructor <init>(I)V
    .registers 2
    .prologue
    .line 18
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 19
    iput p1, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 20
    return-void
.end method

.method constructor <init>(II)V
    .registers 3
    .prologue
    .line 40
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 41
    iput p1, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 42
    return-void
.end method

.method private constructor <init>(IILjava/lang/String;)V
    .registers 4
    .prologue
    .line 44
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 45
    iput p1, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 46
    return-void
.end method

.method protected constructor <init>(J)V
    .registers 4
    .prologue
    .line 22
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 23
    long-to-int v0, p1
    iput v0, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 24
    return-void
.end method

.method protected constructor <init>(S)V
    .registers 2
    .prologue
    .line 14
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 15
    iput p1, p0, Lcom/baidu/titan/sample/ConstructorSmali;->a:I
    .line 16
    return-void
.end method

.method protected constructor <init>(Z)V
    .registers 2
    .prologue
    .line 36
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 38
    return-void
.end method
