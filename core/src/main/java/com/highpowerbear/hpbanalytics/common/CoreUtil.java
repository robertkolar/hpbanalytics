package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.enums.OptionType;
import com.highpowerbear.hpbanalytics.enums.StatisticsInterval;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by robertk on 5/29/2017.
 */
public class CoreUtil {

    public static OptionParseResult parseOptionSymbol(String optionSymbol) throws Exception {
        OptionParseResult result = new OptionParseResult();

        if (optionSymbol.length() > 21 || optionSymbol.length() < 16) {
            throw new Exception(optionSymbol + " has not correct length");
        }
        int l = optionSymbol.length();
        result.setUnderlying(optionSymbol.substring(0, l-15).trim().toUpperCase());

        String yy = optionSymbol.substring(l-15, l-13);
        String MM = optionSymbol.substring(l-13, l-11);
        String dd = optionSymbol.substring(l-11, l-9);
        result.setOptType(OptionType.getFromShortName(optionSymbol.substring(l - 9, l - 8)));

        String str = optionSymbol.substring(l-8, l-3);
        String strDec = optionSymbol.substring(l-3, l);
        DateFormat df = new SimpleDateFormat("yyMMdd");
        df.setLenient(false);
        df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date expDate = df.parse(yy+MM+dd);
        expDate.setTime(expDate.getTime() + (1000 * 60 * 60 * 23)); // add 23 hours
        result.setExpDate(expDate);

        DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        df1.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        result.setStrikePrice(nf.parse(str + "." + strDec).doubleValue());

        return result;
    }

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
        Calendar beginPeriodDate = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
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

    public static String formatExchangeRateDate(final Calendar calendar) {
        return CoreSettings.EXCHANGE_RATE_DATE_FORMAT.format(calendar.getTime());
    }

    public static Calendar previousDay(final Calendar calendar) {
        Calendar previousDay = Calendar.getInstance();
        previousDay.setTimeInMillis(calendar.getTimeInMillis());
        previousDay.set(Calendar.DAY_OF_MONTH, -1);

        return previousDay;
    }
}