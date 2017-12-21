package com.highpowerbear.hpbanalytics.web;

import com.highpowerbear.hpbanalytics.common.HanSettings;
import com.highpowerbear.hpbanalytics.common.HanUtil;
import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.highpowerbear.hpbanalytics.dao.filter.FilterParser;
import com.highpowerbear.hpbanalytics.dao.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.iblogger.HeartbeatControl;
import com.highpowerbear.hpbanalytics.iblogger.IbController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by robertk on 11/18/2017.
 */
@RestController
@RequestMapping("/iblogger")
public class IbLoggerRestController {

    @Autowired private IbLoggerDao ibLoggerDao;
    @Autowired private IbController ibController;
    @Autowired FilterParser filterParser;
    @Autowired HeartbeatControl heartbeatControl;

    @RequestMapping("/ibaccounts")
    public List<IbAccount> getIbAccount() {
        return ibLoggerDao.getIbAccounts();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/ibaccounts")
    public IbAccount updateIbAccount(
            @RequestBody IbAccount ibAccount) {

        IbAccount ibAccountDb = ibLoggerDao.findIbAccount(ibAccount.getAccountId());
        if (ibAccountDb == null) {
            return null;
        }
        return ibLoggerDao.updateIbAccount(ibAccount);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/ibaccounts/{accountId}/connect/{connect}")
    public IbAccount connecIbAccount(
            @PathVariable("accountId") String accountId,
            @PathVariable("connect") boolean connect) {

        IbAccount ibAccount = ibLoggerDao.findIbAccount(accountId);
        if (connect) {
            ibController.connect(ibAccount);
        } else {
            ibController.disconnect(ibAccount);
        }

        HanUtil.waitMilliseconds(HanSettings.ONE_SECOND);
        ibAccount.setIbConnection(ibController.getIbConnection(ibAccount));

        return ibAccount;
    }

    @RequestMapping("/ibaccounts/{accountId}/iborders")
    public ResponseEntity<?> getFilteredIbOrders(
            @PathVariable("accountId") String accountId,
            @RequestParam("filter") String jsonFilter,
            @RequestParam("start") Integer start,
            @RequestParam("limit") Integer limit) {

        start = (start != null ? start : 0);
        limit = (limit != null ? limit : HanSettings.JPA_MAX_RESULTS);

        List<IbOrder> ibOrders = new ArrayList<>();
        IbAccount ibAccount = ibLoggerDao.findIbAccount(accountId);

        if (ibAccount == null) {
            return ResponseEntity.notFound().build();
        }

        IbOrderFilter filter = filterParser.parseIbOrderFilter(jsonFilter);

        for (IbOrder ibOrder : ibLoggerDao.getFilteredIbOrders(ibAccount, filter, start, limit)) {
            Map<IbOrder, Integer> hm = heartbeatControl.getOpenOrderHeartbeatMap().get(ibOrder.getIbAccount());
            ibOrder.setHeartbeatCount(hm.get(ibOrder));
            ibOrders.add(ibOrder);
        }

        return ResponseEntity.ok(new RestList<>(ibOrders, ibLoggerDao.getNumFilteredIbOrders(ibAccount, filter)));
    }
}