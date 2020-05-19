package com.dimples.test.service;

import com.alibaba.fastjson.JSON;
import com.dimples.plugins.metadata.Page;
import com.dimples.test.po.Drug;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DrugServiceTest {

    @Autowired
    private DrugService drugService;

    @Test
    public void listPage() {
        Page<Drug> drugs = drugService.listPage();
        log.info(JSON.toJSONString(drugs));
    }
}