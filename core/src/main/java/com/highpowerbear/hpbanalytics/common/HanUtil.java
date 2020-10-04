package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.config.HanSettings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by robertk on 5/29/2017.
 */
public class HanUtil {

    private HanUtil() {
    }

    private static final DateTimeFormatter exchangeRateDateFormatter = DateTimeFormatter.ofPattern(HanSettings.EXCHANGE_RATE_DATE_FORMAT);

    public static String toDurationString(long durationSeconds) {
        long days = durationSeconds / (24 * 60 * 60);
        long daysRemainder = durationSeconds % (24 * 60 * 60);
        long hours = daysRemainder / (60 * 60);
        long hoursRemainder = daysRemainder % (60 * 60);
        long minutes = hoursRemainder / 60;
        long seconds = hoursRemainder % 60;

        String h = String.format("%02d", hours);
        String m = String.format("%02d", minutes);
        String s = String.format("%02d", seconds);

        return days + "d " + h + ":" + m + ":" + s;
    }

    public static double round(double number, int decimalPlaces) {
        double modifier = Math.pow(10.0, decimalPlaces);
        return Math.round(number * modifier) / modifier;
    }

    public static double round2(double number) {
        return round(number, 2);
    }

    public static String formatExchangeRateDate(final LocalDate localDate) {
        return localDate.format(exchangeRateDateFormatter);
    }
}
