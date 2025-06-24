package com.peace.sharding.tx;

import com.peace.sharding.ds.DataSourceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author zfy
 * @since 2025/6/12
 */
@Configuration
@EnableTransactionManagement
public class ShardingTransactionManager {

    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private DataSourceProperties dataSourceProperties;

    @Bean
    @ConditionalOnBean(DataSource.class)
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        // 注册事务管理器，分表场景必须配置。
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public ShardingTransactionAspect shardingTransactionAspect(PlatformTransactionManager transactionManager) {
        TransactionTemplate txNew = createTxNew(transactionManager);
        return new ShardingTransactionAspect(txNew);
    }

    //    @Bean
    public ShardingTransactionInterceptor shardingTransactionalInterceptor(PlatformTransactionManager transactionManager) {
        // 指定新建事务隔离级别TX
        TransactionTemplate txNew = createTxNew(transactionManager);
        return new ShardingTransactionInterceptor(transactionTemplate, txNew, dataSourceProperties);
    }

    private TransactionTemplate createTxNew(PlatformTransactionManager transactionManager) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new TransactionTemplate(transactionManager, def);
    }

}
