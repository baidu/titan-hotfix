package com.baidu.titan.core.instrument;

import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.visitor.DexClassPoolNodeVisitor;

/**
 * Meizu push sdk标记器
 *
 * Meizu push sdk通过java序列化方式与Meizu手机交换数据，其中实现了Serializable接口的类未添加serialVersionUid字段，
 * 在热修复插桩后，类、类中field和某些方法的access_flag会被修改为public, 而jdk中计算默认serialVersionUid的方法会将access_flag作为因子参与计算，
 * 这使得插桩后的类的默认serialVersionUid与Meizu手机传递的数据中的serialVersionUid无法对应，导致meizu push sdk失效，
 * 所以在此标记一下meizu push sdk中的类，不对其进行插桩
 *
 * @author shanghuibo
 * @since 2019/04/24
 */
public class MeizuPushMarker implements DexClassPoolNodeVisitor {
    /** Key meizu push*/
    private static final String EXTRA_KEY_MEIZU_PUSH = "meizu_push";

    @Override
    public void visitClass(DexClassNode dcn) {
        String typeDesc = dcn.type.toTypeDescriptor();
        if (typeDesc != null && typeDesc.startsWith("Lcom/meizu/cloud/pushsdk")) {
            setIsMeizuPush(dcn);
        }
    }

    @Override
    public void classPoolVisitEnd() {
    }

    /**
     * 标记一个class是meizu push sdk中的类
     */
    private void setIsMeizuPush(DexClassNode dcn) {
        dcn.setExtraInfo(EXTRA_KEY_MEIZU_PUSH, true);
    }

    /**
     * 检查class是否是meizu push sdk中的类
     *
     * @param dcn dex class node
     * @return class是否是meizu push sdk中的类
     */
    public static boolean isMeizuPush(DexClassNode dcn) {
        return dcn.getExtraInfo(EXTRA_KEY_MEIZU_PUSH, false);
    }
}
