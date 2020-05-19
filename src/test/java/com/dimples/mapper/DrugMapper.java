package com.dimples.mapper;

import com.dimples.plugins.metadata.Page;
import com.dimples.po.Drug;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/16
 */
@Mapper
public interface DrugMapper {

    List<Drug> listPage(Page<Drug> page);

}