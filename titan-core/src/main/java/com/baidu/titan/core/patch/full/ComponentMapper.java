package com.baidu.titan.core.patch.full;

import com.baidu.titan.dex.DexType;

/**
 *
 * 组件类名映射
 *
 * @author zhangdi07@baidu.com
 * @since 2019/3/14
 */
public abstract class ComponentMapper {

    public abstract DexType map(DexType old);

}
