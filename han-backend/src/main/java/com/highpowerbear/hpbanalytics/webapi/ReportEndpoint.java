package com.highpowerbear.hpbanalytics.webapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by robertk on 9/17/2015.
 */
@Component
@ServerEndpoint("/websocket/report")
public class ReportEndpoint {
    private static final Logger log = LoggerFactory.getLogger(ReportEndpoint.class);

    @Autowired private WebsocketController websocketController;

    @OnOpen
    public void addSesssion(Session session) {
        log.trace("Websocket connection opened");
        websocketController.getReportSessions().add(session);
    }

    @OnMessage
    public void echo(Session session, String message) {
        log.trace("Websocket message received " + message);
        websocketController.sendReportMessage(session, message);
    }

    @OnError
    public void logError(Throwable t) {
        log.error("Websocket error", t);
    }

    @OnClose
    public void removeSession(Session session) {
        log.trace("Websocket connection closed");
        websocketController.getReportSessions().remove(session);
    }
}
