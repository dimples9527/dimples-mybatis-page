package com.dimples.plugins.reflect;

import org.springframework.util.Assert;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/14
 */
public class ReflectionKit {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new IdentityHashMap<>(8);

    /**
     * 判断是否为基本类型或基本包装类型
     *
     * @param clazz class
     * @return 是否基本类型或基本包装类型
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() || PRIMITIVE_WRAPPER_TYPE_MAP.containsKey(clazz));
    }

}
