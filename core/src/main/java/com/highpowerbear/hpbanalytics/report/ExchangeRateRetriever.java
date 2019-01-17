package com.highpowerbear.hpbanalytics.report;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.dao.ReportDao;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.report.model.ExchangeRates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

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

        for (int i = 0; i < HanSettings.EXCHANGE_RATE_DAYS_BACK; i++) {
            ExchangeRate exchangeRate = new ExchangeRate();
            LocalDate localDate = LocalDate.now().plusDays(i - HanSettings.EXCHANGE_RATE_DAYS_BACK);
            String date = HanUtil.formatExchangeRateDate(localDate);

            exchangeRate.setDate(date);
            ExchangeRates exchangeRates = retrieve(date);

            exchangeRate.setEurUsd(exchangeRates.getRate(Currency.USD));
            exchangeRate.setEurGbp(exchangeRates.getRate(Currency.GBP));
            exchangeRate.setEurChf(exchangeRates.getRate(Currency.CHF));
            exchangeRate.setEurAud(exchangeRates.getRate(Currency.AUD));
            exchangeRate.setEurJpy(exchangeRates.getRate(Currency.JPY));
            exchangeRate.setEurKrw(exchangeRates.getRate(Currency.KRW));
            exchangeRate.setEurHkd(exchangeRates.getRate(Currency.HKD));
            exchangeRate.setEurSgd(exchangeRates.getRate(Currency.SGD));

            reportDao.createOrUpdateExchangeRate(exchangeRate);
        }

        log.info("END ExchangeRateRetriever.retrieve");
    }

    private ExchangeRates retrieve(String date) {
        String query = HanSettings.EXCHANGE_RATE_URL + "/" + date + "?access_key=" + fixerAccessKey + "&symbols=" + HanSettings.EXCHANGE_RATES_SYMBOLS;
        ExchangeRates exchangeRates = restTemplate.getForObject(query, ExchangeRates.class);

        log.info("retrieved exchange rates " + exchangeRates);
        return exchangeRates;
    }
}
