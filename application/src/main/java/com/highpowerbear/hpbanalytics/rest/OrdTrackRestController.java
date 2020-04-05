package com.highpowerbear.hpbanalytics.rest;

import com.highpowerbear.hpbanalytics.connector.IbController;
import com.highpowerbear.hpbanalytics.repository.OrdTrackDao;
import com.highpowerbear.hpbanalytics.repository.filter.FilterParser;
import com.highpowerbear.hpbanalytics.repository.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.service.OrderTrackerService;
import com.highpowerbear.hpbanalytics.model.Position;
import com.highpowerbear.hpbanalytics.rest.model.GenericList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertk on 11/18/2017.
 */
@RestController
@RequestMapping("/ordtrack")
public class OrdTrackRestController {

    private final IbController ibController;
    private final OrdTrackDao ordTrackDao;
    private final OrderTrackerService orderTrackerService;
    private final FilterParser filterParser;

    @Autowired
    public OrdTrackRestController(IbController ibController, OrdTrackDao ordTrackDao, OrderTrackerService orderTrackerService, FilterParser filterParser) {
        this.ibController = ibController;
        this.ordTrackDao = ordTrackDao;
        this.orderTrackerService = orderTrackerService;
        this.filterParser = filterParser;
    }

    @RequestMapping("/ibaccounts")
    public GenericList<IbAccount> getIbAccount() {
        List<IbAccount> ibAccounts = ordTrackDao.getIbAccounts();
        ibAccounts.forEach(ibAccount -> ibAccount.setConnected(ibController.isConnected(ibAccount.getAccountId())));

        return new GenericList<>(ibAccounts, ibAccounts.size());
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

        if (connect && !ibController.isConnected(accountId)) {
            ibController.connect(accountId);
        } else if (!connect && ibController.isConnected(accountId)){
            ibController.disconnect(accountId);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            // Ignore
        }
        ibAccount.setConnected(ibController.isConnected(accountId));

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

        for (IbOrder ibOrder : ordTrackDao.getFilteredIbOrders(accountId, filter, start, limit)) {
            ibOrder.setHeartbeatCount(orderTrackerService.getHeartbeatCount(accountId, ibOrder));
            ibOrders.add(ibOrder);
        }

        return ResponseEntity.ok(new GenericList<>(ibOrders, (int) ordTrackDao.getNumFilteredIbOrders(accountId, filter)));
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

        List<Position> positions = orderTrackerService.getPositions(accountId);
        if (positions == null) {
            positions = new ArrayList<>();
        }
        int total = positions.size();

        if (!positions.isEmpty()) {
            int fromIndex = Math.min(start, total - 1);
            int toIndex = Math.min(fromIndex + limit, total);
            List<Position> positionsPaged = positions.subList(fromIndex, toIndex);

            return ResponseEntity.ok(new GenericList<>(positionsPaged, total));
        } else {
            return ResponseEntity.ok(new GenericList<>(positions, total));
        }
    }
}
