.class public Lcom/baidu/titan/sample/ChangeField;
.super Ljava/lang/Object;
.source "ChangeField.java"


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

    .line 7
    :cond_0
    const/4 v0, 0x1

    sput v0, Lcom/baidu/titan/sample/ChangeField;->initStaticI:I

    .line 14
    const-string v0, "s"

    sput-object v0, Lcom/baidu/titan/sample/ChangeField;->initStaticL:Ljava/lang/String;

    return-void 

    :cond_1
    const v1, 0x9ba775e5

    const-string v2, "Lcom/baidu/titan/sample/ChangeField;"

    invoke-interface {v0, v1, v2}, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;->invokeClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v4

    if-eqz v4, :cond_0

    iget-object v3, v4, Lcom/baidu/titan/sdk/runtime/InterceptResult;->interceptor:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-eqz v3, :cond_2

    sput-object v3, Lcom/baidu/titan/sample/ChangeField;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    :cond_2
    iget v5, v4, Lcom/baidu/titan/sdk/runtime/InterceptResult;->flags:I

    and-int/lit8 v5, v5, 0x1

    if-eqz v5, :cond_0

    invoke-interface {v0, v1, v2}, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;->invokePostClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    return-void 


.end method

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/ChangeField;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    .line 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 5
    iput v0, p0, Lcom/baidu/titan/sample/ChangeField;->initI:I

    .line 8
    iput v0, p0, Lcom/baidu/titan/sample/ChangeField;->finalI:I

    .line 12
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/ChangeField;->initS:Ljava/lang/String;

    .line 15
    const-string v0, "s"

    iput-object v0, p0, Lcom/baidu/titan/sample/ChangeField;->finalL:Ljava/lang/String;

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
