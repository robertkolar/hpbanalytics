package com.highpowerbear.hpbanalytics.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.highpowerbear.dto.ExecutionDTO;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.config.WsTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by robertk on 12/27/2017.
 */
@Service
public class MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final HazelcastInstance hanHazelcastInstance;
    private final ExecutorService executorService;

    private final AtomicBoolean hazelcastConsumerRunning = new AtomicBoolean(true);
    private final List<ExecutionListener> executionListeners = new ArrayList<>();

    @Autowired
    public MessageService(SimpMessagingTemplate simpMessagingTemplate,
                          HazelcastInstance hanHazelcastInstance,
                          ExecutorService executorService) {

        this.simpMessagingTemplate = simpMessagingTemplate;
        this.hanHazelcastInstance = hanHazelcastInstance;
        this.executorService = executorService;

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        startHazelcastConsumer();
    }

    public void sendWsReloadRequestMessage(String topic) {
        sendWsMessage(topic, HanSettings.WS_RELOAD_REQUEST_MESSAGE);
    }

    private void sendWsMessage(String topic, String message) {
        simpMessagingTemplate.convertAndSend(WsTopic.TOPIC_PREFIX + "/" + topic, message);
    }

    public void registerExecutionListener(ExecutionListener executionListener) {
        executionListeners.add(executionListener);
    }

    public void startHazelcastConsumer() {
        IQueue<ExecutionDTO> queue = hanHazelcastInstance.getQueue(HanSettings.HAZELCAST_EXECUTION_QUEUE_NAME);

        executorService.execute(() -> {
            while(hazelcastConsumerRunning.get()) {
                try {
                    ExecutionDTO execution = queue.take();
                    log.info("consumed execution from the hazelcast queue " + execution);
                    executionListeners.forEach(listener -> listener.executionReceived(execution));

                } catch (InterruptedException ie) {
                    log.error(ie.getMessage());
                }
            }
        });
    }

    private void shutdown() {
        hazelcastConsumerRunning.set(true);
    }
}
