package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.TradeType;
import com.ib.client.Types;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by robertk on 4/13/2020.
 */
public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    List<Trade> findByTypeAndCloseDateBetweenOrderByOpenDateAsc(TradeType type, LocalDateTime closeDateBegin, LocalDateTime closeDateEnd);

    @Query("SELECT t FROM Trade t WHERE  t.symbol = :symbol AND t.currency = :currency AND t.secType = :secType AND t.multiplier = :multiplier AND (t.closeDate >= :fillDate OR t.openPosition <> 0) ORDER BY t.openDate ASC")
    List<Trade> findTradesAffectedByExecution(
            @Param("symbol") String symbol,
            @Param("currency") Currency currency,
            @Param("secType") Types.SecType secType,
            @Param("multiplier") double multiplier,
            @Param("fillDate") LocalDateTime fillDate);

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t ORDER BY u")
    List<String> findAllUnderlyings();

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t WHERE t.openPosition <> 0 ORDER BY u")
    List<String> findOpenUnderlyings();

    @Query("SELECT COUNT(t) FROM Trade t WHERE t.openPosition <> 0")
    long countOpenTrades();

    @Query("SELECT COUNT(DISTINCT t.underlying) AS u FROM Trade t")
    long countAllUnderlyings();

    @Query("SELECT COUNT(DISTINCT t.underlying) AS u FROM Trade t WHERE t.openPosition <> 0")
    long countOpenUnderlyings();
}
