package com.dimples.plugins.util;

import com.dimples.plugins.metadata.OrderItem;
import com.dimples.plugins.metadata.Page;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/18
 */
public class PageUtil {

    public static final String H = "h";

    /**
     * 获取真实的对象
     *
     * @param target Object
     * @return Object
     */
    public static Object realProxyObject(Object target) {

        MetaObject metaObject = SystemMetaObject.forObject(target);
        // 分离代理对象链（目标类可能被多个拦截器拦截, 从而形成多个次代理, 通哥循环可以分离出最原始的目标类
        Object object = null;
        while (metaObject.hasGetter(H)) {
            object = metaObject.getValue(H);
            metaObject = SystemMetaObject.forObject(object);
        }

        if (ObjectUtil.isNull(object)) {
            return target;
        }

        return object;
    }

    /**
     * 检测拦截的SQL语句类型
     *
     * @param sql String
     * @return boolean
     */
    public static boolean checkedSelect(String sql) {
        return StrUtil.containsIgnoreCase(sql, "select");
    }

    /**
     * 分离出分页参数
     *
     * @param parameterObject Object
     * @return Page<?>
     */
    @SuppressWarnings("unchecked")
    public static Page<?> getPageParamsForParamObj(Object parameterObject) throws Exception {
        if (ObjectUtil.isNull(parameterObject)) {
            return null;
        }

        // 处理Map参数, 多个匿名参数和@Param注解参数, 都是Map
        if (parameterObject instanceof Map) {
            Map<String, Object> paramMap = (Map<String, Object>) parameterObject;
            for (String key : paramMap.keySet()) {
                Object value = paramMap.get(key);
                if (value instanceof Page) {
                    return (Page<?>) value;
                }
            }
        } else if (parameterObject instanceof Page) {
            // 参数是或者是继承Page
            return (Page<?>) parameterObject;
        } else {
            // 从POJO尝试读取分页参数
            Field[] fields = parameterObject.getClass().getDeclaredFields();
            // 尝试从POJO中获取类型为Page的属性
            for (Field field : fields) {
                if (field.getType() == Page.class) {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), parameterObject.getClass());
                    Method readMethod = propertyDescriptor.getReadMethod();
                    return (Page<?>) readMethod.invoke(parameterObject);
                }
            }
        }
        return null;
    }

    /**
     * 处理分页中的排序
     *
     * @param page   Page
     * @param result StringBuilder
     */
    public static void buildOrder(Page<?> page, StringBuilder result) {
        if (CollectionUtil.isNotEmpty(page.getOrders())) {
            List<OrderItem> orders = page.getOrders();
            for (OrderItem order : orders) {
                if (order.isAsc()) {
                    result.append(" ORDER BY ").append(order.getColumn()).append(" ASC");
                } else {
                    result.append(" ORDER BY ").append(order.getColumn()).append(" DESC");
                }
            }
        }
    }
}
















