package com.highpowerbear.hpbanalytics.webapi;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by robertk on 9/17/2015.
 */
@Component
public class WebsocketController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WebsocketController.class);

    private Set<Session> ibloggerSessions = new HashSet<>();
    private Set<Session> reportSessions = new HashSet<>();

    public Set<Session> getIbloggerSessions() {
        return ibloggerSessions;
    }

    public Set<Session> getReportSessions() {
        return reportSessions;
    }

    public void sendIbLoggerMessage(Session s, String message) {
        try {
            s.getBasicRemote().sendText(message);
        } catch (Throwable ioe) {
            log.error("Error sending websocket message " + message, ioe);
        }
    }

    public void broadcastIbLoggerMessage(String message) {
        ibloggerSessions.stream().filter(Session::isOpen).forEach(s -> sendIbLoggerMessage(s, message));
    }

    public void sendReportMessage(Session s, String message) {
        try {
            s.getBasicRemote().sendText(message);
        } catch (Throwable ioe) {
            log.error("Error sending websocket message " + message, ioe);
        }
    }

    public void broadcastReportMessage(String message) {
        reportSessions.stream().filter(Session::isOpen).forEach(s -> sendReportMessage(s, message));
    }
}
