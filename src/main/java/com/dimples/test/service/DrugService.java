package com.dimples.test.service;

import com.dimples.plugins.metadata.Page;
import com.dimples.test.po.Drug;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/16
 */
public interface DrugService {

    Page<Drug> listPage();

}
