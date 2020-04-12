package com.highpowerbear.hpbanalytics.config;

/**
 * Created by robertk on 4/5/2020.
 */
public class WsTopic {

    private WsTopic() {
    }

    // topics
    public static final String EXECUTION = "execution";
    public static final String TRADE = "trade";
    public static final String STATISTICS = "statistics";

    // settings
    public static final String TOPIC_PREFIX = "/topic";
    public static final String APPLICATION_PREFIX = "/app";
    public static final String STOMP_ENDPOINT = "/websocket";
}
