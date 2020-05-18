package com.dimples.test.service;

import com.dimples.test.mapper.DrugMapper;
import com.dimples.test.po.Drug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<Drug> listPage() {
        return drugMapper.listPage();
    }
}
