.class public Lcom/baidu/titan/sample/AddFieldSmali$fdh;
.super Lcom/baidu/titan/sdk/runtime/FieldHolder;


# static fields
.field public static newStaticL:Ljava/lang/String;

.field public static newStaticI:I

.field public static newInitStaticL:Ljava/lang/String;

.field public static newStaticFinalI:I = 1

.field public static newStaticFinalL:Ljava/lang/String; = "s"

.field public static newInitStaticI:I = 0


# instance fields

.field public newI:I

.field public newInitI:I

.field public newFinalI:I

.field public newInitS:Ljava/lang/String;

.field public newFinalL:Ljava/lang/String;

.field public newS:Ljava/lang/String;


# direct methods

.method public static constructor <clinit>()V
    .locals 1


    :try_start_0
    const-string v0, "com.baidu.titan.sample.AddFieldSmali"

    invoke-static {v0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    :try_end_0
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_0} :catch_0
    goto :goto_0

    :catch_0
    move-exception v0

    invoke-virtual {v0}, Ljava/lang/ClassNotFoundException;->printStackTrace()V

    :goto_0
    return-void


.end method

.method public constructor <init>()V
    .locals 0


    invoke-direct {p0}, Lcom/baidu/titan/sdk/runtime/FieldHolder;-><init>()V

    return-void


.end method

.method public static getOrCreateFieldHolder(Lcom/baidu/titan/sample/AddFieldSmali;)Lcom/baidu/titan/sample/AddFieldSmali$fdh;
    .param p0    # Lcom/baidu/titan/sample/AddFieldSmali;
    .locals 1


    iget-object v0, p0, Lcom/baidu/titan/sample/AddFieldSmali;->$fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;

    if-eqz v0, :cond_0

    :goto_0
    check-cast v0, Lcom/baidu/titan/sample/AddFieldSmali$fdh;

    return-object v0

    :cond_0
    monitor-enter p0

    :try_start_0
    iget-object v0, p0, Lcom/baidu/titan/sample/AddFieldSmali;->$fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;

    if-nez v0, :cond_1

    new-instance v0, Lcom/baidu/titan/sample/AddFieldSmali$fdh;

    invoke-direct {v0}, Lcom/baidu/titan/sample/AddFieldSmali$fdh;-><init>()V

    iput-object v0, p0, Lcom/baidu/titan/sample/AddFieldSmali;->$fh:Lcom/baidu/titan/sdk/runtime/FieldHolder;

    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0
    :cond_1
    monitor-exit p0

    goto :goto_0

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0


.end method
