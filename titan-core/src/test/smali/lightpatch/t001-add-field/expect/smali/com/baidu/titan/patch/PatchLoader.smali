.class public Lcom/baidu/titan/patch/PatchLoader;
.super Lcom/baidu/titan/sdk/loader/BaseLoader;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/loader/BaseLoader;-><init>()V

    return-void


.end method


# virtual methods

.method public apply()V
    .locals 2


    new-instance v0, Lcom/baidu/titan/sample/AddFieldSmali$iter;

    invoke-direct {v0}, Lcom/baidu/titan/sample/AddFieldSmali$iter;-><init>()V

    sput-object v0, Lcom/baidu/titan/sample/AddFieldSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    new-instance v0, Lcom/baidu/titan/patch/ClassClinitInterceptor;

    invoke-direct {v0}, Lcom/baidu/titan/patch/ClassClinitInterceptor;-><init>()V

    sput-object v0, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptorStorage;->$ic:Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;

    const v1, 0x0

    new-array v0, v1, [Ljava/lang/Object;

    invoke-static {v0}, Lcom/baidu/titan/sample/AddFieldSmali$chg;->$staticInit([Ljava/lang/Object;)V

    return-void


.end method

.method public applyInTime()V
    .locals 1


    new-instance v0, Lcom/baidu/titan/sample/AddFieldSmali$iter;

    invoke-direct {v0}, Lcom/baidu/titan/sample/AddFieldSmali$iter;-><init>()V

    sput-object v0, Lcom/baidu/titan/sample/AddFieldSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    return-void


.end method
