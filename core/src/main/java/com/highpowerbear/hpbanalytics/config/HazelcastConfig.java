package com.highpowerbear.hpbanalytics.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by robertk on 10/4/2020.
 */
@Configuration
public class HazelcastConfig {
    private static final Logger log = LoggerFactory.getLogger(HazelcastConfig.class);

    @Bean
    public HazelcastInstance hanHazelcastInstance() {

        Config config = new Config(HanSettings.HAZELCAST_INSTANCE_NAME)
                .addQueueConfig(executionQueueConfig())
                .addMapConfig(exchangeRateMapConfig())
                .addMapConfig(statisticsMapConfig());

        log.info("hazelcast queue configs " + config.getQueueConfigs());
        log.info("hazelcast map configs " + config.getMapConfigs());

        return Hazelcast.newHazelcastInstance(config);
    }

    private QueueConfig executionQueueConfig() {
        log.info("configuring hazelcast execution queue");

        return new QueueConfig(HanSettings.HAZELCAST_EXECUTION_QUEUE_NAME)
                .setMaxSize(HanSettings.HAZELCAST_EXECUTION_QUEUE_MAX_SZE)
                .setStatisticsEnabled(true);
    }

    private MapConfig exchangeRateMapConfig() {
        log.info("configuring hazelcast exchange rate map");

        return new MapConfig(HanSettings.HAZELCAST_EXCHANGE_RATE_MAP_NAME)
                .setEvictionPolicy(EvictionPolicy.NONE)
                .setMaxIdleSeconds(HanSettings.HAZELCAST_EXCHANGE_RATE_MAP_TIME_MAX_IDLE_SECONDS);
    }

    private MapConfig statisticsMapConfig() {
        log.info("configuring hazelcast statistics map");

        return new MapConfig(HanSettings.HAZELCAST_STATISTICS_MAP_NAME)
                .setEvictionPolicy(EvictionPolicy.NONE);
    }
}
