package com.highpowerbear.hpbanalytics.database;

import com.highpowerbear.hpbanalytics.enums.TradeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by robertk on 4/13/2020.
 */
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> getByReportIdAndTypeAndCloseDateBetweenOrderByOpenDateAsc(int reportId, TradeType tradeType, LocalDateTime beginDate, LocalDateTime endDate); // TODO check if inclusive, should be
    void deleteByReportId(int reportId); // TODO cascade delete splitExecutions, check if it is already handled

    @Query("SELECT t FROM Trade t WHERE t.reportId = :reportId AND (t.closeDate >= :fillDate OR t.status = com.highpowerbear.hpbanalytics.enums.TradeStatus.OPEN) AND t.symbol = :symbol ORDER BY t.openDate ASC")
    List<Trade> getTradesAffectedByExecution(@Param("reportId") int reportId,
                                             @Param("fillDate") LocalDateTime fillDate,
                                             @Param("symbol") String symbol);

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t WHERE t.reportId = :reportId ORDER BY u")
    List<String> getAllUnderlyings(@Param("reportId") int reportId);

    @Query("SELECT DISTINCT t.underlying AS u FROM Trade t WHERE t.reportId = :reportId AND t.status = com.highpowerbear.hpbanalytics.enums.TradeStatus.OPEN ORDER BY u")
    List<String> getOpenUnderlyings(@Param("reportId") int reportId);
}
