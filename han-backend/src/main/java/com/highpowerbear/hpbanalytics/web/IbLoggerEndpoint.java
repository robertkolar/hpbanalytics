package com.highpowerbear.hpbanalytics.web;

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
 * Created by rkolar on 5/12/2014.
 */
@Component
@ServerEndpoint("/websocket/iblogger")
public class IbLoggerEndpoint {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(IbLoggerEndpoint.class);

    @Autowired
    private WebsocketController websocketController;

    @OnOpen
    public void addSesssion(Session session) {
        log.trace("Websocket connection opened");
        websocketController.getIbloggerSessions().add(session);
    }

    @OnMessage
    public void echo(Session session, String message) {
        log.trace("Websocket message received " + message);
        websocketController.sendIbLoggerMessage(session, message);
    }

    @OnError
    public void logError(Throwable t) {
        log.error("Websocket error", t);
    }

    @OnClose
    public void removeSession(Session session) {
        log.trace("Websocket connection closed");
        websocketController.getIbloggerSessions().remove(session);
    }
}