package com.highpowerbear.hpbanalytics.web;

import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by robertk on 11/18/2017.
 */
@RestController
public class HanRestController {

    private static final Logger log = LoggerFactory.getLogger(HanRestController.class);

    @Autowired private IbLoggerDao ibLoggerDao;

    @RequestMapping("/accounts")
    public List<IbAccount> getIbAccount() {
        log.info("begin accounts query");
        List<IbAccount> accounts =  ibLoggerDao.getIbAccounts();
        log.info("end accounts query, size=" + accounts.size());

        return accounts;
    }
}