package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.config.WsTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by robertk on 12/27/2017.
 */
@Service
public class MessageService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendWsReloadRequestMessage(String topic) {
        simpMessagingTemplate.convertAndSend(WsTopic.TOPIC_PREFIX + "/" + topic, HanSettings.WS_RELOAD_REQUEST_MESSAGE);
    }
}
