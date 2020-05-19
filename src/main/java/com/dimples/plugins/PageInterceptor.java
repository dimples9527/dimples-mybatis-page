package com.dimples.plugins;

import com.dimples.plugins.dialect.IDialect;
import com.dimples.plugins.dialect.MySqlDialect;
import com.dimples.plugins.dialect.OracleDialect;
import com.dimples.plugins.metadata.Page;
import com.dimples.plugins.util.PageUtil;
import com.google.common.collect.Lists;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 分页插件
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/13
 */
@Slf4j
@Setter
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PageInterceptor implements Interceptor {

    public static final String ORDER_FLAG = "order";

    /**
     * 方言类型
     */
    private String dialectType;

    /**
     * 方言实现类<br>
     * 注意！实现 com.dimples.plugins.dialect.IDialect 接口的子类
     */
    private String dialectClazz;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PageUtil.realProxyObject(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        // 获取当前的 MappedStatement
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String sql = (String) metaObject.getValue("delegate.boundSql.sql");

        // 只拦截SELECT语句
        if (!PageUtil.checkedSelect(sql)) {
            return invocation.proceed();
        }

        // 获取参数
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        Object parameterObject = boundSql.getParameterObject();
        // 分离出分页参数
        Page<?> page = PageUtil.getPageParamsForParamObj(parameterObject);

        // 不需要分页的场合，如果 size 小于 0 返回结果集
        if (ObjectUtil.isNull(page) || page.getSize() < 0) {
            // 如果没有获取到分页参数, 不进行分页
            return invocation.proceed();
        }

        // 获取配置中是否启用分页功能
        if (!page.getUseFlag()) {
            return invocation.proceed();
        }

        // 获取总页数
        if (page.searchCount()) {
            this.count(invocation, metaObject, mappedStatement, boundSql, page);
            if (page.getTotal() <= 0) {
                return null;
            }
        }
        // 修改分页SQL
        return preparePageSql(invocation, metaObject, boundSql, page);
    }

    /**
     * 处理分页
     *
     * @param invocation      Invocation
     * @param metaObject      MetaObject
     * @param boundSql        BoundSql
     * @param page            Page
     * @return Object
     */
    private Object preparePageSql(Invocation invocation, MetaObject metaObject, BoundSql boundSql, Page<?> page) throws Exception {
        IDialect dialect = null;
        if (StrUtil.isNotBlank(dialectType)) {
            switch (dialectType.toLowerCase()) {
                case "mysql":
                    dialect = new MySqlDialect();
                    break;
                case "oracle":
                    dialect = new OracleDialect();
                    break;
                default:
                    break;
            }
        } else {
            if (StrUtil.isNotBlank(dialectClazz)) {
                try {
                    Class<?> clazz = Class.forName(dialectClazz);
                    if (IDialect.class.isAssignableFrom(clazz)) {
                        dialect = (IDialect) clazz.newInstance();
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Class :" + dialectClazz + " is not found");
                }
            }
        }
        // 未配置方言则抛出异常
        if (dialect == null) {
            throw new IllegalArgumentException("根据现有的配置, 找不到相应的Dialect");
        }

        // 配置分页SQL
        String originalSql = boundSql.getSql();
        // 构造新的SQL ，生成分页语句
        String pagingSql = dialect.buildPaginationSql(originalSql, page);

        List<ParameterMapping> mappings = Lists.newArrayList(boundSql.getParameterMappings());
        metaObject.setValue("delegate.boundSql.sql", pagingSql);
        metaObject.setValue("delegate.boundSql.parameterMappings", mappings);
        return invocation.proceed();
    }

    /**
     * 获取数据总条数
     *
     * @param invocation      Invocation
     * @param metaObject      MetaObject
     * @param mappedStatement MappedStatement
     * @param boundSql        BoundSql
     * @param page            Page<?>
     */
    private void count(Invocation invocation, MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql, Page<?> page) {

        //配置对象
        Configuration configuration = mappedStatement.getConfiguration();
        // 当前执行的SQL
        String sql = (String) metaObject.getValue("delegate.boundSql.sql");
        // 去掉ORDER BY语句, 提升性能
        if (page.getCleanOrderBy()) {
            sql = this.cleanOrderByForSql(sql);
        }
        // 改写统计总数的SQL
        String countSql = "SELECT COUNT(0) FROM ( " + sql + " ) AS TOTAL";
        Connection connection = (Connection) invocation.getArgs()[0];
        PreparedStatement preparedStatement = null;
        long total = 0;
        try {
            preparedStatement = connection.prepareStatement(countSql);
            // 构建BoundSql
            BoundSql countBoundSql = new BoundSql(configuration, countSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
            // 构建 Mybatis的ParameterHandler用来设置总数的SQL
            ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), countBoundSql);
            // 设置参数
            parameterHandler.setParameters(preparedStatement);
            // 执行查询
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                total = rs.getLong(1);
            }
            // 设置回显数据
            page.setTotal(total);
            // 检查页码的有效性
            this.checkPage(page);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 这里不能关闭Connection, 否则后续查询不能进行
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查页码的有效性
     *
     * @param page Page
     */
    private void checkPage(Page<?> page) throws Exception {
        if (page.getCheckFlag() && page.getCurrent() > page.getPages()) {
            throw new Exception("查询失败, 查询页码【" + page.getCurrent() + "】大于总页数【" + page.getPages() + "】!!!");
        }
    }

    /**
     * 清除SQL中的 ORDER BY 语句
     *
     * @param sql 　源 SQL
     * @return 处理后的 SQL
     */
    private String cleanOrderByForSql(String sql) {
        StringBuilder stringBuilder = new StringBuilder(sql);
        if (!StrUtil.containsIgnoreCase(stringBuilder, ORDER_FLAG)) {
            return sql;
        }
        int idx = sql.toLowerCase().indexOf(ORDER_FLAG);
        return StrUtil.sub(stringBuilder, 0, idx);
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            // 采用系统默认的Plugin.wrap
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties prop) {
        String dialectType = prop.getProperty("dialectType");
        String dialectClazz = prop.getProperty("dialectClazz");
        if (StrUtil.isNotEmpty(dialectType)) {
            this.dialectType = dialectType;
        }
        if (StrUtil.isNotEmpty(dialectClazz)) {
            this.dialectClazz = dialectClazz;
        }
    }
}
