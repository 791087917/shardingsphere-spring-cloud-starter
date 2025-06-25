package com.peace.sharding.tx;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.peace.sharding.ds.DataSourceFactory;
import com.peace.sharding.ds.DataSourceProperties;
import io.seata.core.context.RootContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author zfy
 * @since 2025/6/12
 */
@Slf4j
@AllArgsConstructor
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class ShardingTransactionInterceptor implements Interceptor {

    private TransactionTemplate transactionTemplate;
    private TransactionTemplate transactionTemplateNew;
    private DataSourceProperties dataSourceProperties;

    /**
     * 拦截sql层存在资源浪费,同一个方法内会多次新建数据库连接,存在内存问题
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String ds = DynamicDataSourceContextHolder.peek();
        // 1.单表数据源不拦截
        // 2.seata数据源不拦截
        if (!dataSourceProperties.getDatasource().equals(ds)) {
            return invocation.proceed();
        }
        // 拦截修改(insert/update/delete)
        return proceedUpdate(invocation, ds);
    }

    private Object proceedUpdate(Invocation invocation, String ds) {
        // 分表本地事务场景：使用默认TX,动态切换数据源后默认增加事务
        // 业务方法上层指定事务后,下层使用DS切换数据源不会生效.
        if (RootContext.getXID() == null) {
            return proceedTx(transactionTemplate, invocation);
        }
        try {
            //分表分布式事务场景：设置数据源为集成seata事务数据源
            DynamicDataSourceContextHolder.push(DataSourceFactory.createSeataDS(ds));
            // 新建本地事务，Seata事务管理器会加入当前分布式事务
            // 查看方法: SeataATShardingSphereTransactionManager.begin
            return proceedTx(transactionTemplateNew, invocation);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    private Object proceedTx(TransactionTemplate tx, Invocation invocation) {
        return tx.execute(status -> {
            try {
                return invocation.proceed();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
