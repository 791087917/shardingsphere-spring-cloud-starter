package com.peace.sharding;

import com.peace.sharding.ds.DataSourceProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author zfy
 * @since 2024/11/18
 */
public class ShardingAutoConfiguration {

    @Bean
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

}
