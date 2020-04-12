package com.highpowerbear.hpbanalytics;

import com.highpowerbear.hpbanalytics.config.ApplicationProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by robertk on 5/29/2017.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableJpaRepositories
public class CoreApplication {
}
