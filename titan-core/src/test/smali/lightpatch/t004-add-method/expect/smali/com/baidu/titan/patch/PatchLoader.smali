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
    .locals 1


    new-instance v0, Lcom/baidu/titan/patch/ClassClinitInterceptor;

    invoke-direct {v0}, Lcom/baidu/titan/patch/ClassClinitInterceptor;-><init>()V

    sput-object v0, Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptorStorage;->$ic:Lcom/baidu/titan/sdk/runtime/ClassClinitInterceptable;

    return-void


.end method

.method public applyInTime()V
    .locals 0


    return-void


.end method
