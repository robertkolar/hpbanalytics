package com.highpowerbear.hpbanalytics.config;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.StatisticsPlMethod;

/**
 * Created by robertk on 5/29/2017.
 */
public class HanSettings {

    private HanSettings() {
    }

    public static final int IFI_START_YEAR = 2016;
    public static final int PL_SCALE = 5;
    public static final int SCHEDULED_THREAD_POOL_SIZE = 10;

    public static final Currency PORTFOLIO_BASE = Currency.EUR;
    public static final StatisticsPlMethod STATISTICS_PL_METHOD = StatisticsPlMethod.PORTFOLIO_BASE_CLOSE_ONLY;

    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
}
