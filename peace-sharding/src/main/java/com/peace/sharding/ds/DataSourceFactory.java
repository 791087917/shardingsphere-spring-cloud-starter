package com.peace.sharding.ds;

import com.peace.sharding.spi.TransactionRuleBuilderSpi;
import com.peace.zookeeper.props.ZooKeeperProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author zfy
 * @since 2024/11/18
 */
@Slf4j
public class DataSourceFactory {

    public static Map<String, DataSource> create(ZooKeeperProperties zkProps, DataSourceProperties dsProps) {
        Assert.notNull(dsProps.getDatasource(), "Sharding datasource is null.");

        String localDS = dsProps.getDatasource();
        String seataDS = createSeataDS(localDS);
        // 默认创建LOCAL + SEATA 两种事务的DataSource
        HashMap<String, DataSource> map = new HashMap<>();
        map.put(localDS, create(zkProps, dsProps.getDatasource(), true));
        map.put(seataDS, create(zkProps, dsProps.getDatasource(), false));
        log.info("Sharding datasource init success : {}", localDS);
        return map;
    }

    public static String createSeataDS(String db) {
        return db + "_seata";
    }

    @SneakyThrows
    private static DataSource create(ZooKeeperProperties properties, String database, Boolean localTx) {
        // ShardingSphere使用ZooKeeper集群模式
        ModeConfiguration mode = getModeConfiguration(properties.getServer(), properties.getNamespace());
        // 设置数据源事务类型: 本地事务/分布式事务
        TransactionRuleBuilderSpi.ENABLE_LOCAL_TX = localTx;
        // 分表 + 集成分布式事务数据源
        return new ShardingSphereDataSource(database, mode);
    }

    private static ModeConfiguration getModeConfiguration(String server, String namespace) {
        Properties props = new Properties();
        props.setProperty("operationTimeoutMilliseconds", "500000");
        props.setProperty("timeToLiveSeconds", "60");
        props.setProperty("maxRetries", "10");
        props.setProperty("retryIntervalMilliseconds", "5000");

        ClusterPersistRepositoryConfiguration configuration = new ClusterPersistRepositoryConfiguration("ZooKeeper",
                namespace,
                server,
                props);
        return new ModeConfiguration("Cluster", configuration);
    }

}
