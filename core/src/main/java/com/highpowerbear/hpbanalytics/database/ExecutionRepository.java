package com.highpowerbear.hpbanalytics.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by robertk on 4/13/2020.
 */
public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    List<Execution> getByReportIdOrderByFillDateAsc(int reportId);
    List<Execution> getByReportIdAndSymbolAndFillDateAfterOrderByFillDateAsc(int reportId, String symbol, LocalDateTime cutoffDate);
    List<Execution> getByReportIdAndSymbolAndFillDateGreaterThanEqualOrderByFillDateAsc(int reportId, String symbol, LocalDateTime cutoffDate);
}
