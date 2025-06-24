package com.peace.sharding.ds.annotation;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.peace.sharding.tx.ShardingTransactionAspect;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 动态数据源切换时,加入当前事务DS无法生效
 * 1. 指定事务隔离级别为 NOT_SUPPORTED
 * 2. 注解对应方法默认不开启事务,由切面控制事务
 *
 * @author zfy
 * @since 2025/6/12
 */
@DS("")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sharding {

    /**
     * Data source name
     */
    @AliasFor(annotation = DS.class, attribute = "value")
    String ds();

    /**
     * Transaction enable
     * {@link ShardingTransactionAspect}
     */
    boolean tx() default false;

}