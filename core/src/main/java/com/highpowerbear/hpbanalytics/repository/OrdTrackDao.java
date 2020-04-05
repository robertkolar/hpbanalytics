package com.highpowerbear.hpbanalytics.repository;

import com.highpowerbear.hpbanalytics.repository.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;

import java.util.List;

/**
 * Created by robertk on 3/28/2015.
 */
public interface OrdTrackDao {

    IbAccount findIbAccount(String accountId);
    List<IbAccount> getIbAccounts();
    IbAccount updateIbAccount(IbAccount ibAccount);
    List<IbOrder> getFilteredIbOrders(String accountId, IbOrderFilter filter, int start, int limit);
    long getNumFilteredIbOrders(String accountId, IbOrderFilter filter);
    List<IbOrder> getOpenIbOrders(String accountId);
    void newIbOrder(IbOrder ibOrder);
    void updateIbOrder(IbOrder ibOrder);
    IbOrder getIbOrderByPermId(String accountId, long permId);
}
