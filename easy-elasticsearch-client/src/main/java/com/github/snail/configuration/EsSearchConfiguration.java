package com.github.snail.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.snail.selector.SourceSelector;
import com.github.snail.service.ESSearchService;
import com.github.snail.service.impl.ESSearchServiceImpl;

/**
 * @author snail
 * Created on 2022-11-28
 */

@Configuration
public class EsSearchConfiguration {

    @Bean
    public ESSearchService esSearchServiceImpl() {
        return new ESSearchServiceImpl();
    }

    @Bean
    public SourceSelector sourceSelector() {
        return new SourceSelector();
    }
}
