.class public Lcom/baidu/titan/patch/ClassClinitInterceptor;
.super Lcom/baidu/titan/sdk/runtime/SimpleClassClinitInterceptor;


# direct methods

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/SimpleClassClinitInterceptor;-><init>()V

    return-void


.end method


# virtual methods

.method public invokeClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/String;
    .locals 1


    const v0, 0x0

    return-object v0


.end method

.method public invokePostClinit(ILjava/lang/String;)Lcom/baidu/titan/sdk/runtime/InterceptResult;
    .param p1    # I
    .param p2    # Ljava/lang/String;
    .locals 1


    const v0, 0x0

    return-object v0


.end method
