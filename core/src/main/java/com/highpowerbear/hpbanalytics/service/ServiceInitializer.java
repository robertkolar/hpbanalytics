package com.highpowerbear.hpbanalytics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by robertk on 10/25/2020.
 */
@Component
public class ServiceInitializer {

    @Autowired
    public ServiceInitializer(List<InitializingService> initializingServices) {
        initializingServices.forEach(InitializingService::initialize);
    }
}
