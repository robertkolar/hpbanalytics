package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.hpbanalytics.enums.OptionType;
import com.highpowerbear.hpbanalytics.model.OptionParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author robertk
 */
public class OptionUtil {
    private static final Logger log = LoggerFactory.getLogger(OptionUtil.class);

    private static final List<String> miniOptionRoots = new ArrayList<>();
    static {
        miniOptionRoots.add("AMZN7");
        miniOptionRoots.add("AAPL7");
        miniOptionRoots.add("GOOG7");
        miniOptionRoots.add("GLD7");
        miniOptionRoots.add("SPY7");
    }
    
    public static boolean isMini(String optionSymbol) {
        boolean isMini = false;
        for (String miniOptionRoot : miniOptionRoots) {
            if (optionSymbol.startsWith(miniOptionRoot)) {
                isMini = true;
                break;
            }
        }
        return isMini;
    }
    
    public static OptionParseResult parse(String optionSymbol) throws Exception {
        OptionParseResult result = new OptionParseResult();
        if (optionSymbol.length() > 21 || optionSymbol.length() < 16) {
            throw new Exception(optionSymbol + " has not correct length");
        }
        int l = optionSymbol.length();
        result.setUnderlying(optionSymbol.substring(0, (isMini(optionSymbol) ? l-16 : l-15)).trim().toUpperCase());

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
        String ds = df1.format(expDate);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        result.setStrikePrice(nf.parse(str + "." + strDec).doubleValue());

        log.info("Parsed: " + optionSymbol + " --> " + result.getUnderlying() + " " + result.getOptType() + " " + ds + " " + result.getStrikePrice());

        return result;
    }
}
