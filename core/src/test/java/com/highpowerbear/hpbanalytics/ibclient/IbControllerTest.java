package com.highpowerbear.hpbanalytics.ibclient;

import com.highpowerbear.hpbanalytics.common.MessageSender;
import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.ordtrack.model.Position;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.inject.Provider;
import java.util.Collections;

/**
 * Created by robertk on 10/9/2018.
 */
public class IbControllerTest {

    @Mock
    private OrdTrackDao ordTrackDao;
    @Mock
    private Provider<IbListener> ibListeners;
    @Mock
    private MessageSender messageSender;
    @Mock
    private IbAccount ibAccount;
    @Mock
    private IbConnection ibConnection;
    @Mock
    private EClientSocket eClientSocket;

    private IbController ibController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(ibAccount.getAccountId()).thenReturn("AA112233");
        Mockito.when(ordTrackDao.getIbAccounts()).thenReturn(Collections.singletonList(ibAccount));
        Mockito.when(ibConnection.getClientSocket()).thenReturn(eClientSocket);
        Mockito.when(ibConnection.isConnected()).thenReturn(true);

        ibController = new IbController(ordTrackDao, ibListeners, messageSender);
        ibController.getIbConnectionMap().put(ibAccount.getAccountId(), ibConnection);
    }

    @Test
    public void testPositions() {
        Contract c1 = new Contract();
        c1.localSymbol("SPY");
        c1.symbol("SPY");
        c1.currency("USD");
        c1.secType("STK");
        c1.exchange("SMART");

        Contract c2 = new Contract();
        c2.localSymbol("IWM");
        c2.symbol("IWM");
        c2.currency("USD");
        c2.secType("STK");
        c2.exchange("SMART");

        ibController.requestPositions(ibAccount.getAccountId());
        ibController.addPosition(new Position(ibAccount.getAccountId(), c1, 1, 2));
        ibController.addPosition(new Position(ibAccount.getAccountId(), c1, 2, 3));

        Assert.assertEquals(2, ibController.getTemporaryPositionMap().get(ibAccount.getAccountId()).size());
        Assert.assertNull(ibController.getPositionMap().get(ibAccount.getAccountId()));

        ibController.positionEnd(ibAccount.getAccountId());
        Assert.assertEquals(2, ibController.getTemporaryPositionMap().get(ibAccount.getAccountId()).size());
        Assert.assertEquals(2, ibController.getPositionMap().get(ibAccount.getAccountId()).size());

        ibController.requestPositions(ibAccount.getAccountId());
        Assert.assertEquals(0, ibController.getTemporaryPositionMap().get(ibAccount.getAccountId()).size());
        Assert.assertEquals(2, ibController.getPositionMap().get(ibAccount.getAccountId()).size());

        ibController.positionEnd(ibAccount.getAccountId());
        Assert.assertEquals(0, ibController.getTemporaryPositionMap().get(ibAccount.getAccountId()).size());
        Assert.assertEquals(0, ibController.getPositionMap().get(ibAccount.getAccountId()).size());
    }
}
