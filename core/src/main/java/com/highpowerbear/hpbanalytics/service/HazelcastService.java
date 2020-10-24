package com.highpowerbear.hpbanalytics.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import com.highpowerbear.dto.ExecutionDTO;
import com.highpowerbear.hpbanalytics.common.ExecutionMapper;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.database.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by robertk on 10/23/2020.
 */
@Service
public class HazelcastService {
    private static final Logger log = LoggerFactory.getLogger(HazelcastService.class);

    private final HazelcastInstance hanHazelcastInstance;
    private final ScheduledExecutorService executorService;
    private final ExecutionMapper executionMapper;
    private final AnalyticsService analyticsService;

    private final AtomicBoolean hazelcastConsumerRunning = new AtomicBoolean(true);

    @Autowired
    public HazelcastService(HazelcastInstance hanHazelcastInstance,
                            ScheduledExecutorService executorService,
                            ExecutionMapper executionMapper,
                            AnalyticsService analyticsService) {

        this.hanHazelcastInstance = hanHazelcastInstance;
        this.executorService = executorService;
        this.executionMapper = executionMapper;
        this.analyticsService = analyticsService;

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown)); // shutdown hook
        startHazelcastConsumer();
    }

    public void startHazelcastConsumer() {

        final int delay = HanSettings.HAZELCAST_CONSUMER_START_DELAY_SECONDS;
        log.info("starting hazelcast consumer in " + delay + " seconds");

        executorService.schedule(() -> {
            IQueue<ExecutionDTO> queue = hanHazelcastInstance.getQueue(HanSettings.HAZELCAST_EXECUTION_QUEUE_NAME);
            log.info("hazelcast consumer ready");

            while (hazelcastConsumerRunning.get()) {
                try {
                    ExecutionDTO dto = queue.take();
                    Execution execution = executionMapper.dtoToEntity(dto);
                    execution.setSymbol(HanUtil.removeWhiteSpaces(execution.getSymbol()));

                    log.info("consumed execution from the hazelcast queue " + execution);
                    analyticsService.addExecution(execution);

                } catch (HazelcastInstanceNotActiveException he) {
                    log.error(he.getMessage() + " ... stopping hazelcast consumer task");
                    hazelcastConsumerRunning.set(false);

                } catch (Exception e) {
                    log.error("hazelcast consumer task exception caught: " + e.getMessage());
                }
            }
            log.info("hazelcast consumer task exit");

        }, delay, TimeUnit.SECONDS);
    }

    private void shutdown() {
        executorService.shutdown();
        hazelcastConsumerRunning.set(false);
    }
}