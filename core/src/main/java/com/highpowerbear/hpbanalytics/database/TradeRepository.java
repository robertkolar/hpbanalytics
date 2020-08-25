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

    List<Trade> findByTypeAndCloseDateBetweenOrderByOpenDateAsc(TradeType type, LocalDateTime closeDateBegin, LocalDateTime closeDateEnd); // TODO check if inclusive, should be

    // TODO cascade delete splitExecutions, check if it is already handled by deleteAll()

    @Query("SELECT t FROM Trade t WHERE (t.closeDate >= :fillDate OR t.status = com.highpowerbear.hpbanalytics.enums.TradeStatus.OPEN) AND t.symbol = :symbol ORDER BY t.openDate ASC")
    List<Trade> findTradesAffectedByExecution(@Param("fillDate") LocalDateTime fillDate,
                                             @Param("symbol") String symbol);

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t ORDER BY u")
    List<String> findAllUnderlyings();

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t WHERE t.status = com.highpowerbear.hpbanalytics.enums.TradeStatus.OPEN ORDER BY u")
    List<String> findOpenUnderlyings();
}
