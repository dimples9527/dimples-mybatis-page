package com.dimples.plugins.metadata;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/18
 */
public interface IPage<T> {

    /**
     * 进行 count 查询 【 默认: true 】
     *
     * @return true 是 / false 否
     */
    default boolean searchCount() {
        return true;
    }

}
