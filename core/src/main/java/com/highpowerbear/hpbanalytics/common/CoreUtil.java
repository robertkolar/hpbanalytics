package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;
import java.util.Calendar;

/**
 * Created by robertk on 5/29/2017.
 */
public class CoreUtil {

    public static void waitMilliseconds(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ie) {
            // Ignore
        }
    }

    public static String removeSpace(String source) {
        return source.replaceAll("\\b\\s+\\b", "");
    }

    public static Calendar toBeginOfPeriod(Calendar cal, StatisticsInterval interval) {
        Calendar beginPeriodDate = CoreUtil.calNow();
        beginPeriodDate.setTimeInMillis(cal.getTimeInMillis());

        if (StatisticsInterval.YEAR.equals(interval)) {
            beginPeriodDate.set(Calendar.MONTH, 0);
            beginPeriodDate.set(Calendar.DAY_OF_MONTH, 1);

        } else if (StatisticsInterval.MONTH.equals(interval)) {
            beginPeriodDate.set(Calendar.DAY_OF_MONTH, 1);
        }

        beginPeriodDate.set(Calendar.HOUR_OF_DAY, 0);
        beginPeriodDate.set(Calendar.MINUTE, 0);
        beginPeriodDate.set(Calendar.SECOND, 0);
        beginPeriodDate.set(Calendar.MILLISECOND, 0);

        return beginPeriodDate;
    }

    public static String toDurationString(long millis) {
        long days = millis / (24 * 60 * 60 * 1000);
        long daysRemainder = millis % (24 * 60 * 60 * 1000);
        long hours = daysRemainder / (60 * 60 * 1000);
        long hoursRemainder = daysRemainder % (60 * 60 * 1000);
        long minutes = hoursRemainder / (60 * 1000);
        long minutesRemainder = hoursRemainder % (60 * 1000);
        long seconds = minutesRemainder / (1000);

        return days + " Days " + hours + ":" + minutes + ":" + seconds;
    }

    public static double round(double number, int decimalPlaces) {
        double modifier = Math.pow(10.0, decimalPlaces);
        return Math.round(number * modifier) / modifier;
    }

    public static double round2(double number) {
        return round(number, 2);
    }

    public static String formatLogDate(final Calendar calendar) {
        return CoreSettings.LOG_DATE_FORMAT.format(calendar.getTime());
    }

    public static String formatExchangeRateDate(final Calendar calendar) {
        return CoreSettings.EXCHANGE_RATE_DATE_FORMAT.format(calendar.getTime());
    }

    public static Calendar previousDay(final Calendar calendar) {
        Calendar previousDay = CoreUtil.calNow();
        previousDay.setTimeInMillis(calendar.getTimeInMillis());
        previousDay.set(Calendar.DAY_OF_MONTH, -1);

        return previousDay;
    }

    public static Calendar calNow() {
        return Calendar.getInstance();
    }
}