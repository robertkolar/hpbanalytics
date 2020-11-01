package com.highpowerbear.hpbanalytics.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.config.ApplicationProperties;
import com.highpowerbear.hpbanalytics.config.HanSettings;
import com.highpowerbear.hpbanalytics.database.ExchangeRate;
import com.highpowerbear.hpbanalytics.database.ExchangeRateRepository;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.model.ExchangeRates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by robertk on 10/10/2016.
 */
@Service
public class ExchangeRateService implements InitializingService, ScheduledTaskPerformer {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final HazelcastInstance hanHazelcastInstance;
    private final ApplicationProperties applicationProperties;
    private final ScheduledExecutorService executorService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                               HazelcastInstance hanHazelcastInstance,
                               ApplicationProperties applicationProperties,
                               ScheduledExecutorService executorService) {

        this.exchangeRateRepository = exchangeRateRepository;
        this.hanHazelcastInstance = hanHazelcastInstance;
        this.applicationProperties = applicationProperties;
        this.executorService = executorService;
    }

    @Override
    public void initialize() {
        log.info("initializing ExchangeRateService");
        executorService.schedule(this::retrieveExchangeRates, HanSettings.EXCHANGE_RATE_RETRIEVAL_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void performStartOfDayTasks() {
        retrieveExchangeRates();
    }

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
            exchangeRateMap().put(date, exchangeRate);
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

    public BigDecimal getExchangeRate(LocalDate localDate, Currency currency) {

        String date = HanUtil.formatExchangeRateDate(localDate);
        IMap<String, ExchangeRate> map = exchangeRateMap();

        ExchangeRate exchangeRate = map.get(date);
        if (exchangeRate == null) {
            exchangeRate = exchangeRateRepository.findById(date).orElse(null);

            if (exchangeRate == null) {
                throw new IllegalStateException("exchange rate not available for " + date);
            }
            map.put(date, exchangeRate);
        }

        double rate = exchangeRate.getRate(HanSettings.PORTFOLIO_BASE_CURRENCY, currency);
        return BigDecimal.valueOf(rate);
    }

    private IMap<String, ExchangeRate> exchangeRateMap() {
        return hanHazelcastInstance.getMap(HanSettings.HAZELCAST_EXCHANGE_RATE_MAP_NAME);
    }
}
