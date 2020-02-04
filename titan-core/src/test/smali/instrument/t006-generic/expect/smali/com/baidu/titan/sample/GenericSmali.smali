.class public Lcom/baidu/titan/sample/GenericSmali;
.super Ljava/lang/Object;
.source "GenericSmali.java"

# annotations

.annotation system Ldalvik/annotation/Signature;
    value = {
        "<K:",
        "Ljava/lang/Object;",
        "V:",
        "Ljava/lang/Object;",
        ">",
        "Ljava/lang/Object;"
    }
.end annotation


# static fields
.field public static synthetic $ic:Lcom/baidu/titan/sdk/runtime/Interceptable;


# instance fields

.field public transient synthetic $fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;


# direct methods

.method public constructor <init>()V
    .locals 5


    sget-object v0, Lcom/baidu/titan/sample/GenericSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 6
    :cond_0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void

    :cond_1
    invoke-static {}, Lcom/baidu/titan/sdk/runtime/TitanRuntime;->newInitContext()Lcom/baidu/titan/sdk/runtime/InitContext;

    move-result-object v1

    const/high16 v2, 0x10000

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

.method public static min(Ljava/util/Collection;Ljava/util/Comparator;)Ljava/lang/Object;
    .param p0    # Ljava/util/Collection;
    .param p1    # Ljava/util/Comparator;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<T:",
            "Ljava/lang/Object;",
            ">(",
            "Ljava/util/Collection",
            "<+TT;>;",
            "Ljava/util/Comparator",
            "<-TT;>;)TT;"
        }
    .end annotation
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/GenericSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_3

    :cond_0
    const/4 v0, 0x0

    .line 17
    if-nez p1, :cond_2

    .line 25
    :cond_1
    :goto_0
    return-object v0

    .line 21
    :cond_2
    if-eqz p0, :cond_1

    .line 25
    invoke-interface {p0}, Ljava/util/Collection;->iterator()Ljava/util/Iterator;

    move-result-object v0

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    goto :goto_0

    :cond_3
    move-object v1, v0

    const v2, 0x10001

    const/16 v3, 0x0

    # register: v1, v2, v3, p0, p1
    invoke-interface/range {v1 .. p1}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    iget-object v1, v0, Lcom/baidu/titan/sdk/runtime/InterceptResult;->objValue:Ljava/lang/Object;

    return-object v1


.end method


# virtual methods

.method public add(Ljava/lang/Object;Ljava/lang/Object;)V
    .param p1    # Ljava/lang/Object;
    .param p2    # Ljava/lang/Object;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(TK;TV;)V"
        }
    .end annotation
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/GenericSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 13
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const/high16 v3, 0x100000

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLL(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method

.method public find(Ljava/lang/Object;I)V
    .param p1    # Ljava/lang/Object;
    .param p2    # I
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<T:",
            "Ljava/lang/Object;",
            ">(TT;I)V"
        }
    .end annotation
    .locals 4


    sget-object v0, Lcom/baidu/titan/sample/GenericSmali;->$ic:Lcom/baidu/titan/sdk/runtime/Interceptable;

    if-nez v0, :cond_1

    .line 9
    :cond_0
    return-void

    :cond_1
    move-object v2, v0

    const v3, 0x100001

    # register: v2, v3, p0, p1, p2
    invoke-interface/range {v2 .. p2}, Lcom/baidu/titan/sdk/runtime/Interceptable;->invokeLI(ILjava/lang/Object;Ljava/lang/Object;I)Lcom/baidu/titan/sdk/runtime/InterceptResult;

    move-result-object v0

    if-eqz v0, :cond_0

    return-void


.end method
