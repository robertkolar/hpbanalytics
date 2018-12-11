package com.highpowerbear.hpbanalytics.rest;

import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.dao.filter.FilterParser;
import com.highpowerbear.hpbanalytics.dao.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.ordtrack.HeartbeatControl;
import com.highpowerbear.hpbanalytics.ordtrack.OrdTrackService;
import com.highpowerbear.hpbanalytics.ordtrack.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by robertk on 11/18/2017.
 */
@RestController
@RequestMapping("/ordtrack")
public class OrdTrackRestController {

    private final OrdTrackDao ordTrackDao;
    private final OrdTrackService ordTrackService;
    private final FilterParser filterParser;
    private final HeartbeatControl heartbeatControl;

    @Autowired
    public OrdTrackRestController(OrdTrackDao ordTrackDao, OrdTrackService ordTrackService, FilterParser filterParser, HeartbeatControl heartbeatControl) {
        this.ordTrackDao = ordTrackDao;
        this.ordTrackService = ordTrackService;
        this.filterParser = filterParser;
        this.heartbeatControl = heartbeatControl;
    }

    @RequestMapping("/ibaccounts")
    public RestList<IbAccount> getIbAccount() {
        List<IbAccount> ibAccounts = ordTrackDao.getIbAccounts();
        ibAccounts.forEach(ibAccount -> ibAccount.setConnected(ordTrackService.isConnected(ibAccount.getAccountId())));

        return new RestList<>(ibAccounts, (long) ibAccounts.size());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/ibaccounts")
    public ResponseEntity<?> updateIbAccount(
            @RequestBody IbAccount ibAccount) {

        IbAccount ibAccountDb = ordTrackDao.findIbAccount(ibAccount.getAccountId());
        if (ibAccountDb == null) {
            return ResponseEntity.notFound().build();
        }
        ibAccountDb = ordTrackDao.updateIbAccount(ibAccount);

        return ResponseEntity.ok(ibAccountDb);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/ibaccounts/{accountId}/connect/{connect}")
    public ResponseEntity<?> connectIbAccount(
            @PathVariable("accountId") String accountId,
            @PathVariable("connect") boolean connect) {

        IbAccount ibAccount = ordTrackDao.findIbAccount(accountId);
        if (ibAccount == null) {
            return ResponseEntity.notFound().build();
        }

        if (connect && !ordTrackService.isConnected(accountId)) {
            ordTrackService.connect(accountId);
        } else if (!connect && ordTrackService.isConnected(accountId)){
            ordTrackService.disconnect(accountId);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            // Ignore
        }
        ibAccount.setConnected(ordTrackService.isConnected(accountId));

        return ResponseEntity.ok(ibAccount);
    }

    @RequestMapping("/ibaccounts/{accountId}/iborders")
    public ResponseEntity<?> getFilteredIbOrders(
            @PathVariable("accountId") String accountId,
            @RequestParam(required = false, value = "filter") String jsonFilter,
            @RequestParam("start") Integer start,
            @RequestParam("limit") Integer limit) {

        IbAccount ibAccount = ordTrackDao.findIbAccount(accountId);
        if (ibAccount == null) {
            return ResponseEntity.notFound().build();
        }

        List<IbOrder> ibOrders = new ArrayList<>();

        IbOrderFilter filter = filterParser.parseIbOrderFilter(jsonFilter);
        Map<IbOrder, Integer> hm = heartbeatControl.getOpenOrderHeartbeatMap().get(accountId);

        for (IbOrder ibOrder : ordTrackDao.getFilteredIbOrders(accountId, filter, start, limit)) {
            ibOrder.setHeartbeatCount(hm.get(ibOrder));
            ibOrders.add(ibOrder);
        }

        return ResponseEntity.ok(new RestList<>(ibOrders, ordTrackDao.getNumFilteredIbOrders(accountId, filter)));
    }

    @RequestMapping("/ibaccounts/{accountId}/positions")
    public ResponseEntity<?> getPositions(
            @PathVariable("accountId") String accountId,
            @RequestParam("start") Integer start,
            @RequestParam("limit") Integer limit) {

        IbAccount ibAccount = ordTrackDao.findIbAccount(accountId);
        if (ibAccount == null) {
            return ResponseEntity.notFound().build();
        }

        List<Position> positions = ordTrackService.getPositions(accountId);
        if (positions == null) {
            positions = new ArrayList<>();
        }
        int total = positions.size();

        if (!positions.isEmpty()) {
            int fromIndex = Math.min(start, total - 1);
            int toIndex = Math.min(fromIndex + limit, total);
            List<Position> positionsPaged = positions.subList(fromIndex, toIndex);

            return ResponseEntity.ok(new RestList<>(positionsPaged, (long) total));
        } else {
            return ResponseEntity.ok(new RestList<>(positions, (long) total));
        }
    }
}