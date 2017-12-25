package com.highpowerbear.hpbanalytics.iblogger;

import com.highpowerbear.hpbanalytics.common.CoreSettings;
import com.highpowerbear.hpbanalytics.dao.IbLoggerDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;
import com.highpowerbear.hpbanalytics.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by robertk on 4/6/2015.
 */
@Component
public class HeartbeatControl {

    @Autowired private IbLoggerDao ibLoggerDao;

    private Map<IbAccount, Map<IbOrder, Integer>> openOrderHeartbeatMap = new HashMap<>(); // ibAccount --> (ibOrder --> number of failed heartbeats left before UNKNOWN)

    @PostConstruct
    public void init() {
        ibLoggerDao.getIbAccounts().forEach(ibAccount -> openOrderHeartbeatMap.put(ibAccount, new ConcurrentHashMap<>()));
        ibLoggerDao.getIbAccounts().stream()
                .flatMap(ibAccount -> ibLoggerDao.getOpenIbOrders(ibAccount).stream())
                .forEach(this::initHeartbeat);
    }

    public Map<IbAccount, Map<IbOrder, Integer>> getOpenOrderHeartbeatMap() {
        return openOrderHeartbeatMap;
    }

    public void updateHeartbeats(IbAccount ibAccount) {
        Map<IbOrder, Integer> hm = openOrderHeartbeatMap.get(ibAccount);
        Set<IbOrder> keyset = new HashSet<>(hm.keySet());

        for (IbOrder ibOrder : keyset) {
            Integer failedHeartbeatsLeft = hm.get(ibOrder);

            if (failedHeartbeatsLeft <= 0) {
                if (!OrderStatus.UNKNOWN.equals(ibOrder.getStatus())) {
                    ibOrder.addEvent(OrderStatus.UNKNOWN, null);
                    ibLoggerDao.updateIbOrder(ibOrder);
                }
                hm.remove(ibOrder);
            } else {
                hm.put(ibOrder, failedHeartbeatsLeft - 1);
            }
        }
    }

    public void initHeartbeat(IbOrder ibOrder) {
        openOrderHeartbeatMap.get(ibOrder.getIbAccount()).put(ibOrder, CoreSettings.MAX_ORDER_HEARTBEAT_FAILS);
    }

    public void removeHeartbeat(IbOrder ibOrder) {
        openOrderHeartbeatMap.get(ibOrder.getIbAccount()).remove(ibOrder);
    }
}