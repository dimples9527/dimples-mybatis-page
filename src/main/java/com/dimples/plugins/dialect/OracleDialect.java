package com.dimples.plugins.dialect;

import com.dimples.plugins.metadata.Page;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/19
 */
public class OracleDialect implements IDialect {

    @Override
    public String buildPaginationSql(String originalSql, Page<?> page) {
        return originalSql;
    }

}
