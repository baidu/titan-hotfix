.class public Lcom/baidu/titan/sample/ChangeMethodCode$chg;
.super Ljava/lang/Object;


# direct methods

.method public static method1(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodCode;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 1


    .line 13
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v0, p1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 14
    const-string v0, ""

    return-object v0


.end method

.method public static method2(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodCode;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 1


    .line 19
    invoke-static {}, Ljava/lang/Thread;->dumpStack()V

    .line 20
    const-string v0, ""

    return-object v0


.end method

.method public static method3(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodCode;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 2


    .line 25
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {p1}, Ljava/lang/String;->length()I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/io/PrintStream;->println(I)V

    .line 26
    const-string v0, ""

    return-object v0


.end method

.method public static method4(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodCode;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 2


    .line 31
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0, p2}, Ljava/util/ArrayList;-><init>(I)V

    .line 32
    const/4 v1, 0x1

    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v1

    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 33
    const-string v0, ""

    return-object v0


.end method

.method public static method5(Lcom/baidu/titan/sample/ChangeMethodCode;Ljava/lang/String;I)Ljava/lang/String;
    .param p0    # Lcom/baidu/titan/sample/ChangeMethodCode;
    .param p1    # Ljava/lang/String;
    .param p2    # I
    .locals 4


    .line 38
    const/4 v1, 0x0

    .line 40
    :try_start_0
    new-instance v0, Ljava/io/BufferedInputStream;

    new-instance v2, Ljava/io/FileInputStream;

    new-instance v3, Ljava/io/File;

    invoke-direct {v3, p1}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-direct {v2, v3}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V

    invoke-direct {v0, v2}, Ljava/io/BufferedInputStream;-><init>(Ljava/io/InputStream;)V

    .line 44
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_1
    .catchall {:try_start_0 .. :try_end_0} :catchall_0
    if-eqz v0, :cond_0

    .line 46
    :try_start_1
    invoke-virtual {v0}, Ljava/io/InputStream;->close()V

    .line 52
    :cond_0
    :goto_0
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_0
    const-string v0, ""

    return-object v0

    .line 47
    :catch_0
    move-exception v0

    .line 48
    invoke-virtual {v0}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_0

    .line 41
    :catch_1
    move-exception v0

    .line 42
    :try_start_2
    invoke-virtual {v0}, Ljava/io/IOException;->printStackTrace()V

    .line 44
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0
    if-eqz v1, :cond_0

    .line 46
    :try_start_3
    invoke-virtual {v1}, Ljava/io/InputStream;->close()V

    :try_end_3
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_2
    goto :goto_0

    .line 47
    :catch_2
    move-exception v0

    .line 48
    invoke-virtual {v0}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_0

    .line 44
    :catchall_0
    move-exception v0

    if-eqz v1, :cond_1

    .line 46
    :try_start_4
    invoke-virtual {v1}, Ljava/io/InputStream;->close()V

    .line 49
    :cond_1
    :goto_1
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_3
    throw v0

    .line 47
    :catch_3
    move-exception v1

    .line 48
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_1


.end method
