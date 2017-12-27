package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.enums.Currency;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by robertk on 5/29/2017.
 */
public class CoreSettings {

    public static final Integer IB_CONNECT_CLIENT_ID = 0;
    public static final Integer JPA_MAX_RESULTS = 1000;
    public static final Integer MAX_ORDER_HEARTBEAT_FAILS = 5;
    public static final String EXCHANGE_RATE_URL = "http://api.fixer.io/";
    public static final Integer EXCHANGE_RATE_DAYS_BACK = 5;
    public static final DateFormat EXCHANGE_RATE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final Currency PORTFOLIO_BASE = Currency.EUR;
    public static final String EMAIL_FROM = "hpb@highpowerbear.com";
    public static final String EMAIL_TO = "info@highpowerbear.com";
}