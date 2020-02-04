.class public Lcom/baidu/titan/sample/AddMethodSmali$copy;
.super Ljava/lang/Object;


# direct methods

.method public constructor <init>(I)V
    .param p1    # I
    .locals 0


    .line 29
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 31
    return-void


.end method

.method public constructor <init>(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 33
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 35
    return-void


.end method

.method private newMethodLL(Ljava/lang/String;)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 55
    return-object p1


.end method

.method private newMethodVL()Ljava/lang/String;
    .locals 1


    .line 43
    const-string v0, "s"

    return-object v0


.end method

.method private newMethodVL(Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .locals 0


    .line 52
    return-void


.end method

.method private newMethodVL(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 48
    return-void


.end method

.method private newMethodVV()V
    .locals 0


    .line 40
    return-void


.end method

.method private static newStaticMethodLL(Ljava/lang/String;)Ljava/lang/String;
    .param p0    # Ljava/lang/String;
    .locals 0


    .line 76
    return-object p0


.end method

.method private static newStaticMethodVL()Ljava/lang/String;
    .locals 1


    .line 64
    const-string v0, "s"

    return-object v0


.end method

.method private static newStaticMethodVL(Ljava/lang/Object;)V
    .param p0    # Ljava/lang/Object;
    .locals 0


    .line 73
    return-void


.end method

.method private static newStaticMethodVL(Ljava/lang/String;)V
    .param p0    # Ljava/lang/String;
    .locals 0


    .line 69
    return-void


.end method

.method private static newStaticMethodVV()V
    .locals 0


    .line 61
    return-void


.end method

.method private oldMethodLL(Ljava/lang/String;)Ljava/lang/String;
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 23
    return-object p1


.end method

.method private oldMethodVL()Ljava/lang/String;
    .locals 1


    .line 11
    const-string v0, "s"

    return-object v0


.end method

.method private oldMethodVL(Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .locals 0


    .line 20
    return-void


.end method

.method private oldMethodVL(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 16
    return-void


.end method

.method private oldMethodVV()V
    .locals 0


    .line 8
    return-void


.end method
