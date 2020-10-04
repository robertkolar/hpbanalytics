package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.dto.ExecutionDTO;

/**
 * Created by robertk on 10/4/2020.
 */
public interface ExecutionListener {

    void executionReceived(ExecutionDTO execution);
}
