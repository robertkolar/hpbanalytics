package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;

/**
 * Created by robertk on 10/10/2016.
 */
@Service
public class ExchangeRateRetriever {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateRetriever.class);

    @Autowired private ReportDao reportDao;

    @Value( "${fixer.access-key}" )
    private String fixerAccessKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void retrieve() {
        log.info("BEGIN ExchangeRateRetriever.retrieve");

        for (int i = 0; i < CoreSettings.EXCHANGE_RATE_DAYS_BACK; i++) {
            ExchangeRate exchangeRate = new ExchangeRate();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, i - CoreSettings.EXCHANGE_RATE_DAYS_BACK);
            String date = CoreUtil.formatExchangeRateDate(calendar);

            exchangeRate.setDate(date);
            RatesVO ratesVO = retrieveRates(date);

            exchangeRate.setEurUsd(ratesVO.getRates().getUsd());
            exchangeRate.setEurGbp(ratesVO.getRates().getGbp());
            exchangeRate.setEurChf(ratesVO.getRates().getChf());
            exchangeRate.setEurAud(ratesVO.getRates().getAud());
            exchangeRate.setEurJpy(ratesVO.getRates().getJpy());

            reportDao.createOrUpdateExchangeRate(exchangeRate);
        }

        log.info("END ExchangeRateRetriever.retrieve");
    }

    private RatesVO retrieveRates(String date) {
        String query = CoreSettings.EXCHANGE_RATE_URL + "/" + date + "?access_key=" + fixerAccessKey + "&symbols=USD,GBP,CHF,AUD,JPY";
        RatesVO ratesVO = restTemplate.getForObject(query, RatesVO.class);
        log.info(ratesVO.toString());

        return ratesVO;
    }
}
