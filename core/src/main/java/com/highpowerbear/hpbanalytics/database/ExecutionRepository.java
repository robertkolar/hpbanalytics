package com.highpowerbear.hpbanalytics.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by robertk on 4/13/2020.
 */
public interface ExecutionRepository extends JpaRepository<Execution, Long>, JpaSpecificationExecutor<Execution> {

    List<Execution> findAllByOrderByFillDateAsc();
    List<Execution> findBySymbolAndFillDateAfterOrderByFillDateAsc(String symbol, LocalDateTime cutoffDate);
    List<Execution> findBySymbolAndFillDateGreaterThanEqualOrderByFillDateAsc(String symbol, LocalDateTime cutoffDate);
}
