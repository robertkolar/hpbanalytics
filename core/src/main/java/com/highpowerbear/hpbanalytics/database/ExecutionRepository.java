package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.ib.client.Types;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by robertk on 4/13/2020.
 */
public interface ExecutionRepository extends JpaRepository<Execution, Long>, JpaSpecificationExecutor<Execution> {

    List<Execution> findAllByOrderByFillDateAsc();
    boolean existsByFillDate(LocalDateTime fillDate);

    @Query("SELECT e FROM Execution e WHERE e.symbol = :symbol AND e.currency = :currency AND e.secType = :secType AND e.multiplier = :multiplier AND e.fillDate >= :cutoffDate ORDER BY e.fillDate ASC")
    List<Execution> findExecutionsToAnalyzeAgain(
            @Param("symbol") String symbol,
            @Param("currency") Currency currency,
            @Param("secType") Types.SecType secType,
            @Param("multiplier") double multiplier,
            @Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("update Execution e set e.trade = null")
    int disassociateAllExecutions();
}
