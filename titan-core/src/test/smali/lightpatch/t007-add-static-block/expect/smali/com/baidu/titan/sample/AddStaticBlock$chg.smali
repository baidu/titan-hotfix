.class public Lcom/baidu/titan/sample/AddStaticBlock$chg;
.super Ljava/lang/Object;


# direct methods

.method public static $staticInit([Ljava/lang/Object;)V
    .param p0    # [Ljava/lang/Object;
    .locals 2


    .line 5
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v1, "static block"

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 6
    return-void


.end method
