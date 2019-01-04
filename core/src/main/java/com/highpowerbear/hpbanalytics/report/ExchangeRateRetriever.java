package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.report.model.ExchangeRates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * Created by robertk on 10/10/2016.
 */
@Component
public class ExchangeRateRetriever {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateRetriever.class);

    private final ReportDao reportDao;

    @Value( "${fixer.access-key}" )
    private String fixerAccessKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public ExchangeRateRetriever(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Scheduled(cron="0 0 6 * * *")
    private void retrieveExchangeRates() {
        log.info("BEGIN ExchangeRateRetriever.retrieve");

        for (int i = 0; i < CoreSettings.EXCHANGE_RATE_DAYS_BACK; i++) {
            ExchangeRate exchangeRate = new ExchangeRate();
            LocalDateTime localDateTime = LocalDateTime.now().plusDays(i - CoreSettings.EXCHANGE_RATE_DAYS_BACK);
            String date = CoreUtil.formatExchangeRateDate(localDateTime);

            exchangeRate.setDate(date);
            ExchangeRates exchangeRates = retrieve(date);

            exchangeRate.setEurUsd(exchangeRates.getRates().getUsd());
            exchangeRate.setEurGbp(exchangeRates.getRates().getGbp());
            exchangeRate.setEurChf(exchangeRates.getRates().getChf());
            exchangeRate.setEurAud(exchangeRates.getRates().getAud());
            exchangeRate.setEurJpy(exchangeRates.getRates().getJpy());
            exchangeRate.setEurKrw(exchangeRates.getRates().getKrw());
            exchangeRate.setEurHkd(exchangeRates.getRates().getHkd());
            exchangeRate.setEurSgd(exchangeRates.getRates().getSgd());

            reportDao.createOrUpdateExchangeRate(exchangeRate);
        }

        log.info("END ExchangeRateRetriever.retrieve");
    }

    private ExchangeRates retrieve(String date) {
        String query = CoreSettings.EXCHANGE_RATE_URL + "/" + date + "?access_key=" + fixerAccessKey + "&symbols=USD,GBP,CHF,AUD,JPY,KRW,HKD,SGD";
        ExchangeRates exchangeRates = restTemplate.getForObject(query, ExchangeRates.class);
        if (exchangeRates != null) {
            log.info(exchangeRates.toString());
        }

        return exchangeRates;
    }
}
