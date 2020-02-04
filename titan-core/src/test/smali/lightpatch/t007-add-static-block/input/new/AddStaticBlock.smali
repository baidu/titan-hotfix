.class public Lcom/baidu/titan/sample/AddStaticBlock;
.super Ljava/lang/Object;
.source "AddStaticBlock.java"

.method static constructor <clinit>()V
    .registers 2
    .prologue
    .line 5
    sget-object v0, Ljava/lang/System;->out:Ljava/io/PrintStream;
    const-string v1, "static block"
    invoke-virtual { v0, v1 }, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    .line 6
    return-void
.end method

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 3
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method
