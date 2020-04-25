package com.highpowerbear.hpbanalytics.service;

import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.ApplicationProperties;
import com.highpowerbear.hpbanalytics.database.ExchangeRate;
import com.highpowerbear.hpbanalytics.database.ExchangeRateRepository;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.model.ExchangeRates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

/**
 * Created by robertk on 10/10/2016.
 */
@Service
public class ExchangeRateRetrieverService {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateRetrieverService.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final ApplicationProperties applicationProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public ExchangeRateRetrieverService(ExchangeRateRepository exchangeRateRepository,
                                        ApplicationProperties applicationProperties) {

        this.exchangeRateRepository = exchangeRateRepository;
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    private void init() {
        retrieveExchangeRates();
    }

    @Scheduled(cron="0 0 6 * * *")
    private void retrieveExchangeRates() {
        log.info("BEGIN ExchangeRateRetriever.retrieve");
        final int daysBack = applicationProperties.getFixer().getDaysBack();

        for (int i = 0; i < daysBack; i++) {
            LocalDate localDate = LocalDate.now().plusDays(i - daysBack);
            String date = HanUtil.formatExchangeRateDate(localDate);

            ExchangeRates exchangeRates = retrieve(date);

            ExchangeRate exchangeRate = new ExchangeRate()
                    .setDate(date)
                    .setEurUsd(exchangeRates.getRate(Currency.USD))
                    .setEurGbp(exchangeRates.getRate(Currency.GBP))
                    .setEurChf(exchangeRates.getRate(Currency.CHF))
                    .setEurAud(exchangeRates.getRate(Currency.AUD))
                    .setEurJpy(exchangeRates.getRate(Currency.JPY))
                    .setEurKrw(exchangeRates.getRate(Currency.KRW))
                    .setEurHkd(exchangeRates.getRate(Currency.HKD))
                    .setEurSgd(exchangeRates.getRate(Currency.SGD));

            exchangeRateRepository.save(exchangeRate);
        }

        log.info("END ExchangeRateRetriever.retrieve");
    }

    private ExchangeRates retrieve(String date) {
        String fixerUrl = applicationProperties.getFixer().getUrl();
        String fixerAccessKey = applicationProperties.getFixer().getAccessKey();
        String fixerSymbols = applicationProperties.getFixer().getSymbols();

        String query = fixerUrl + "/" + date + "?access_key=" + fixerAccessKey + "&symbols=" + fixerSymbols;
        ExchangeRates exchangeRates = restTemplate.getForObject(query, ExchangeRates.class);

        log.info("retrieved exchange rates " + exchangeRates);
        return exchangeRates;
    }
}
