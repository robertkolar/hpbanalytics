package com.highpowerbear;

import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.jms.Queue;

/**
 * Created by robertk on 5/29/2017.
 */
@SpringBootApplication
@EnableJms
@EnableScheduling
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public CommandLineRunner start() {
        return (args) -> log.info("Application started");
    }

    @Bean
    public Queue queue() {
        return new ActiveMQQueue("ibLoggerToReport");
    }
}