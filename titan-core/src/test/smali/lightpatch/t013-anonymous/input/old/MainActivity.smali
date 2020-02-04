.class public Lcom/baidu/titan/sample/MainActivity;
.super Landroid/app/Activity;


# static fields
.field private static final TAG:Ljava/lang/String; = "Titan.Sample"


# instance fields
.field private toastText:Ljava/lang/String;


# direct methods
.method public constructor <init>()V
    .locals 1

    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    const-string v0, "hello new field"

    iput-object v0, p0, Lcom/baidu/titan/sample/MainActivity;->toastText:Ljava/lang/String;

    return-void
.end method

.method static synthetic access$000(Lcom/baidu/titan/sample/MainActivity;)V
    .locals 0

    invoke-direct {p0}, Lcom/baidu/titan/sample/MainActivity;->doInstall()V

    return-void
.end method

.method private doInstall()V
    .locals 4

    new-instance v0, Ljava/io/File;

    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v1

    const-string v2, "titanpatch"

    invoke-direct {v0, v1, v2}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    new-instance v1, Ljava/io/File;

    const-string v2, "patch.apk"

    invoke-direct {v1, v0, v2}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    invoke-static {}, Lcom/baidu/titan/sdk/pm/PatchManager;->getInstance()Lcom/baidu/titan/sdk/pm/PatchManager;

    move-result-object v0

    invoke-static {v1}, Landroid/net/Uri;->fromFile(Ljava/io/File;)Landroid/net/Uri;

    move-result-object v1

    const/4 v2, 0x0

    new-instance v3, Lcom/baidu/titan/sample/MainActivity$5;

    invoke-direct {v3, p0}, Lcom/baidu/titan/sample/MainActivity$5;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1, v2, v3}, Lcom/baidu/titan/sdk/pm/PatchManager;->installPatch(Landroid/net/Uri;Landroid/os/Bundle;Lcom/baidu/titan/sdk/pm/PatchManager$PatchInstallObserver;)V

    return-void
.end method

.method private requestPermission()V
    .locals 4

    const/4 v3, 0x1

    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x17

    if-lt v0, v1, :cond_0

    const-string v0, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->checkSelfPermission(Ljava/lang/String;)I

    move-result v0

    if-eqz v0, :cond_0

    new-array v0, v3, [Ljava/lang/String;

    const/4 v1, 0x0

    const-string v2, "android.permission.WRITE_EXTERNAL_STORAGE"

    aput-object v2, v0, v1

    invoke-virtual {p0, v0, v3}, Lcom/baidu/titan/sample/MainActivity;->requestPermissions([Ljava/lang/String;I)V

    :cond_0
    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .locals 5

    const/4 v4, 0x0

    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    const/high16 v0, 0x7f040000

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->setContentView(I)V

    invoke-direct {p0}, Lcom/baidu/titan/sample/MainActivity;->requestPermission()V

    const/high16 v0, 0x7f080000

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$1;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$1;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const v0, 0x7f080001

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$2;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$2;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const v0, 0x7f080002

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$3;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$3;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const v0, 0x7f080003

    invoke-virtual {p0, v0}, Lcom/baidu/titan/sample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    new-instance v1, Lcom/baidu/titan/sample/MainActivity$4;

    invoke-direct {v1, p0}, Lcom/baidu/titan/sample/MainActivity$4;-><init>(Lcom/baidu/titan/sample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const-string v0, "ToastUtil"

    const-string v1, "MainActivity onCreate"

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    new-instance v0, Lcom/baidu/titan/sample/ToastUtil;

    invoke-direct {v0, p0, v4}, Lcom/baidu/titan/sample/ToastUtil;-><init>(Landroid/content/Context;I)V

    new-instance v0, Lcom/baidu/titan/sample/Person;

    const-string v1, "perter"

    const/16 v2, 0x12

    const-string v3, "12837482738"

    invoke-direct {v0, v1, v4, v2, v3}, Lcom/baidu/titan/sample/Person;-><init>(Ljava/lang/String;IILjava/lang/String;)V

    invoke-virtual {v0}, Lcom/baidu/titan/sample/Person;->getBirthYear()Ljava/lang/String;

    move-result-object v0

    invoke-static {p0, v0}, Lcom/baidu/titan/sample/ToastUtil;->showToast(Landroid/content/Context;Ljava/lang/String;)V

    return-void
.end method
