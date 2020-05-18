# dimples-mybatis-page

#### 使用

- 注册插件
```
    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        pageInterceptor.setDialectType("cache");
        pageInterceptor.setDialectClazz("com.medical.cdr.core.page.CacheDialect");
        return pageInterceptor;
    }
```

- Service中使用
```
Page<MenuDTO> page = new Page<>(queryRequest.getPageNumber(), queryRequest.getPageSize());
        if (StrUtil.isNotBlank(queryRequest.getField()) && StrUtil.isNotBlank(queryRequest.getOrder())) {
            OrderItem orderItem = new OrderItem().setAsc(StrUtil.equalsIgnoreCase(queryRequest.getOrder(), "ASC"))
                    .setColumn(queryRequest.getField());
            page.addOrder(orderItem);
        } else {
            OrderItem orderItem = new OrderItem().setAsc(true).setColumn("menu_order");
            page.addOrder(orderItem);
        }
        page.setRecords(this.baseMapper.selectListByRoleId(page, roleId, belongSystem));
```

- Mapper中
```
List<MenuDTO> selectListByRoleId(@Param("page") Page<MenuDTO> page, @Param("roleId") String roleId, @Param("belongSystem") String belongSystem);
```
