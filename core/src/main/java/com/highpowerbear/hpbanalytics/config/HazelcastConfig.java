package com.highpowerbear.hpbanalytics.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by robertk on 10/4/2020.
 */
@Configuration
public class HazelcastConfig {

    @Bean
    public HazelcastInstance hanHazelcastInstance() {

        QueueConfig executionQueueConfig = new QueueConfig(HanSettings.HAZELCAST_EXECUTION_QUEUE_NAME)
                .setBackupCount(HanSettings.HAZELCAST_EXECUTION_QUEUE_BACKUP_COUNT)
                .setMaxSize(HanSettings.HAZELCAST_EXECUTION_QUEUE_MAX_SZE)
                .setStatisticsEnabled(true);

        Config config = new Config(HanSettings.HAZELCAST_INSTANCE_NAME)
                .addQueueConfig(executionQueueConfig);

        return Hazelcast.newHazelcastInstance(config);
    }
}
