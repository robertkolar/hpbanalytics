package com.highpowerbear.hpbanalytics.common;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by robertk on 9/17/2015.
 */
@Component
public class WsMessageSender {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WsMessageSender.class);

    @Autowired private SimpMessagingTemplate simpMessagingTemplate;

    public void sendIbLoggerMessage(String message) {
        log.info("sendIbLoggerMessage " + message);
        simpMessagingTemplate.convertAndSend("/topic/iblogger", message);
    }

    public void sendReportMessage(String message) {
        log.info("sendReportMessage " + message);
        simpMessagingTemplate.convertAndSend("/topic/report", message);
    }
}
