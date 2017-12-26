package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
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
import java.util.Calendar;

/**
 * Created by robertk on 10/10/2016.
 */
@Service
public class ExchangeRateRetriever {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExchangeRateRetriever.class);

    @Autowired private ReportDao reportDao;

    private final RestTemplate restTemplate = new RestTemplate();

    public void retrieve() {
        log.info("BEGIN ExchangeRateRetriever.retrive");

        for (int i = 0; i < CoreSettings.EXCHANGE_RATE_DAYS_BACK; i++) {
            ExchangeRate exchangeRate = new ExchangeRate();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, i - CoreSettings.EXCHANGE_RATE_DAYS_BACK);
            String date = CoreSettings.EXCHANGE_RATE_DATE_FORMAT.format(cal.getTime());

            exchangeRate.setDate(date);
            exchangeRate.setEurUsd(retrievePair(date, "EUR", "USD"));
            exchangeRate.setEurGbp(retrievePair(date, "EUR", "GBP"));
            exchangeRate.setEurChf(retrievePair(date, "EUR", "CHF"));
            exchangeRate.setEurAud(retrievePair(date, "EUR", "AUD"));
            exchangeRate.setEurJpy(retrievePair(date, "EUR", "JPY"));

            reportDao.createOrUpdateExchangeRate(exchangeRate);
        }

        log.info("END ExchangeRateRetriever.retrive");
    }

    private double retrievePair(String date, String base, String symbol) {
        String query = CoreSettings.EXCHANGE_RATE_URL + "/" + date + "?base=" + base + "&symbol=" + symbol;
        String response = restTemplate.getForEntity(query, String.class).getBody();
        log.info(response);

        JsonReader reader = Json.createReader(new StringReader(response));
        JsonObject jsonObject = (JsonObject) reader.read();
        Double rate = jsonObject.getJsonObject("rates").getJsonNumber(symbol).doubleValue();
        reader.close();

        return rate;
    }
}
