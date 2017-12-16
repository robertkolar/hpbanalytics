package com.highpowerbear.hpbanalytics.common;

/**
 * Created by robertk on 5/29/2017.
 */
public class HanDefinitions {

    // constants
    public static final int ONE_SECOND = 1000; // milliseconds
    public static final String CONVERSION_ORIGIN_PREFIX_IB = "IB:";
    public static final Integer ONE_SECOND_MILLIS = 1000;
    public static final String ORIGIN_INTERNAL = "INTERNAL";
    public static final String NOT_AVAILABLE = "N/A";
    public static final String ALL_UNDERLYINGS = "ALLUNDLS";
    public static final String ASSIGN_TRADE_COMMENT = "ASSIGN";
    public static final String EXPIRE_TRADE_COMMENT = "EXPIRE";
    public static final String CLOSE_TRADE_COMMENT = "CLOSE";

    // settings
    public static final String IBLOGGER_TO_REPORT_QUEUE = "java:/jms/queue/IbLoggerToReportQ";
    public static final Integer IB_CONNECT_CLIENT_ID = 0;
    public static final Integer JPA_MAX_RESULTS = 1000;
    public static final Integer MAX_ORDER_HEARTBEAT_FAILS = 5;
    public static final String TIMEZONE = "America/New_York";
    public static final String EXCHANGE_RATE_URL = "http://api.fixer.io/";
    public static final Integer EXCHANGE_RATE_DAYS_BACK = 5;
    public static final Integer MAX_STATS_RETURNED = 180;

    public enum FilterOperatorString {
        LIKE("LIKE");

        private String sql;
        FilterOperatorString(String sql) {
            this.sql = sql;
        }
        public String getSql() {
            return sql;
        }
    }

    public enum FilterOperatorNumber {
        EQ("="),
        GT(">"),
        LT("<");

        private String sql;
        FilterOperatorNumber(String sql) {
            this.sql = sql;
        }
        public String getSql() {
            return sql;
        }
    }

    public enum FilterOperatorCalendar {
        EQ("="),
        GT(">"),
        LT("<");

        private String sql;
        FilterOperatorCalendar(String sql) {
            this.sql = sql;
        }
        public String getSql() {
            return sql;
        }
    }

    public enum FilterOperatorEnum {
        IN("IN");

        private String sql;
        FilterOperatorEnum(String sql) {
            this.sql = sql;
        }
        public String getSql() {
            return sql;
        }
    }

    public enum FilterKey {
        PROPERTY,
        OPERATOR,
        VALUE;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    public enum IbOrderFilterField {
        SYMBOL("symbol"),
        SEC_TYPE("secType"),
        SUBMIT_DATE("submitDate"),
        STATUS("status");

        private String varName;

        IbOrderFilterField(String varName) {
            this.varName = varName;
        }

        public String getVarName() {
            return varName;
        }
    }

    public enum ExecutionFilterField {
        SYMBOL("symbol"),
        SEC_TYPE("secType"),
        FILL_DATE("fillDate");

        private String varName;

        ExecutionFilterField(String varName) {
            this.varName = varName;
        }

        public String getVarName() {
            return varName;
        }
    }

    public enum TradeFilterField {
        SYMBOL("symbol"),
        SEC_TYPE("secType"),
        OPEN_DATE("openDate"),
        STATUS("status");

        private String varName;

        TradeFilterField(String varName) {
            this.varName = varName;
        }

        public String getVarName() {
            return varName;
        }
    }
}
