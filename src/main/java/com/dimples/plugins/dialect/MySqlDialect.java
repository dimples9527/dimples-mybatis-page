package com.dimples.plugins.dialect;

import com.dimples.plugins.metadata.Page;
import com.dimples.plugins.util.PageUtil;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/19
 */
public class MySqlDialect implements IDialect {

    @Override
    public String buildPaginationSql(String originalSql, Page<?> page) {
        long offset = page.getSize();
        long limit = (page.getCurrent() - 1) * page.getSize();

        StringBuilder result = new StringBuilder(originalSql.length() + 200);
        result.append(originalSql);

        PageUtil.buildOrder(page, result);

        if (offset > 0) {
            result.append(" limit ").append(limit).append(",").append(offset);
        }

        return result.toString();
    }

}
