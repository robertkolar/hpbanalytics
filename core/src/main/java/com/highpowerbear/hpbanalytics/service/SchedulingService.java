package com.highpowerbear.hpbanalytics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by robertk on 11/1/2020.
 */
@Service
public class SchedulingService {

    private final List<ScheduledTaskPerformer> scheduledTaskPerformers;

    @Autowired
    public SchedulingService(List<ScheduledTaskPerformer> scheduledTaskPerformers) {
        this.scheduledTaskPerformers = scheduledTaskPerformers;
    }

    @Scheduled(cron = "0 0 1 * * *")
    private void performStartOfDayTasks() {
        scheduledTaskPerformers.forEach(ScheduledTaskPerformer::performStartOfDayTasks);
    }
}
