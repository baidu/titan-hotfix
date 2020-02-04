.class public Lcom/baidu/titan/sample/ChangeField;
.super Ljava/lang/Object;
.source "ChangeField.java"

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
    .line 7
    const/4 v0, 1
    sput v0, Lcom/baidu/titan/sample/ChangeField;->initStaticI:I
    .line 14
    const-string v0, "s"
    sput-object v0, Lcom/baidu/titan/sample/ChangeField;->initStaticL:Ljava/lang/String;
    return-void
.end method

.method public constructor <init>()V
    .registers 2
    .prologue
    const/4 v0, 1
    .line 3
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
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
.end method
