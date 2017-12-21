package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by robertk on 10/10/2016.
 */
@Service
public class ExchangeRateRetriever {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExchangeRateRetriever.class);

    @Autowired private ReportDao reportDao;

    private final RestTemplate restTemplate = new RestTemplate();;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public void retrieve() {
        log.info("BEGIN ExchangeRateRetriever.retrive");

        for (int i = 0; i < HanSettings.EXCHANGE_RATE_DAYS_BACK; i++) {
            ExchangeRate exchangeRate = new ExchangeRate();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, i - HanSettings.EXCHANGE_RATE_DAYS_BACK);
            String date = df.format(cal.getTime());

            exchangeRate.setDate(date);
            exchangeRate.setEurUsd(retrievePair(date, "EUR", "USD"));
            exchangeRate.setGbpUsd(retrievePair(date, "GBP", "USD"));
            exchangeRate.setAudUsd(retrievePair(date, "AUD", "USD"));
            exchangeRate.setNzdUsd(retrievePair(date, "NZD", "USD"));
            exchangeRate.setUsdChf(retrievePair(date, "USD", "CHF"));
            exchangeRate.setUsdJpy(retrievePair(date, "USD", "JPY"));
            exchangeRate.setUsdCad(retrievePair(date, "USD", "CAD"));

            reportDao.createOrUpdateExchangeRate(exchangeRate);
        }

        log.info("END ExchangeRateRetriever.retrive");
    }

    private double retrievePair(String date, String base, String symbol) {
        String query = HanSettings.EXCHANGE_RATE_URL + "/" + date + "?base=" + base + "&symbol=" + symbol;
        String response = restTemplate.getForEntity(query, String.class).getBody();
        log.info(response);

        JsonReader reader = Json.createReader(new StringReader(response));
        JsonObject jsonObject = (JsonObject) reader.read();
        Double rate = jsonObject.getJsonObject("rates").getJsonNumber(symbol).doubleValue();
        reader.close();

        return rate;
    }
}
