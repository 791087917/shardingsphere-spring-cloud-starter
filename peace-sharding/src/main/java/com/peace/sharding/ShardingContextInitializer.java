package com.peace.sharding;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author zfy
 * @since 2025/6/12
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShardingContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String CONST_APPLICATION_ID = "spring.application.name";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String application = environment.getProperty(CONST_APPLICATION_ID);
        if (application == null) {
            throw new RuntimeException("服务名称不能为空，请重试！");
        }
        System.setProperty(CONST_APPLICATION_ID, application);
    }
}
