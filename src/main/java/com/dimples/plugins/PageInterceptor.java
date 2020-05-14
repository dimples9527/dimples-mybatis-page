package com.dimples.plugins;

import com.baomidou.mybatisplus.core.MybatisDefaultParameterHandler;
import com.dimples.plugins.dialect.IDialect;
import com.dimples.plugins.dialect.MySqlDialect;
import com.dimples.plugins.metadata.Page;
import com.dimples.plugins.utils.PluginUtils;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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

    private String dialectType;

    private String dialectClazz;

    /**
     * 单页限制 500 条，小于 0 如 -1 不受限制
     */
    private static final int LIMIT = 500;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 由于是使用的mybatis-plus而不是mybatis, 所以此处获取的是plus中的自定义的StatementHandler,并且不能强转，只能调用其提供的PluginUtils.realTarget进行获取
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 先判断是不是SELECT操作  (2019-04-10 00:37:31 跳过存储过程)
        if (SqlCommandType.SELECT != ms.getSqlCommandType()
                || StatementType.CALLABLE == ms.getStatementType()) {
            return invocation.proceed();
        }

        // 针对定义了rowBounds，做为mapper接口方法的参数
        BoundSql sourceBoundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        // 获取参数
        Object parameter = sourceBoundSql.getParameterObject();

        // 定义数据库方言
        IDialect dialect = buildDialect();

        // 判断参数里是否有page对象
        Page<?> page = null;
        if (parameter instanceof Page) {
            page = (Page<?>) parameter;
        } else if (parameter instanceof Map) {
            for (Object arg : ((Map<?, ?>) parameter).values()) {
                if (arg instanceof Page) {
                    page = (Page<?>) arg;
                    break;
                }
            }
        }

        /*
         * 不需要分页的场合，如果 size 小于 0 返回结果集
         */
        if (null == page || page.getSize() < 0) {
            return invocation.proceed();
        }

        /*
         * 处理单页条数限制
         */
        if (LIMIT > 0 && LIMIT <= page.getSize()) {
            page.setSize(LIMIT);
        }

        // 处理分页
        if (ObjectUtil.isNotEmpty(page)) {
            final BoundSql boundSql = ms.getBoundSql(parameter);
            String originalSql = boundSql.getSql();
            // 是否需要查询总记录数
            if (Objects.requireNonNull(page).getTotal() == 0) {
                MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
                Connection connection = (Connection) invocation.getArgs()[0];
                count(originalSql, connection, mappedStatement, boundSql, page);
            }

            // 构造新的SQL ，生成分页语句
            String pagingSql = dialect.buildPaginationSql(originalSql, page);

            List<ParameterMapping> mappings = new ArrayList<>(boundSql.getParameterMappings());
            metaObject.setValue("delegate.boundSql.sql", pagingSql);
            metaObject.setValue("delegate.boundSql.parameterMappings", mappings);

        }
        return invocation.proceed();
    }

    private IDialect buildDialect() throws InstantiationException, IllegalAccessException {
        IDialect dialect = null;
        if (dialectType != null && !"".equals(dialectType)) {
            switch (dialectType.toLowerCase()) {
                case "mysql":
                    dialect = new MySqlDialect();
                    break;
                case "oracle":
                    log.info("oracle");
                    break;
                default:
                    break;
            }
        } else {
            if (dialectClazz != null && !"".equals(dialectClazz)) {
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
            throw new IllegalArgumentException("The value of the dialect property in mybatis configuration.xml is not defined.");
        }
        return dialect;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        String dialectType = properties.getProperty("dialectType");
        String dialectClazz = properties.getProperty("dialectClazz");
        if (StrUtil.isNotEmpty(dialectType)) {
            this.dialectType = dialectType;
        }
        if (StrUtil.isNotEmpty(dialectClazz)) {
            this.dialectClazz = dialectClazz;
        }
    }

    /**
     * 查询总记录条数
     *
     * @param sql             原始sql
     * @param connection      Connection
     * @param mappedStatement MappedStatement
     * @param boundSql        BoundSql
     * @param page            Page
     */
    public void count(String sql, Connection connection, MappedStatement mappedStatement, BoundSql boundSql, Page<?> page) {
        // 记录总记录数
        String countSql = "SELECT COUNT(0) FROM ( " + sql + " ) AS TOTAL";
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = connection.prepareStatement(countSql);
            // 这里不使用重写的 DefaultParameterHandler 的原因是为了支持mybatis在xml中的一些特殊标签，如<bind/>
            DefaultParameterHandler parameterHandler = new MybatisDefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
            parameterHandler.setParameters(preparedStatement);
            rs = preparedStatement.executeQuery();
            int total = 0;
            if (rs.next()) {
                total = rs.getInt(1);
            }
            page.setTotal(total);
            /*
             * 溢出总页数，设置第一页
             */
            long pages = page.getPages();
            if (page.getCurrent() > pages) {
                // 设置为第一条
                page.setCurrent(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置方言类型
     *
     * @param dialectType 数据库名,全小写
     */
    public void setDialectType(String dialectType) {
        this.dialectType = dialectType;
    }

    /**
     * 设置方言实现类
     *
     * @param dialectClazz 方言实现类
     */
    public void setDialectClazz(String dialectClazz) {
        this.dialectClazz = dialectClazz;
    }
}
