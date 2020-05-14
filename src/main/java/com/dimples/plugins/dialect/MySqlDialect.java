package com.dimples.plugins.dialect;

import com.dimples.plugins.metadata.OrderItem;
import com.dimples.plugins.metadata.Page;

import java.util.List;

import cn.hutool.core.collection.CollectionUtil;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/13
 */
public class MySqlDialect implements IDialect {

    @Override
    public String buildPaginationSql(String originalSql, Page<?> page) {
        int offset = page.getSize();
        int limit = (page.getCurrent() - 1) * page.getSize();

        StringBuilder result = new StringBuilder(originalSql.length() + 200);
        result.append(originalSql);

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

        if (offset > 0) {
            result.append(" limit ").append(limit).append(",").append(offset);
        } else {
            result.append(limit);
        }

        return result.toString();
    }
}
