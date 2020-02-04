package com.baidu.titan.core.filters;

import com.baidu.titan.core.instrument.MeizuPushMarker;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

/**
 * 过滤对meizu push sdk的访问
 *
 * @see com.baidu.titan.core.instrument.MeizuPushMarker
 *
 *
 * @author shanghuibo
 * @since 2019/04/24
 */
public class MeizuPushFilter implements DexClassPoolNodeVisitor {
    /** delegate visitor*/
    private DexClassPoolNodeVisitor delegate;

    public MeizuPushFilter(DexClassPoolNodeVisitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visitClass(DexClassNode dexClassNode) {
        if (!MeizuPushMarker.isMeizuPush(dexClassNode)) {
            if (delegate != null) {
                delegate.visitClass(dexClassNode);
            }
        }
    }

    @Override
    public void classPoolVisitEnd() {
        if (delegate != null) {
            delegate.classPoolVisitEnd();
        }
    }
}
