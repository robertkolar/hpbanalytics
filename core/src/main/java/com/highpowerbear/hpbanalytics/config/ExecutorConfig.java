package com.highpowerbear.hpbanalytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by robertk on 4/5/2020.
 */
@Configuration
public class ExecutorConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(HanSettings.SCHEDULED_THREAD_POOL_SIZE);
    }
}
