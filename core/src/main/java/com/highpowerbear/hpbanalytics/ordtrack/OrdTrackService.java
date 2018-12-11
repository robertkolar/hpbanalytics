package com.highpowerbear.hpbanalytics.ordtrack;

import com.highpowerbear.hpbanalytics.common.CoreUtil;
import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.enums.Currency;
import com.highpowerbear.hpbanalytics.enums.SecType;
import com.highpowerbear.hpbanalytics.ibclient.IbController;
import com.highpowerbear.hpbanalytics.ordtrack.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.highpowerbear.hpbanalytics.common.CoreSettings.WS_TOPIC_ORDTRACK;

/**
 * Created by robertk on 12/11/2018.
 */
@Service
public class OrdTrackService {

    private final OrdTrackDao ordTrackDao;
    private final HeartbeatControl heartbeatControl;
    private final MessageSender messageSender;

    private IbController ibController;

    private final Map<String, Map<Integer, Position>> positionMap = new HashMap<>(); // accountId -> (conid -> position)

    @Autowired
    public OrdTrackService(OrdTrackDao ordTrackDao, HeartbeatControl heartbeatControl, MessageSender messageSender) {
        this.ordTrackDao = ordTrackDao;
        this.heartbeatControl = heartbeatControl;
        this.messageSender = messageSender;
    }

    public void setIbController(IbController ibController) {
        this.ibController = ibController;
    }

    public void connect(String accountId) {
        ibController.connect(accountId);

        if (isConnected(accountId)) {
            ibController.requestOpenOrders(accountId);
            ibController.requestPositions(accountId);
        }
    }

    public void disconnect(String accountId) {
        ibController.cancelPositions(accountId);
        CoreUtil.waitMilliseconds(1000);

        ibController.disconnect(accountId);
    }

    public boolean isConnected(String accountId) {
        return ibController.isConnected(accountId);
    }

    @Scheduled(fixedRate = 5000)
    private void reconnect() {
        ordTrackDao.getIbAccounts().stream().map(IbAccount::getAccountId).forEach(accountId -> {
            if (!ibController.isConnected(accountId) && ibController.isMarkConnected(accountId)) {
                connect(accountId);
            }
        });
    }

    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    private void performPeriodicTasks() {
        ordTrackDao.getIbAccounts().stream()
                .map(IbAccount::getAccountId)
                .filter(ibController::isConnected).forEach(accountId -> {

            heartbeatControl.updateHeartbeats(accountId);
            ibController.requestOpenOrders(accountId);
        });
    }

    public void positionChanged(String accountId, int conid, SecType secType, String underlyingSymbol, String symbol, Currency currency, String exchange, double size) {
        positionMap.computeIfAbsent(accountId, k -> new HashMap<>());
        Position position = positionMap.get(accountId).get(conid);

        if (position == null && size != 0) {
            positionMap.get(accountId).put(conid, new Position(accountId, conid, secType, underlyingSymbol, symbol, currency, exchange, size));
        } else if (size != 0){
            position.setSize(size);
        } else {
            positionMap.get(accountId).remove(conid);
        }
        messageSender.sendWsMessage(WS_TOPIC_ORDTRACK, "position changed " + symbol);
    }

    public List<Position> getPositions(String accountId) {
        if (positionMap.get(accountId) == null) {
            return null;
        }
        List<Position> positions = new ArrayList<>(positionMap.get(accountId).values());
        positions.sort(Comparator.comparing(Position::getSymbol));
        return positions;
    }
}
