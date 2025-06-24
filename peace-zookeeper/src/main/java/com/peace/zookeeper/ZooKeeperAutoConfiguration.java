package com.peace.zookeeper;

import com.peace.zookeeper.props.ZooKeeperProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author zfy
 * @since 2024/11/29
 */
public class ZooKeeperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ZooKeeperProperties zooKeeperProperties() {
        return new ZooKeeperProperties();
    }

}
