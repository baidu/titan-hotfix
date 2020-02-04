.class public Lcom/baidu/titan/sample/ChangeMethodArgs$chg;
.super Ljava/lang/Object;


# direct methods

.method public static method1(Lcom/baidu/titan/sample/ChangeMethodArgs;I)V
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodArgs;
    .param p1    # I
    .locals 0


    .line 39
    return-void


.end method

.method public static method2(Lcom/baidu/titan/sample/ChangeMethodArgs;Ljava/lang/String;)V
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodArgs;
    .param p1    # Ljava/lang/String;
    .locals 0


    .line 43
    return-void


.end method

.method public static method3(Lcom/baidu/titan/sample/ChangeMethodArgs;)Ljava/lang/String;
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodArgs;
    .locals 1


    .line 46
    const-string v0, "s"

    return-object v0


.end method

.method public static method4(Lcom/baidu/titan/sample/ChangeMethodArgs;Ljava/lang/String;)Ljava/lang/String;
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodArgs;
    .param p1    # Ljava/lang/String;
    .locals 2


    .line 50
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "return: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0


.end method

.method public static staticMethod1(I)V
    .param p0    # I
    .locals 0


    .line 55
    return-void


.end method

.method public static staticMethod2(Ljava/lang/String;)V
    .param p0    # Ljava/lang/String;
    .locals 0


    .line 59
    return-void


.end method

.method public static staticMethod3()Ljava/lang/String;
    .locals 1


    .line 62
    const-string v0, "s"

    return-object v0


.end method

.method public static staticMethod4(Ljava/lang/String;)Ljava/lang/String;
    .param p0    # Ljava/lang/String;
    .locals 2


    .line 66
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "return: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0


.end method
