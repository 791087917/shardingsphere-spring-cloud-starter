package com.peace.zookeeper.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zfy
 * @since 2024/11/18
 */
@Data
@ConfigurationProperties(prefix = "spring.zookeeper")
public class ZooKeeperProperties {

    /**
     * zookeeper server list
     */
    private String server;
    /**
     * zookeeper namespace
     */
    private String namespace;

}
