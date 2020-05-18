package com.dimples.plugins.metadata;

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
    private Integer current;

    /**
     * 每页限制条数
     */
    private Integer size;

    /**
     * 总条数，插件会回填这个值
     */
    private Integer total;

    /**
     * 总页数， 插件会回填这个值
     */
    private Integer pages;

    /**
     * 查询记录
     */
    private List<T> records;

    /**
     * 是否进行 count 查询
     */
    private boolean searchCount = true;

    /**
     * 是否启动插件，如果不启动，则不作分页
     */
    private Boolean useFlag;

    /**
     * 是否检测页码的有效性，如果为true,而页码大于最大页数，则抛出异常
     */
    private Boolean checkFlag;

    /**
     * 是否清除最后order by后面的语句
     */
    private Boolean cleanOrderBy;

    /*==================================== Constructor ============================================*/

    public Page(Integer current, Integer size) {
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


    /**
     * 默认启用分页插件
     */
    public static final boolean DEFAULT_USER_FLAG = true;

    /**
     * 默认页码
     */
    public static final long DEFAULT_PAGE = 1L;

    /**
     * 默认页大小
     */
    public static final long DEFAULT_PAGE_SIZE = 500L;

    /**
     * 默认检测页码参数
     */
    public static final boolean DEFAULT_CHECK_FLAG = true;

    /**
     *
     */
    public static final boolean DEFAULT_CLEAN_ORDER_BY = true;

}












