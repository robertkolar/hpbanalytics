package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.StatisticsPLMethod;

import java.time.format.DateTimeFormatter;

/**
 * Created by robertk on 5/29/2017.
 */
public class HanSettings {
    public static final int MAX_ORDER_HEARTBEAT_FAILS = 5;
    public static final String EXCHANGE_RATE_URL = "http://data.fixer.io/api";
    public static final int EXCHANGE_RATE_DAYS_BACK = 5;
    public static final int IFI_START_YEAR = 2016;
    public static final int PL_SCALE = 4;
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final DateTimeFormatter JSON_DATE_FORMATTER = DateTimeFormatter.ofPattern(JSON_DATE_FORMAT);
    public static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss.SSS");
    public static final DateTimeFormatter EXCHANGE_RATE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter IB_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final Currency PORTFOLIO_BASE = Currency.EUR;
    public static final StatisticsPLMethod STATISTICS_PL_METHOD = StatisticsPLMethod.PORTFOLIO_BASE_CLOSE_ONLY;
    public static final String EMAIL_FROM = "hpb@highpowerbear.com";
    public static final String EMAIL_TO = "info@highpowerbear.com";
    public static final String JMS_DEST_ORDER_FILLED = "orderFilled";
    public static final String JMS_DEST_EXECUTION_RECEIVED = "executionReceived";
    public static final String WS_TOPIC_ORDTRACK = "/topic/ordtrack";
    public static final String WS_TOPIC_REPORT = "/topic/report";
    public static final String EXCHANGE_RATES_SYMBOLS = "EUR,USD,GBP,CHF,AUD,JPY,KRW,HKD,SGD";
}
