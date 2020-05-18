package com.dimples.plugins;

import com.dimples.plugins.metadata.Page;
import com.dimples.plugins.util.PageUtil;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 分页插件
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/13
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
@Slf4j
public class PageInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PageUtil.realProxyObject(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        String sql = (String) metaObject.getValue("delegate.boundSql.sql");
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 只拦截SELECT语句
        if (!PageUtil.checkedSelect(sql)) {
            return invocation.proceed();
        }

        // 获取参数
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        Object parameterObject = boundSql.getParameterObject();
        // 分离出分页参数
        Page<?> page = PageUtil.getPageParamsForParamObj(parameterObject);
        if (ObjectUtil.isNull(page)) {
            // 如果没有获取到分页参数, 不进行分页
            return invocation.proceed();
        }

        // 获取配置中是否启用分页功能
        boolean userFlag = ObjectUtil.isNull(page.getUseFlag()) ? Page.DEFAULT_USER_FLAG : page.getUseFlag();
        if (!userFlag) {
            return invocation.proceed();
        }

        // 获取总页数
        if (page.searchCount()) {
            this.count(invocation, metaObject, boundSql, page);
            if (page.getTotal() <= 0) {
                return null;
            }
        }

        return invocation.proceed();
    }

    /**
     * 获取数据总条数
     *
     * @param invocation Invocation
     * @param metaObject MetaObject
     * @param boundSql   BoundSql
     * @param page       Page<?>
     */
    private void count(Invocation invocation, MetaObject metaObject, BoundSql boundSql, Page<?> page) {
        // 获取当前的 MappedStatement
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

    }

    @Override
    public Object plugin(Object target) {
        // 采用系统默认的Plugin.wrap
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
