package com.highpowerbear.hpbanalytics.connector;

import com.highpowerbear.hpbanalytics.dao.OrdTrackDao;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robertk on 1/4/2019.
 */
@Component
public class Initializer {

    @Autowired
    public Initializer(OrdTrackDao ordTrackDao, Provider<IbListener> ibListenerProvider, IbController ibController) {
        List<IbListener> ibListeners = new ArrayList<>();

        for (IbAccount ibAccount : ordTrackDao.getIbAccounts()) {
            IbListener ibListener = ibListenerProvider.get();
            ibListener.setAccountId(ibAccount.getAccountId());

            ibListeners.add(ibListener);
        }
        ibController.initialize(ibListeners);
    }
}
