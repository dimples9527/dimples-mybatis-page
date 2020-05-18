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

- XML 中
```
<include refid="Menu_List" />
SELECT *
FROM EMR_USER.tb_menu tm
LEFT JOIN EMR_USER.tb_menu_system_dict AS tmsd ON tmsd.id = tm.belong_system
LEFT JOIN EMR_USER.tb_menu_params AS tmp ON tmp.menu_id = tm.menu_id
LEFT JOIN EMR_USER.tb_menu_href_params AS tmhp ON tmhp.id = tmp.menu_href_param_id
LEFT JOIN EMR_USER.tb_role_menu AS trm ON trm.menu_id = tm.menu_id
LEFT JOIN EMR_USER.tb_role AS tr ON tr.role_id = trm.role_id
<where>
    AND tm.menu_type = '菜单'
    AND tr.role_id = #{roleId}
        <if test="belongSystem != null and belongSystem != ''">
    AND belong_system = #{belongSystem}
    </if>
</where>
```
