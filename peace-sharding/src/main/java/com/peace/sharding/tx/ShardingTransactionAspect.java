package com.peace.sharding.tx;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.peace.sharding.ds.DataSourceFactory;
import com.peace.sharding.ds.annotation.Sharding;
import io.seata.core.context.RootContext;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.transaction.base.seata.at.SeataTransactionHolder;
import org.apache.shardingsphere.transaction.base.seata.at.SeataXIDContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * @author zfy
 * @since 2025/6/10
 */
@Aspect
@AllArgsConstructor
public class ShardingTransactionAspect {

    /**
     * 事务隔离级别为: REQUIRES_NEW
     */
    private TransactionTemplate transactionTemplate;

    /**
     * 备注：SeataATShardingSphereTransactionManager.commit是加入分布式事务时不会清理分布式事务上下文
     * 说明：切GlobalTransactional注解，方法执行完后手动清理事务上下文
     * 不清理情况：分表的seata事务会混乱，接口第二次访问无法正常提交回滚
     */
    @After("@annotation(io.seata.spring.annotation.GlobalTransactional)")
    public void after() {
        SeataTransactionHolder.clear();
        RootContext.unbind();
        SeataXIDContext.remove();
    }

    /**
     * 动态切换 本地事务/分布式事务
     */
    @Around("@annotation(com.peace.sharding.ds.annotation.Sharding)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 获取注解分表注解
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Sharding sharding = method.getAnnotation(Sharding.class);
        Assert.notNull(sharding, "Sharding ds annotation is null.");

        if (RootContext.getXID() == null) {
            // 本地事务场景: 是否新开事务执行
            return sharding.tx() ? proceed(pjp) : pjp.proceed();
        }
        try {
            // 分表分布式事务场景：置顶当前线程数据源为seata数据源: {ds}_seata
            // Seata事务管理器会加入当前分布式事务,查看方法: SeataATShardingSphereTransactionManager.begin
            DynamicDataSourceContextHolder.push(DataSourceFactory.createSeataDS(sharding.ds()));
            return this.proceed(pjp);
        } finally {
            // 方法执行完,线程恢复为默认数据源
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 新开启事务执行方法
     */
    private Object proceed(ProceedingJoinPoint pjp) {
        return transactionTemplate.execute(status -> {
            try {
                return pjp.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

}
