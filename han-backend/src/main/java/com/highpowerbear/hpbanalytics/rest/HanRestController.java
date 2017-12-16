package com.highpowerbear.hpbanalytics.rest;

import com.highpowerbear.hpbanalytics.dao.HanDaoImpl;
import com.highpowerbear.hpbanalytics.entity.ExchangeRate;
import com.highpowerbear.hpbanalytics.entity.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertk on 11/18/2017.
 */
@RestController
public class HanRestController {

    private static final Logger log = LoggerFactory.getLogger(HanRestController.class);

    @Autowired
    private HanDaoImpl dao;

    @RequestMapping("/rates")
    public List<ExchangeRate> getAllExchangeRates() {
        log.info("begin rates query");
        List<ExchangeRate> rates =  dao.getAllExchangeRates();
        log.info("end rates query, size=" + rates.size());

        return rates;
    }

    @RequestMapping("/trades")
    public List<Trade> getAllTrades() {
        log.info("begin trades query");
        List<Trade> trades = dao.getAllTrades();
        log.info("end trades query, size=" + trades.size());

        return trades;
    }

    @RequestMapping("/trades-string")
    public List<String> getAllTradesString() {
        log.info("begin trades query");
        List<Trade> trades = dao.getAllTrades();
        log.info("end trades query, size=" + trades.size());

        List<String> result = new ArrayList<>();
        trades.forEach(t -> result.add(t.toString()));

        return result;
    }

}