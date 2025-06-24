package com.peace.zookeeper.auth;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author zfy
 * @since 2024/11/29
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DefaultAuthContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String AUTH_CONFIG = "java.security.auth.login.config";
    private static final String AUTH_CLIENT = "zookeeper.sasl.client";
    private static final String AUTH_FILE = "zk_jass.conf";

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        try {
            if (System.getProperty(AUTH_CLIENT) == null) {
                String path = System.getProperty("user.home") + "/" + AUTH_FILE;
                Files.deleteIfExists(new File(path).toPath());

                InputStream is = new ClassPathResource(AUTH_FILE).getInputStream();
                File target = new File(path);
                Files.copy(is, target.toPath());

                System.setProperty(AUTH_CONFIG, target.getAbsolutePath());
                System.setProperty(AUTH_CLIENT, "true");
                log.info("Set default zookeeper auth");
            }
        } catch (Exception e) {
            log.error("Set default zookeeper auth failed", e);
        }
    }

}
