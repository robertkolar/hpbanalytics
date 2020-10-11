package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by robertk on 4/13/2020.
 */
public interface ExecutionRepository extends JpaRepository<Execution, Long>, JpaSpecificationExecutor<Execution> {

    List<Execution> findAllByOrderByFillDateAsc();
    List<Execution> findBySymbolAndCurrencyAndMultiplierAndFillDateGreaterThanEqualOrderByFillDateAsc(String symbol, Currency currency, double multiplier, LocalDateTime cutoffDate);
    List<Execution> findByIdInOrderByFillDateAsc(List<Long> ids);
    boolean existsByFillDate(LocalDateTime fillDate);
}
