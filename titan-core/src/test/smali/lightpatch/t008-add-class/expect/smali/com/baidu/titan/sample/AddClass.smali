.class public Lcom/baidu/titan/sample/AddClass;
.super Ljava/lang/Object;


# static fields
.field private static initStaticI:I = 0

.field private static initStaticL:Ljava/lang/String;

.field private static final staticFinalI:I = 1

.field private static final staticFinalL:Ljava/lang/String; = "s"

.field private static staticI:I

.field private static staticL:Ljava/lang/String;


# instance fields

.field private final finalI:I

.field private final finalL:Ljava/lang/String;

.field private i:I

.field private initI:I

.field private initS:Ljava/lang/String;

.field private s:Ljava/lang/String;


# direct methods

.method static constructor <clinit>()V
    .locals 1


    .line 8
    const/4 v0, 0x1

    sput v0, Lcom/baidu/titan/sample/AddClass;->initStaticI:I

    .line 15
    const-string v0, "s"

    sput-object v0, Lcom/baidu/titan/sample/AddClass;->initStaticL:Ljava/lang/String;

    return-void


.end method

.method public constructor <init>()V
    .locals 1


    const/4 v0, 0x1

    .line 20
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 22
    return-void


.end method

.method protected constructor <init>(B)V
    .param p1    # B
    .locals 1


    const/4 v0, 0x1

    .line 24
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 26
    return-void


.end method

.method protected constructor <init>(C)V
    .param p1    # C
    .locals 1


    const/4 v0, 0x1

    .line 48
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 50
    return-void


.end method

.method protected constructor <init>(D)V
    .param p1    # D
    .locals 1


    const/4 v0, 0x1

    .line 44
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 46
    return-void


.end method

.method protected constructor <init>(F)V
    .param p1    # F
    .locals 1


    const/4 v0, 0x1

    .line 40
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 42
    return-void


.end method

.method protected constructor <init>(I)V
    .param p1    # I
    .locals 1


    const/4 v0, 0x1

    .line 32
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 34
    return-void


.end method

.method constructor <init>(II)V
    .param p1    # I
    .param p2    # I
    .locals 1


    const/4 v0, 0x1

    .line 56
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 58
    return-void


.end method

.method private constructor <init>(IILjava/lang/String;)V
    .param p1    # I
    .param p2    # I
    .param p3    # Ljava/lang/String;
    .locals 1


    const/4 v0, 0x1

    .line 60
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 62
    return-void


.end method

.method protected constructor <init>(J)V
    .param p1    # J
    .locals 1


    const/4 v0, 0x1

    .line 36
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 38
    return-void


.end method

.method protected constructor <init>(S)V
    .param p1    # S
    .locals 1


    const/4 v0, 0x1

    .line 28
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 30
    return-void


.end method

.method protected constructor <init>(Z)V
    .param p1    # Z
    .locals 1


    const/4 v0, 0x1

    .line 52
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 6
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->initI:I

    .line 9
    iput v0, p0, Lcom/baidu/titan/sample/AddClass;->finalI:I

    .line 13
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->initS:Ljava/lang/String;

    .line 16
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/AddClass;->finalL:Ljava/lang/String;

    .line 54
    return-void


.end method

.method static invokeStatic()V
    .locals 0


    .line 116
    return-void


.end method

.method static invokeStatic(B)V
    .param p0    # B
    .locals 0


    .line 120
    return-void


.end method

.method static invokeStatic(C)V
    .param p0    # C
    .locals 0


    .line 144
    return-void


.end method

.method static invokeStatic(D)V
    .param p0    # D
    .locals 0


    .line 140
    return-void


.end method

.method static invokeStatic(F)V
    .param p0    # F
    .locals 0


    .line 136
    return-void


.end method

.method static invokeStatic(I)V
    .param p0    # I
    .locals 0


    .line 128
    return-void


.end method

.method static invokeStatic(ILjava/lang/String;)V
    .param p0    # I
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 156
    return-void


.end method

.method static invokeStatic(J)V
    .param p0    # J
    .locals 0


    .line 132
    return-void


.end method

.method static invokeStatic(Ljava/lang/String;)V
    .param p0    # Ljava/lang/String;
    .locals 0


    .line 152
    return-void


.end method

.method static invokeStatic(Ljava/lang/String;I)V
    .param p0    # Ljava/lang/String;
    .param p1    # I
    .locals 0


    .line 160
    return-void


.end method

.method static invokeStatic(S)V
    .param p0    # S
    .locals 0


    .line 124
    return-void


.end method

.method static invokeStatic(Z)V
    .param p0    # Z
    .locals 0


    .line 148
    return-void


.end method


# virtual methods

.method invoke()V
    .locals 0


    .line 67
    return-void


.end method

.method invoke(B)V
    .param p1    # B
    .locals 0


    .line 71
    return-void


.end method

.method invoke(C)V
    .param p1    # C
    .locals 0


    .line 95
    return-void


.end method

.method invoke(D)V
    .param p1    # D
    .locals 0


    .line 91
    return-void


.end method

.method invoke(F)V
    .param p1    # F
    .locals 0


    .line 87
    return-void


.end method

.method invoke(I)V
    .param p1    # I
    .locals 0


    .line 79
    return-void


.end method

.method invoke(ILjava/lang/String;)V
    .param p1    # I
    .param p2    # Ljava/lang/String;
    .locals 0


    .line 107
    return-void


.end method

.method invoke(J)V
    .param p1    # J
    .locals 0


    .line 83
    return-void


.end method

.method invoke(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 103
    return-void


.end method

.method invoke(Ljava/lang/String;I)V
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 0


    .line 111
    return-void


.end method

.method invoke(S)V
    .param p1    # S
    .locals 0


    .line 75
    return-void


.end method

.method invoke(Z)V
    .param p1    # Z
    .locals 0


    .line 99
    return-void


.end method
