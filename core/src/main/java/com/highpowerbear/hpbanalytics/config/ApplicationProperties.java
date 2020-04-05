package com.highpowerbear.hpbanalytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Created by robertk on 4/5/2020.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
}
