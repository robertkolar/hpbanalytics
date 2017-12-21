package com.highpowerbear.hpbanalytics.dao;

import com.highpowerbear.hpbanalytics.dao.filter.IbOrderFilter;
import com.highpowerbear.hpbanalytics.entity.IbAccount;
import com.highpowerbear.hpbanalytics.entity.IbOrder;

import java.util.List;

/**
 * Created by robertk on 3/28/2015.
 */
public interface IbLoggerDao {

    IbAccount findIbAccount(String accountId);
    List<IbAccount> getIbAccounts();
    IbAccount updateIbAccount(IbAccount ibAccount);
    List<IbOrder> getFilteredIbOrders(IbAccount ibAccount, IbOrderFilter filter, Integer start, Integer limit);
    Long getNumFilteredIbOrders(IbAccount ibAccount, IbOrderFilter filter);
    List<IbOrder> getOpenIbOrders(IbAccount ibAccount);
    void newIbOrder(IbOrder ibOrder);
    void updateIbOrder(IbOrder ibOrder);
    IbOrder getIbOrderByPermId(IbAccount ibAccount, Integer permId);
}