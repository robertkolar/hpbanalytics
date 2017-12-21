package com.highpowerbear.hpbanalytics.webapi;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Created by robertk on 9/17/2015.
 */
@Controller
public class WebsocketController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WebsocketController.class);

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
