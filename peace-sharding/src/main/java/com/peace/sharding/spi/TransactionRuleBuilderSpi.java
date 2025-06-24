package com.peace.sharding.spi;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.constant.TransactionOrder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Map;

/**
 * @author zfy
 * @since 2024/11/14
 */
public class TransactionRuleBuilderSpi implements GlobalRuleBuilder<TransactionRuleConfiguration> {

    public static Boolean ENABLE_LOCAL_TX = Boolean.TRUE;

    /**
     * ShardingSphere对外提供Spi机制，无侵入式修改源码逻辑
     * 动态设置事务规则 {@link TransactionRule}
     */
    @Override
    public GlobalRule build(TransactionRuleConfiguration ruleConfig, Map<String, ShardingSphereDatabase> databases, ConfigurationProperties configProps) {
        databases.forEach((name, db) -> {
            ShardingSphereResourceMetaData meta = db.getResourceMetaData();
            if (meta == null || meta.getDataSources() == null || meta.getDataSources().isEmpty()) {
                throw new RuntimeException("Logic database " + name + " is not exist or datasource is empty");
            }
        });
        // ZK中RuleConfig默认设置为本地事务,扩展shardingsphere初始化事务规则
        TransactionRuleConfiguration config = Boolean.TRUE.equals(ENABLE_LOCAL_TX)
                ? ruleConfig
                : new TransactionRuleConfiguration("BASE", "Seata", ruleConfig.getProps());
        // 默认重置为本地事务
        ENABLE_LOCAL_TX = Boolean.TRUE;
        return new TransactionRule(config, databases);
    }

    /**
     * 执行顺序大于默认规则，根据TransactionRuleConfiguration类型覆盖默认规则
     */
    @Override
    public int getOrder() {
        return TransactionOrder.ORDER + 1;
    }

    @Override
    public Class<TransactionRuleConfiguration> getTypeClass() {
        return TransactionRuleConfiguration.class;
    }

}
