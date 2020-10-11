package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.TradeType;
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

    @Query("SELECT t FROM Trade t WHERE (t.closeDate >= :fillDate OR t.openPosition <> 0) AND t.conid = :conid ORDER BY t.openDate ASC")
    List<Trade> findTradesAffectedByExecution(@Param("fillDate") LocalDateTime fillDate, @Param("conid") int conid);

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t ORDER BY u")
    List<String> findAllUnderlyings();

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t WHERE t.openPosition <> 0 ORDER BY u")
    List<String> findOpenUnderlyings();
}
