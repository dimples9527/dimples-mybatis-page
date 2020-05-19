package com.dimples.test.service;

import com.dimples.plugins.metadata.OrderItem;
import com.dimples.plugins.metadata.Page;
import com.dimples.test.mapper.DrugMapper;
import com.dimples.test.po.Drug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/16
 */
@Service
public class DrugServiceImpl implements DrugService {

    private DrugMapper drugMapper;

    @Autowired
    public DrugServiceImpl(DrugMapper drugMapper) {
        this.drugMapper = drugMapper;
    }

    @Override
    public Page<Drug> listPage() {
        Page<Drug> page = new Page<>(1, 10);
        page.addOrder(OrderItem.desc("case_id"));
        page.setRecords(drugMapper.listPage(page));
        return page;
    }
}













