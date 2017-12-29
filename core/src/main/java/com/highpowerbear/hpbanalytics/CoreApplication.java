package com.highpowerbear.hpbanalytics;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by robertk on 5/29/2017.
 */
@SpringBootApplication
@EnableJms
@EnableScheduling
@EnableAsync
public class CoreApplication {
}