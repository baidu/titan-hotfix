.class public Lcom/baidu/titan/sample/MethodOverloadingSmali;
.super Ljava/lang/Object;
.source "MethodOverloadingSmali.java"

.method public constructor <init>()V
    .registers 1
    .prologue
    .line 3
    invoke-direct { p0 }, Ljava/lang/Object;-><init>()V
    return-void
.end method

.method static invokeC(C)V
    .registers 1
    .prologue
    .line 17
    return-void
.end method

.method static invokeD(D)V
    .registers 2
    .prologue
    .line 13
    return-void
.end method

.method static invokeS(S)V
    .registers 1
    .prologue
    .line 8
    return-void
.end method

.method invokeB(B)V
    .registers 2
    .prologue
    .line 185
    return-void
.end method

.method invokeF(F)V
    .registers 2
    .prologue
    .line 113
    return-void
.end method

.method invokeI(I)V
    .registers 2
    .prologue
    .line 41
    return-void
.end method

.method invokeII(II)V
    .registers 3
    .prologue
    .line 95
    return-void
.end method

.method invokeIII(III)V
    .registers 4
    .prologue
    .line 191
    return-void
.end method

.method invokeIIII(IIII)V
    .registers 5
    .prologue
    .line 179
    return-void
.end method

.method invokeIIL(IILjava/lang/Object;)V
    .registers 4
    .prologue
    .line 143
    return-void
.end method

.method invokeIL(ILjava/lang/Object;)V
    .registers 3
    .prologue
    .line 65
    return-void
.end method

.method invokeILL(ILjava/lang/Object;Ljava/lang/Object;)V
    .registers 4
    .prologue
    .line 125
    return-void
.end method

.method invokeJ(J)V
    .registers 3
    .prologue
    .line 77
    return-void
.end method

.method invokeJL(JLjava/lang/Object;)V
    .registers 4
    .prologue
    .line 155
    return-void
.end method

.method invokeL(Ljava/lang/Object;)V
    .registers 2
    .prologue
    .line 29
    return-void
.end method

.method invokeLF(Ljava/lang/Object;F)V
    .registers 3
    .prologue
    .line 161
    return-void
.end method

.method invokeLI(Ljava/lang/Object;I)V
    .registers 3
    .prologue
    .line 47
    return-void
.end method

.method invokeLII(Ljava/lang/Object;II)V
    .registers 4
    .prologue
    .line 101
    return-void
.end method

.method invokeLIII(Ljava/lang/Object;III)V
    .registers 5
    .prologue
    .line 149
    return-void
.end method

.method invokeLIL(Ljava/lang/Object;ILjava/lang/Object;)V
    .registers 4
    .prologue
    .line 107
    return-void
.end method

.method invokeLILL(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;)V
    .registers 5
    .prologue
    .line 209
    return-void
.end method

.method invokeLJ(Ljava/lang/Object;J)V
    .registers 4
    .prologue
    .line 119
    return-void
.end method

.method invokeLL(Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 3
    .prologue
    .line 35
    return-void
.end method

.method invokeLLI(Ljava/lang/Object;Ljava/lang/Object;I)V
    .registers 4
    .prologue
    .line 89
    return-void
.end method

.method invokeLLII(Ljava/lang/Object;Ljava/lang/Object;II)V
    .registers 5
    .prologue
    .line 197
    return-void
.end method

.method invokeLLIL(Ljava/lang/Object;Ljava/lang/Object;ILjava/lang/Object;)V
    .registers 5
    .prologue
    .line 203
    return-void
.end method

.method invokeLLL(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 4
    .prologue
    .line 53
    return-void
.end method

.method invokeLLLI(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)V
    .registers 5
    .prologue
    .line 173
    return-void
.end method

.method invokeLLLL(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 5
    .prologue
    .line 83
    return-void
.end method

.method invokeLLLLL(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 6
    .prologue
    .line 137
    return-void
.end method

.method invokeLLZ(Ljava/lang/Object;Ljava/lang/Object;Z)V
    .registers 4
    .prologue
    .line 131
    return-void
.end method

.method invokeLZ(Ljava/lang/Object;Z)V
    .registers 3
    .prologue
    .line 71
    return-void
.end method

.method invokeV()V
    .registers 1
    .prologue
    .line 23
    return-void
.end method

.method invokeZ(Z)V
    .registers 2
    .prologue
    .line 59
    return-void
.end method

.method invokeZL(ZLjava/lang/Object;)V
    .registers 3
    .prologue
    .line 167
    return-void
.end method

.method returnB()B
    .registers 2
    .prologue
    .line 220
    const/16 v0, 127
    return v0
.end method

.method returnC()C
    .registers 2
    .prologue
    .line 232
    const v0, 65535
    return v0
.end method

.method returnD()D
    .registers 3
    .prologue
    .line 256
    const-wide v0, 9218868437227405311L
    return-wide v0
.end method

.method returnF()F
    .registers 2
    .prologue
    .line 250
    const v0, 2139095039
    return v0
.end method

.method returnI()I
    .registers 2
    .prologue
    .line 238
    const v0, 2147483647
    return v0
.end method

.method returnJ()J
    .registers 3
    .prologue
    .line 244
    const-wide v0, 9223372036854775807L
    return-wide v0
.end method

.method returnL()Ljava/lang/Object;
    .registers 2
    .prologue
    .line 262
    new-instance v0, Ljava/lang/Object;
    invoke-direct { v0 }, Ljava/lang/Object;-><init>()V
    return-object v0
.end method

.method returnS()S
    .registers 2
    .prologue
    .line 226
    const/16 v0, 32767
    return v0
.end method

.method returnZ()Z
    .registers 2
    .prologue
    .line 214
    const/4 v0, 1
    return v0
.end method
