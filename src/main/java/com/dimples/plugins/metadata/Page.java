package com.dimples.plugins.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

import lombok.Data;

/**
 * 分页参数
 *
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/17
 */
@Data
public class Page<T> implements IPage<T> {

    /**
     * 当前页码
     */
    private long current = 1L;

    /**
     * 每页限制条数
     */
    private long size = 500L;

    /**
     * 总条数，插件会回填这个值
     */
    private long total = 0L;

    /**
     * 排序字段信息
     */
    private List<OrderItem> orders = Lists.newArrayList();

    /**
     * 查询记录
     */
    private List<T> records = Lists.newArrayList();

    /**
     * 是否进行 count 查询
     */
    @JsonIgnore
    private boolean searchCount = true;

    /**
     * 是否启动插件，如果不启动，则不作分页
     */
    @JsonIgnore
    private Boolean useFlag = true;

    /**
     * 是否检测页码的有效性，如果为true,而页码大于最大页数，则抛出异常
     */
    @JsonIgnore
    private Boolean checkFlag = true;

    /**
     * 是否清除最后order by后面的语句
     */
    @JsonIgnore
    private Boolean cleanOrderBy = true;

    /*==================================== Constructor ============================================*/

    public Page(long current, long size) {
        this.current = current;
        this.size = size;
    }

    @Override
    public boolean searchCount() {
        if (total < 0) {
            return false;
        }
        return searchCount;
    }

    public Page<T> setSearchCount(boolean searchCount) {
        this.searchCount = searchCount;
        return this;
    }

    @Override
    public long getTotal() {
        return this.total;
    }

    @Override
    public void setTotal(long total) {
        this.total = total;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public Page<T> setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public List<T> getRecords() {
        return this.records;
    }

    @Override
    public void setRecords(List<T> records) {
        this.records = records;
    }

    @Override
    public List<OrderItem> orders() {
        return getOrders();
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }

    /**
     * 添加新的排序条件，构造条件可以使用工厂：{@link OrderItem#build(String, boolean)}
     *
     * @param items 条件
     */
    public void addOrder(OrderItem... items) {
        orders.addAll(Arrays.asList(items));
    }

    /**
     * 添加新的排序条件，构造条件可以使用工厂：{@link OrderItem#build(String, boolean)}
     *
     * @param items 条件
     * @return 返回分页参数本身
     */
    public Page<T> addOrder(List<OrderItem> items) {
        orders.addAll(items);
        return this;
    }

}












