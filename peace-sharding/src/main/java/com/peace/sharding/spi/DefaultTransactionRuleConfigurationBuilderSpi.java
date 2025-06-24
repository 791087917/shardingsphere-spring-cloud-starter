package com.peace.sharding.spi;


import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.constant.TransactionOrder;

import java.util.Properties;

/**
 * 适配{@link TransactionRuleBuilderSpi}
 *
 * @author zfy
 * @since 2024/11/14
 */
public class DefaultTransactionRuleConfigurationBuilderSpi implements DefaultGlobalRuleConfigurationBuilder<TransactionRuleConfiguration, TransactionRuleBuilderSpi> {

    @Override
    public TransactionRuleConfiguration build() {
        return new TransactionRuleConfiguration(TransactionType.LOCAL.name(), null, new Properties());
    }

    @Override
    public int getOrder() {
        return TransactionOrder.ORDER + 1;
    }

    @Override
    public Class<TransactionRuleBuilderSpi> getTypeClass() {
        return TransactionRuleBuilderSpi.class;
    }
}
