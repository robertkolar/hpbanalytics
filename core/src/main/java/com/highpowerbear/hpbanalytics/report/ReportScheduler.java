package com.highpowerbear.hpbanalytics.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by robertk on 10/10/2016.
 */
@Component
public class ReportScheduler {

    private final ExchangeRateRetriever exchangeRateRetriever;

    @Autowired
    public ReportScheduler(ExchangeRateRetriever exchangeRateRetriever) {
        this.exchangeRateRetriever = exchangeRateRetriever;
    }

    @Scheduled(cron="0 0 6 * * *")
    private void retrieveExchangeRates() {
        exchangeRateRetriever.retrieve();
    }
}