.class public Lcom/baidu/titan/sample/UnchangeClass;
.super Ljava/lang/Object;
.source "UnchangeClass.java"


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

.field public static initStaticI:I = 0

.field public static initStaticL:Ljava/lang/String;

.field public static final staticFinalI:I = 1

.field public static final staticFinalL:Ljava/lang/String; = "s"

.field public static staticI:I

.field public static staticL:Ljava/lang/String;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;

.field public final finalI:I

.field public final finalL:Ljava/lang/String;

.field public i:I

.field public initI:I

.field public initS:Ljava/lang/String;

.field public s:Ljava/lang/String;


# direct methods

.method public static constructor <clinit>()V
    .locals 6


    sget-object v0, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptorStorage;->$ic:Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;

    if-nez v0, :cond_1

    .line 8
    :cond_0
    const/4 v0, 0x1

    sput v0, Lcom/baidu/titan/sample/UnchangeClass;->initStaticI:I

    .line 15
    const-string v0, "s"

    sput-object v0, Lcom/baidu/titan/sample/UnchangeClass;->initStaticL:Ljava/lang/String;

    return-void 

    :cond_1
    const v1, 0x2d6ad940

    const-string v2, "Lcom/baidu/titan/sample/UnchangeClass;"

    invoke-interface {v0, v1, v2}, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;->invokeClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v4

    if-eqz v4, :cond_0

    iget-object v3, v4, Lcom/baidu/titan/sdk/runtime/InterceptResult;->interceptor:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-eqz v3, :cond_2

    sput-object v3, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    :cond_2
    iget v5, v4, Lcom/baidu/titan/sdk/runtime/InterceptResult;->flags:I

    and-int/lit8 v5, v5, 0x1

    if-eqz v5, :cond_0

    invoke-interface {v0, v1, v2}, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;->invokePostClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    return-void 


.end method

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 20
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x10001

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(B)V
    .param p1    # B
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 24
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Byte;->valueOf(B)Ljava/lang/Byte;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10002

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(C)V
    .param p1    # C
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 48
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Character;->valueOf(C)Ljava/lang/Character;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10003

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(D)V
    .param p1    # D
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 44
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10004

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(F)V
    .param p1    # F
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 40
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10005

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(I)V
    .param p1    # I
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 32
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10006

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(II)V
    .param p1    # I
    .param p2    # I
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 56
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x2

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    aput-object v4, v2, v3

    const/16 v3, 0x1

    invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10007

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method private constructor <init>(IILjava/lang/String;)V
    .param p1    # I
    .param p2    # I
    .param p3    # Ljava/lang/String;
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 60
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x3

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    aput-object v4, v2, v3

    const/16 v3, 0x1

    invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    aput-object v4, v2, v3

    const/16 v3, 0x2

    aput-object p3, v2, v3

    const v2, 0x10008

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(J)V
    .param p1    # J
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 36
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x10009

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(S)V
    .param p1    # S
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 28
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Short;->valueOf(S)Ljava/lang/Short;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x1000a

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public constructor <init>(Z)V
    .param p1    # Z
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 52
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

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

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const v2, 0x1

    new-array v2, v2, [Ljava/lang/Object;

    iput-object v2, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->initArgs:[Ljava/lang/Object;

    const/16 v3, 0x0

    invoke-static {p1}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;

    move-result-object v4

    aput-object v4, v2, v3

    const v2, 0x1000b

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeUnInit(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    iget v3, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->flag:I

    and-int/lit8 v4, v3, 0x1

    if-eqz v4, :cond_0

    and-int/lit8 v4, v3, 0x2

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p0, v1, Lcom/baidu/titan/sdk/runtime/InitContext;->thisArg:Ljava/lang/Object;

    invoke-interface {v0, v2, v1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeInitBody(ILcom/baidu/titan/sdk/runtime/InitContext;)V

    return-void 


.end method

.method public static invokeStatic()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 116
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x1000c

    const/16 v3, 0x0

    # register: v1, v2, v3
    invoke-interface/range {v1 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(B)V
    .param p0    # B
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 120
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x1000d

    const/16 v3, 0x0

    # register: v1, v2, v3, p0
    invoke-interface/range {v1 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeB(ILjava/lang/Object;B)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(C)V
    .param p0    # C
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 144
    :cond_0
    return-void 

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p0}, Ljava/lang/Character;->valueOf(C)Ljava/lang/Character;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x1000e

    const/4 v2, 0x0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(D)V
    .param p0    # D
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 140
    :cond_0
    return-void 

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p0}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x1000f

    const/4 v2, 0x0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(F)V
    .param p0    # F
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 136
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10010

    const/16 v3, 0x0

    # register: v1, v2, v3, p0
    invoke-interface/range {v1 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeF(ILjava/lang/Object;F)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(I)V
    .param p0    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 128
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10011

    const/16 v3, 0x0

    # register: v1, v2, v3, p0
    invoke-interface/range {v1 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeI(ILjava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(ILjava/lang/String;)V
    .param p0    # I
    .param p1    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 156
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10012

    const/16 v3, 0x0

    # register: v1, v2, v3, p0, p1
    invoke-interface/range {v1 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeIL(ILjava/lang/Object;ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(J)V
    .param p0    # J
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 132
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10013

    const/16 v3, 0x0

    # register: v1, v2, v3, p0
    invoke-interface/range {v1 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeJ(ILjava/lang/Object;J)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(Ljava/lang/String;)V
    .param p0    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 152
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10014

    const/16 v3, 0x0

    # register: v1, v2, v3, p0
    invoke-interface/range {v1 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(Ljava/lang/String;I)V
    .param p0    # Ljava/lang/String;
    .param p1    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 160
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10015

    const/16 v3, 0x0

    # register: v1, v2, v3, p0, p1
    invoke-interface/range {v1 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(S)V
    .param p0    # S
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 124
    :cond_0
    return-void 

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p0}, Ljava/lang/Short;->valueOf(S)Ljava/lang/Short;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x10016

    const/4 v2, 0x0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public static invokeStatic(Z)V
    .param p0    # Z
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 148
    :cond_0
    return-void 

    :cond_1
    move-object v1, v0

    const v2, 0x10017

    const/16 v3, 0x0

    # register: v1, v2, v3, p0
    invoke-interface/range {v1 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeZ(ILjava/lang/Object;Z)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method


# virtual methods

.method public invoke()V
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 67
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const/high16 v3, 0x100000

    # register: v2, v3, p0
    invoke-interface/range {v2 .. p0}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeV(ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(B)V
    .param p1    # B
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 71
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x100001

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeB(ILjava/lang/Object;B)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(C)V
    .param p1    # C
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 95
    :cond_0
    return-void 

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p1}, Ljava/lang/Character;->valueOf(C)Ljava/lang/Character;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x100002

    move-object v2, p0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(D)V
    .param p1    # D
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 91
    :cond_0
    return-void 

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p1}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x100003

    move-object v2, p0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(F)V
    .param p1    # F
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 87
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x100004

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeF(ILjava/lang/Object;F)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(I)V
    .param p1    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 79
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x100005

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeI(ILjava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(ILjava/lang/String;)V
    .param p1    # I
    .param p2    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 107
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x100006

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeIL(ILjava/lang/Object;ILjava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(J)V
    .param p1    # J
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 83
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x100007

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeJ(ILjava/lang/Object;J)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(Ljava/lang/String;)V
    .param p1    # Ljava/lang/String;
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 103
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x100008

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeL(ILjava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(Ljava/lang/String;I)V
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 111
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x100009

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(S)V
    .param p1    # S
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 75
    :cond_0
    return-void 

    :cond_1
    const/16 v3, 0x1

    new-array v3, v3, [Ljava/lang/Object;

    const/16 v1, 0x0

    invoke-static {p1}, Ljava/lang/Short;->valueOf(S)Ljava/lang/Short;

    move-result-object v2

    aput-object v2, v3, v1

    const v1, 0x10000a

    move-object v2, p0

    # register: v0, v1, v2, v3
    invoke-interface/range {v0 .. v3}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeCommon(ILjava/lang/Object;[Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method

.method public invoke(Z)V
    .param p1    # Z
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/UnchangeClass;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 99
    :cond_0
    return-void 

    :cond_1
    move-object v2, v0

    const v3, 0x10000b

    # register: v2, v3, p0, p1
    invoke-interface/range {v2 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeZ(ILjava/lang/Object;Z)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void 


.end method
