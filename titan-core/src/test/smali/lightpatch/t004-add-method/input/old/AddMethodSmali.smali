.class public Lcom/baidu/titan/sample/AddMethodSmali;
.super Ljava/lang/Object;
.source "AddMethodSmali.java"

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 3
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method private oldMethodLL(Ljava/lang/String;)Ljava/lang/String;
    .registers 2
    .prologue
    .line 23
    return-object p1
.end method

.method private oldMethodVL()Ljava/lang/String;
    .registers 2
    .prologue
    .line 11
    const-string v0, "s"
    return-object v0
.end method

.method private oldMethodVL(Ljava/lang/Object;)V
    .registers 2
    .prologue
    .line 20
    return-void
.end method

.method private oldMethodVL(Ljava/lang/String;)V
    .registers 2
    .prologue
    .line 16
    return-void
.end method

.method private oldMethodVV()V
    .registers 1
    .prologue
    .line 8
    return-void
.end method
