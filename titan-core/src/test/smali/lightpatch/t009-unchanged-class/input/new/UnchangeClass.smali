.class public Lcom/baidu/titan/sample/UnchangeClass;
.super Ljava/lang/Object;
.source "UnchangeClass.java"

.field private static initStaticI:I = 0

.field private static initStaticL:Ljava/lang/String;

.field private final static staticFinalI:I = 1

.field private final static staticFinalL:Ljava/lang/String; = "s"

.field private static staticI:I

.field private static staticL:Ljava/lang/String;

.field private final finalI:I

.field private final finalL:Ljava/lang/String;

.field private i:I

.field private initI:I

.field private initS:Ljava/lang/String;

.field private s:Ljava/lang/String;

.method static constructor <clinit>()V
    .registers 1
    .prologue
    .line 8
    const/4 v0, 1
    sput v0, Lcom/baidu/titan/sample/UnchangeClass;->initStaticI:I
    .line 15
    const-string v0, "s"
    sput-object v0, Lcom/baidu/titan/sample/UnchangeClass;->initStaticL:Ljava/lang/String;
    return-void
.end method

.method public constructor <init>()V
    .registers 2
    .prologue
    const/4 v0, 1
    .line 20
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 22
    return-void
.end method

.method protected constructor <init>(B)V
    .registers 3
    .prologue
    const/4 v0, 1
    .line 24
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 26
    return-void
.end method

.method protected constructor <init>(C)V
    .registers 3
    .prologue
    const/4 v0, 1
    .line 48
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 50
    return-void
.end method

.method protected constructor <init>(D)V
    .registers 4
    .prologue
    const/4 v0, 1
    .line 44
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 46
    return-void
.end method

.method protected constructor <init>(F)V
    .registers 3
    .prologue
    const/4 v0, 1
    .line 40
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 42
    return-void
.end method

.method protected constructor <init>(I)V
    .registers 3
    .prologue
    const/4 v0, 1
    .line 32
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 34
    return-void
.end method

.method constructor <init>(II)V
    .registers 4
    .prologue
    const/4 v0, 1
    .line 56
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 58
    return-void
.end method

.method private constructor <init>(IILjava/lang/String;)V
    .registers 5
    .prologue
    const/4 v0, 1
    .line 60
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 62
    return-void
.end method

.method protected constructor <init>(J)V
    .registers 4
    .prologue
    const/4 v0, 1
    .line 36
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 38
    return-void
.end method

.method protected constructor <init>(S)V
    .registers 3
    .prologue
    const/4 v0, 1
    .line 28
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 30
    return-void
.end method

.method protected constructor <init>(Z)V
    .registers 3
    .prologue
    const/4 v0, 1
    .line 52
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initI:I
    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalI:I
    .line 13
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->initS:Ljava/lang/String;
    .line 16
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/UnchangeClass;->finalL:Ljava/lang/String;
    .line 54
    return-void
.end method

.method static invokeStatic()V
    .registers 0
    .prologue
    .line 116
    return-void
.end method

.method static invokeStatic(B)V
    .registers 1
    .prologue
    .line 120
    return-void
.end method

.method static invokeStatic(C)V
    .registers 1
    .prologue
    .line 144
    return-void
.end method

.method static invokeStatic(D)V
    .registers 2
    .prologue
    .line 140
    return-void
.end method

.method static invokeStatic(F)V
    .registers 1
    .prologue
    .line 136
    return-void
.end method

.method static invokeStatic(I)V
    .registers 1
    .prologue
    .line 128
    return-void
.end method

.method static invokeStatic(ILjava/lang/String;)V
    .registers 2
    .prologue
    .line 156
    return-void
.end method

.method static invokeStatic(J)V
    .registers 2
    .prologue
    .line 132
    return-void
.end method

.method static invokeStatic(Ljava/lang/String;)V
    .registers 1
    .prologue
    .line 152
    return-void
.end method

.method static invokeStatic(Ljava/lang/String;I)V
    .registers 2
    .prologue
    .line 160
    return-void
.end method

.method static invokeStatic(S)V
    .registers 1
    .prologue
    .line 124
    return-void
.end method

.method static invokeStatic(Z)V
    .registers 1
    .prologue
    .line 148
    return-void
.end method

.method invoke()V
    .registers 1
    .prologue
    .line 67
    return-void
.end method

.method invoke(B)V
    .registers 2
    .prologue
    .line 71
    return-void
.end method

.method invoke(C)V
    .registers 2
    .prologue
    .line 95
    return-void
.end method

.method invoke(D)V
    .registers 3
    .prologue
    .line 91
    return-void
.end method

.method invoke(F)V
    .registers 2
    .prologue
    .line 87
    return-void
.end method

.method invoke(I)V
    .registers 2
    .prologue
    .line 79
    return-void
.end method

.method invoke(ILjava/lang/String;)V
    .registers 3
    .prologue
    .line 107
    return-void
.end method

.method invoke(J)V
    .registers 3
    .prologue
    .line 83
    return-void
.end method

.method invoke(Ljava/lang/String;)V
    .registers 2
    .prologue
    .line 103
    return-void
.end method

.method invoke(Ljava/lang/String;I)V
    .registers 3
    .prologue
    .line 111
    return-void
.end method

.method invoke(S)V
    .registers 2
    .prologue
    .line 75
    return-void
.end method

.method invoke(Z)V
    .registers 2
    .prologue
    .line 99
    return-void
.end method
