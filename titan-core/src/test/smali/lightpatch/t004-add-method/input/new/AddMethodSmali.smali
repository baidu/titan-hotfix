.class public Lcom/baidu/titan/sample/AddMethodSmali;
.super Ljava/lang/Object;
.source "AddMethodSmali.java"

.method public constructor <init>(I)V
    .registers 2
    .prologue
    .line 29
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 31
    return-void
.end method

.method public constructor <init>(Ljava/lang/String;)V
    .registers 2
    .prologue
    .line 33
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 35
    return-void
.end method

.method private newMethodLL(Ljava/lang/String;)Ljava/lang/String;
    .registers 2
    .prologue
    .line 55
    return-object p1
.end method

.method private newMethodVL()Ljava/lang/String;
    .registers 2
    .prologue
    .line 43
    const-string v0, "s"
    return-object v0
.end method

.method private newMethodVL(Ljava/lang/Object;)V
    .registers 2
    .prologue
    .line 52
    return-void
.end method

.method private newMethodVL(Ljava/lang/String;)V
    .registers 2
    .prologue
    .line 48
    return-void
.end method

.method private newMethodVV()V
    .registers 1
    .prologue
    .line 40
    return-void
.end method

.method private static newStaticMethodLL(Ljava/lang/String;)Ljava/lang/String;
    .registers 1
    .prologue
    .line 76
    return-object p0
.end method

.method private static newStaticMethodVL()Ljava/lang/String;
    .registers 1
    .prologue
    .line 64
    const-string v0, "s"
    return-object v0
.end method

.method private static newStaticMethodVL(Ljava/lang/Object;)V
    .registers 1
    .prologue
    .line 73
    return-void
.end method

.method private static newStaticMethodVL(Ljava/lang/String;)V
    .registers 1
    .prologue
    .line 69
    return-void
.end method

.method private static newStaticMethodVV()V
    .registers 0
    .prologue
    .line 61
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
