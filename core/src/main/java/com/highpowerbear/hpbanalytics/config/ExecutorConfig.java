package com.highpowerbear.hpbanalytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by robertk on 4/5/2020.
 */
@Configuration
public class ExecutorConfig {

    @Bean
    @Primary
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(HanSettings.SCHEDULED_THREAD_POOL_SIZE);
    }
}
