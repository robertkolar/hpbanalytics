package com.highpowerbear.hpbanalytics.database;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by robertk on 4/13/2020.
 */
public interface ExecutionRepository extends JpaRepository<Execution, Long> {
}
