package com.dimples.configure;

import com.dimples.plugins.PageInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhongyj <1126834403@qq.com><br/>
 * @date 2020/5/16
 */
@Configuration
public class MybatisConfigure {

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        pageInterceptor.setDialectType("mysql");
        return pageInterceptor;
    }

}
