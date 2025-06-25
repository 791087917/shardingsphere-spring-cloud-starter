package com.peace.sharding.ds;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zfy
 * @since 2025/6/17
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.dynamic.sharding")
public class DataSourceProperties {

    private String datasource;

}
