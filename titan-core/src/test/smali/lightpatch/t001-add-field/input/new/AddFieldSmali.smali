.class public Lcom/baidu/titan/sample/AddFieldSmali;
.super Ljava/lang/Object;
.source "AddFieldSmali.java"

.field private static newInitStaticI:I = 0

.field private static newInitStaticL:Ljava/lang/String;

.field private final static newStaticFinalI:I = 1

.field private final static newStaticFinalL:Ljava/lang/String; = "s"

.field private static newStaticI:I

.field private static newStaticL:Ljava/lang/String;

.field private final newFinalI:I

.field private final newFinalL:Ljava/lang/String;

.field private newI:I

.field private newInitI:I

.field private newInitS:Ljava/lang/String;

.field private newS:Ljava/lang/String;

.field public oldFieldI:I

.method static constructor <clinit>()V
    .registers 1
    .prologue
    .line 9
    const/4 v0, 1
    sput v0, Lcom/baidu/titan/sample/AddFieldSmali;->newInitStaticI:I
    .line 16
    const-string v0, "s"
    sput-object v0, Lcom/baidu/titan/sample/AddFieldSmali;->newInitStaticL:Ljava/lang/String;
    return-void
.end method

.method public constructor <init>()V
    .registers 2
    .prologue
    const/4 v0, 1
    .line 3
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    .line 7
    iput v0, p0, Lcom/baidu/titan/sample/AddFieldSmali;->newInitI:I
    .line 10
    iput v0, p0, Lcom/baidu/titan/sample/AddFieldSmali;->newFinalI:I
    .line 14
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/AddFieldSmali;->newInitS:Ljava/lang/String;
    .line 17
    const-string v0, "s"
    iput-object v0, p0, Lcom/baidu/titan/sample/AddFieldSmali;->newFinalL:Ljava/lang/String;
    return-void
.end method
