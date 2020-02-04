.class public Lcom/baidu/titan/sample/GenericSmali;
.super Ljava/lang/Object;
.source "GenericSmali.java"

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

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 6
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method public static min(Ljava/util/Collection;Ljava/util/Comparator;)Ljava/lang/Object;
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
    .registers 3
    .prologue
    const/4 v0, 0
    .line 17
    if-nez p1, :L1
    :L0
    .line 25
    return-object v0
    :L1
    .line 21
    if-eqz p0, :L0
    .line 25
    invoke-interface { p0 }, Ljava/util/Collection;->iterator()Ljava/util/Iterator;
    move-result-object v0
    invoke-interface { v0 }, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v0
    goto :L0
.end method

.method public add(Ljava/lang/Object;Ljava/lang/Object;)V
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(TK;TV;)V"
        }
    .end annotation
    .registers 3
    .prologue
    .line 13
    return-void
.end method

.method public find(Ljava/lang/Object;I)V
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<T:",
            "Ljava/lang/Object;",
            ">(TT;I)V"
        }
    .end annotation
    .registers 3
    .prologue
    .line 9
    return-void
.end method
